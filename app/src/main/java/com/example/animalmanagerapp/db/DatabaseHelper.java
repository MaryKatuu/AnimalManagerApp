package com.example.animalmanagerapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Central SQLite access point for the app. Handles table creation and all
 * create / read / update / delete operations for animals and their health
 * / feeding event logs.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "animal_manager.db";
    private static final int DATABASE_VERSION = 1;

    // Animals table
    public static final String TABLE_ANIMALS = "animals";
    public static final String COL_ANIMAL_ID = "id";
    public static final String COL_TAG_NUMBER = "tag_number";
    public static final String COL_TYPE_BREED = "type_breed";
    public static final String COL_DATE_ACQUIRED = "date_acquired";
    public static final String COL_SEX = "sex";
    public static final String COL_AGE = "age";

    // Health events table
    public static final String TABLE_EVENTS = "health_events";
    public static final String COL_EVENT_ID = "id";
    public static final String COL_EVENT_ANIMAL_ID = "animal_id";
    public static final String COL_EVENT_TYPE = "event_type";
    public static final String COL_EVENT_DATE = "event_date";
    public static final String COL_EVENT_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAnimals = "CREATE TABLE " + TABLE_ANIMALS + " (" +
                COL_ANIMAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAG_NUMBER + " TEXT NOT NULL, " +
                COL_TYPE_BREED + " TEXT NOT NULL, " +
                COL_DATE_ACQUIRED + " TEXT NOT NULL, " +
                COL_SEX + " TEXT, " +
                COL_AGE + " TEXT" +
                ");";

        String createEvents = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_ANIMAL_ID + " INTEGER NOT NULL, " +
                COL_EVENT_TYPE + " TEXT NOT NULL, " +
                COL_EVENT_DATE + " TEXT NOT NULL, " +
                COL_EVENT_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COL_EVENT_ANIMAL_ID + ") REFERENCES " +
                TABLE_ANIMALS + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE" +
                ");";

        db.execSQL(createAnimals);
        db.execSQL(createEvents);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMALS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---------------------------------------------------------------
    // ANIMAL CRUD
    // ---------------------------------------------------------------

    public long addAnimal(Animal animal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAG_NUMBER, animal.getTagNumber());
        values.put(COL_TYPE_BREED, animal.getTypeBreed());
        values.put(COL_DATE_ACQUIRED, animal.getDateAcquired());
        values.put(COL_SEX, animal.getSex());
        values.put(COL_AGE, animal.getAge());
        long id = db.insert(TABLE_ANIMALS, null, values);
        db.close();
        return id;
    }

    public int updateAnimal(Animal animal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAG_NUMBER, animal.getTagNumber());
        values.put(COL_TYPE_BREED, animal.getTypeBreed());
        values.put(COL_DATE_ACQUIRED, animal.getDateAcquired());
        values.put(COL_SEX, animal.getSex());
        values.put(COL_AGE, animal.getAge());
        int rows = db.update(TABLE_ANIMALS, values, COL_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animal.getId())});
        db.close();
        return rows;
    }

    public void deleteAnimal(long animalId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENTS, COL_EVENT_ANIMAL_ID + " = ?", new String[]{String.valueOf(animalId)});
        db.delete(TABLE_ANIMALS, COL_ANIMAL_ID + " = ?", new String[]{String.valueOf(animalId)});
        db.close();
    }

    public Animal getAnimal(long animalId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ANIMALS, null, COL_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animalId)}, null, null, null);

        Animal animal = null;
        if (cursor.moveToFirst()) {
            animal = cursorToAnimal(cursor);
        }
        cursor.close();
        db.close();
        return animal;
    }

    /**
     * Returns all animals, optionally filtered by a search term matching
     * the tag number or type/breed (case-insensitive, partial match).
     * Pass null or an empty string to return every animal.
     */
    public List<Animal> getAllAnimals(String searchTerm) {
        List<Animal> animals = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String like = "%" + searchTerm.trim() + "%";
            cursor = db.query(TABLE_ANIMALS, null,
                    COL_TAG_NUMBER + " LIKE ? OR " + COL_TYPE_BREED + " LIKE ?",
                    new String[]{like, like}, null, null,
                    COL_TAG_NUMBER + " ASC");
        } else {
            cursor = db.query(TABLE_ANIMALS, null, null, null, null, null,
                    COL_TAG_NUMBER + " ASC");
        }

        if (cursor.moveToFirst()) {
            do {
                animals.add(cursorToAnimal(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return animals;
    }

    private Animal cursorToAnimal(Cursor cursor) {
        Animal animal = new Animal();
        animal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ANIMAL_ID)));
        animal.setTagNumber(cursor.getString(cursor.getColumnIndexOrThrow(COL_TAG_NUMBER)));
        animal.setTypeBreed(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE_BREED)));
        animal.setDateAcquired(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_ACQUIRED)));
        animal.setSex(cursor.getString(cursor.getColumnIndexOrThrow(COL_SEX)));
        animal.setAge(cursor.getString(cursor.getColumnIndexOrThrow(COL_AGE)));
        return animal;
    }

    // ---------------------------------------------------------------
    // HEALTH / FEEDING EVENT CRUD
    // ---------------------------------------------------------------

    public long addEvent(HealthEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_ANIMAL_ID, event.getAnimalId());
        values.put(COL_EVENT_TYPE, event.getEventType());
        values.put(COL_EVENT_DATE, event.getEventDate());
        values.put(COL_EVENT_NOTES, event.getNotes());
        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    public List<HealthEvent> getEventsForAnimal(long animalId) {
        List<HealthEvent> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COL_EVENT_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animalId)}, null, null,
                COL_EVENT_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return events;
    }

    /**
     * Most recent health/feeding event logged for this animal (by date),
     * or null if none have been logged yet. Powers the "days since last
     * treatment" status badge.
     */
    public HealthEvent getMostRecentEvent(long animalId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COL_EVENT_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animalId)}, null, null,
                COL_EVENT_DATE + " DESC", "1");

        HealthEvent event = null;
        if (cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
        }
        cursor.close();
        db.close();
        return event;
    }

    private HealthEvent cursorToEvent(Cursor cursor) {
        HealthEvent event = new HealthEvent();
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ID)));
        event.setAnimalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ANIMAL_ID)));
        event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TYPE)));
        event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE)));
        event.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_NOTES)));
        return event;
    }

    public void deleteEvent(long eventId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENTS, COL_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
    }

    // ---------------------------------------------------------------
    // SUMMARY QUERIES (for the dashboard)
    // ---------------------------------------------------------------

    public int getTotalAnimalCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ANIMALS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getDistinctTypeCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COL_TYPE_BREED + ") FROM " + TABLE_ANIMALS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Animals that have never had a health/feeding event logged, and
     * animals whose most recent event was more than {@code overdueDays}
     * days ago. Used to power a dashboard "needs attention" list.
     */
    public List<Animal> getAnimalsNeedingAttention(int overdueDays) {
        List<Animal> result = new ArrayList<>();
        List<Animal> all = getAllAnimals(null);
        for (Animal animal : all) {
            HealthEvent lastEvent = getMostRecentEvent(animal.getId());
            if (lastEvent == null) {
                result.add(animal);
            } else {
                int daysSince = DateUtils.daysSince(lastEvent.getEventDate());
                if (daysSince != Integer.MIN_VALUE && daysSince >= overdueDays) {
                    result.add(animal);
                }
            }
        }
        return result;
    }
}