package com.example.animalmanagerapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;
import com.example.animalmanagerapp.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central SQLite access point. Handles table creation/upgrades and all
 * CRUD operations for accounts, animals, and health/feeding events,
 * including livestock type/variety/quantity, poultry fields, and the
 * sold/archive workflow.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "animal_manager.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_ANIMALS = "animals";
    public static final String COL_ANIMAL_ID = "id";
    public static final String COL_TAG_NUMBER = "tag_number";
    public static final String COL_TYPE_BREED = "type_breed";
    public static final String COL_VARIETY = "variety";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_DATE_ACQUIRED = "date_acquired";
    public static final String COL_SEX = "sex";
    public static final String COL_AGE = "age";
    public static final String COL_LAYING_COUNT = "laying_count";
    public static final String COL_TRAYS_COLLECTED = "trays_collected";
    public static final String COL_IS_SOLD = "is_sold";
    public static final String COL_SOLD_DATE = "sold_date";
    public static final String COL_SALE_AMOUNT = "sale_amount";

    public static final String TABLE_EVENTS = "health_events";
    public static final String COL_EVENT_ID = "id";
    public static final String COL_EVENT_ANIMAL_ID = "animal_id";
    public static final String COL_EVENT_TYPE = "event_type";
    public static final String COL_EVENT_DATE = "event_date";
    public static final String COL_EVENT_NOTES = "notes";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_PASSWORD_SALT = "password_salt";
    public static final String COL_SECURITY_QUESTION = "security_question";
    public static final String COL_SECURITY_ANSWER_HASH = "security_answer_hash";
    public static final String COL_SECURITY_ANSWER_SALT = "security_answer_salt";
    public static final String COL_RECOVERY_CODE_HASH = "recovery_code_hash";
    public static final String COL_RECOVERY_CODE_SALT = "recovery_code_salt";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ANIMALS + " (" +
                COL_ANIMAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAG_NUMBER + " TEXT NOT NULL, " +
                COL_TYPE_BREED + " TEXT NOT NULL, " +
                COL_VARIETY + " TEXT, " +
                COL_QUANTITY + " TEXT, " +
                COL_DATE_ACQUIRED + " TEXT NOT NULL, " +
                COL_SEX + " TEXT, " +
                COL_AGE + " TEXT, " +
                COL_LAYING_COUNT + " TEXT, " +
                COL_TRAYS_COLLECTED + " TEXT, " +
                COL_IS_SOLD + " INTEGER NOT NULL DEFAULT 0, " +
                COL_SOLD_DATE + " TEXT, " +
                COL_SALE_AMOUNT + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_ANIMAL_ID + " INTEGER NOT NULL, " +
                COL_EVENT_TYPE + " TEXT NOT NULL, " +
                COL_EVENT_DATE + " TEXT NOT NULL, " +
                COL_EVENT_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COL_EVENT_ANIMAL_ID + ") REFERENCES " +
                TABLE_ANIMALS + "(" + COL_ANIMAL_ID + ") ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COL_EMAIL + " TEXT, " +
                COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                COL_PASSWORD_SALT + " TEXT NOT NULL, " +
                COL_SECURITY_QUESTION + " TEXT NOT NULL, " +
                COL_SECURITY_ANSWER_HASH + " TEXT NOT NULL, " +
                COL_SECURITY_ANSWER_SALT + " TEXT NOT NULL, " +
                COL_RECOVERY_CODE_HASH + " TEXT NOT NULL, " +
                COL_RECOVERY_CODE_SALT + " TEXT NOT NULL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_VARIETY + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_QUANTITY + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_LAYING_COUNT + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_TRAYS_COLLECTED + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_IS_SOLD + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_SOLD_DATE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ANIMALS + " ADD COLUMN " + COL_SALE_AMOUNT + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COL_EMAIL + " TEXT, " +
                    COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                    COL_PASSWORD_SALT + " TEXT NOT NULL, " +
                    COL_SECURITY_QUESTION + " TEXT NOT NULL, " +
                    COL_SECURITY_ANSWER_HASH + " TEXT NOT NULL, " +
                    COL_SECURITY_ANSWER_SALT + " TEXT NOT NULL, " +
                    COL_RECOVERY_CODE_HASH + " TEXT NOT NULL, " +
                    COL_RECOVERY_CODE_SALT + " TEXT NOT NULL" +
                    ");");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---------------- USER / ACCOUNT CRUD ----------------

    public long addUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, user.getUsername());
        values.put(COL_EMAIL, TextUtils.isEmpty(user.getEmail()) ? null : user.getEmail());
        values.put(COL_PASSWORD_HASH, user.getPasswordHash());
        values.put(COL_PASSWORD_SALT, user.getPasswordSalt());
        values.put(COL_SECURITY_QUESTION, user.getSecurityQuestion());
        values.put(COL_SECURITY_ANSWER_HASH, user.getSecurityAnswerHash());
        values.put(COL_SECURITY_ANSWER_SALT, user.getSecurityAnswerSalt());
        values.put(COL_RECOVERY_CODE_HASH, user.getRecoveryCodeHash());
        values.put(COL_RECOVERY_CODE_SALT, user.getRecoveryCodeSalt());
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                "LOWER(" + COL_USERNAME + ") = LOWER(?)", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean emailExists(String email) {
        if (TextUtils.isEmpty(email)) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                "LOWER(" + COL_EMAIL + ") = LOWER(?)", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /** Looks up a user by username OR email, case-insensitive. */
    public User getUserByIdentifier(String identifier) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                "LOWER(" + COL_USERNAME + ") = LOWER(?) OR LOWER(" + COL_EMAIL + ") = LOWER(?)",
                new String[]{identifier, identifier}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        db.close();
        return user;
    }

    public void updatePassword(long userId, String newHash, String newSalt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD_HASH, newHash);
        values.put(COL_PASSWORD_SALT, newSalt);
        db.update(TABLE_USERS, values, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)));
        user.setEmail(safeString(cursor, COL_EMAIL));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH)));
        user.setPasswordSalt(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_SALT)));
        user.setSecurityQuestion(cursor.getString(cursor.getColumnIndexOrThrow(COL_SECURITY_QUESTION)));
        user.setSecurityAnswerHash(cursor.getString(cursor.getColumnIndexOrThrow(COL_SECURITY_ANSWER_HASH)));
        user.setSecurityAnswerSalt(cursor.getString(cursor.getColumnIndexOrThrow(COL_SECURITY_ANSWER_SALT)));
        user.setRecoveryCodeHash(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECOVERY_CODE_HASH)));
        user.setRecoveryCodeSalt(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECOVERY_CODE_SALT)));
        return user;
    }

    // ---------------- ANIMAL CRUD ----------------

    public long addAnimal(Animal animal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAG_NUMBER, animal.getTagNumber());
        values.put(COL_TYPE_BREED, animal.getAnimalType());
        values.put(COL_VARIETY, animal.getVariety());
        values.put(COL_QUANTITY, animal.getQuantity());
        values.put(COL_DATE_ACQUIRED, animal.getDateAcquired());
        values.put(COL_SEX, animal.getSex());
        values.put(COL_AGE, animal.getAge());
        values.put(COL_LAYING_COUNT, animal.getLayingCount());
        values.put(COL_TRAYS_COLLECTED, animal.getTraysCollected());
        values.put(COL_IS_SOLD, 0);
        long id = db.insert(TABLE_ANIMALS, null, values);
        db.close();
        return id;
    }

    public int updateAnimal(Animal animal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAG_NUMBER, animal.getTagNumber());
        values.put(COL_TYPE_BREED, animal.getAnimalType());
        values.put(COL_VARIETY, animal.getVariety());
        values.put(COL_QUANTITY, animal.getQuantity());
        values.put(COL_DATE_ACQUIRED, animal.getDateAcquired());
        values.put(COL_SEX, animal.getSex());
        values.put(COL_AGE, animal.getAge());
        values.put(COL_LAYING_COUNT, animal.getLayingCount());
        values.put(COL_TRAYS_COLLECTED, animal.getTraysCollected());
        int rows = db.update(TABLE_ANIMALS, values, COL_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animal.getId())});
        db.close();
        return rows;
    }

    public void markAnimalSold(long animalId, String soldDateIso, String saleAmount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_SOLD, 1);
        values.put(COL_SOLD_DATE, soldDateIso);
        values.put(COL_SALE_AMOUNT, saleAmount);
        db.update(TABLE_ANIMALS, values, COL_ANIMAL_ID + " = ?", new String[]{String.valueOf(animalId)});
        db.close();
    }

    public void unmarkAnimalSold(long animalId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_SOLD, 0);
        values.putNull(COL_SOLD_DATE);
        values.putNull(COL_SALE_AMOUNT);
        db.update(TABLE_ANIMALS, values, COL_ANIMAL_ID + " = ?", new String[]{String.valueOf(animalId)});
        db.close();
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
        if (cursor.moveToFirst()) animal = cursorToAnimal(cursor);
        cursor.close();
        db.close();
        return animal;
    }

    public List<Animal> getAllAnimals(String searchTerm) {
        List<Animal> animals = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String like = "%" + searchTerm.trim() + "%";
            cursor = db.query(TABLE_ANIMALS, null,
                    COL_IS_SOLD + " = 0 AND (" + COL_TAG_NUMBER + " LIKE ? OR " + COL_TYPE_BREED + " LIKE ? OR " + COL_VARIETY + " LIKE ?)",
                    new String[]{like, like, like}, null, null, COL_TAG_NUMBER + " ASC");
        } else {
            cursor = db.query(TABLE_ANIMALS, null, COL_IS_SOLD + " = 0", null, null, null, COL_TAG_NUMBER + " ASC");
        }
        if (cursor.moveToFirst()) {
            do { animals.add(cursorToAnimal(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return animals;
    }

    public List<Animal> getSoldAnimals(String searchTerm) {
        List<Animal> animals = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String like = "%" + searchTerm.trim() + "%";
            cursor = db.query(TABLE_ANIMALS, null,
                    COL_IS_SOLD + " = 1 AND (" + COL_TAG_NUMBER + " LIKE ? OR " + COL_TYPE_BREED + " LIKE ?)",
                    new String[]{like, like}, null, null, COL_SOLD_DATE + " DESC");
        } else {
            cursor = db.query(TABLE_ANIMALS, null, COL_IS_SOLD + " = 1", null, null, null, COL_SOLD_DATE + " DESC");
        }
        if (cursor.moveToFirst()) {
            do { animals.add(cursorToAnimal(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return animals;
    }

    private Animal cursorToAnimal(Cursor cursor) {
        Animal animal = new Animal();
        animal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ANIMAL_ID)));
        animal.setTagNumber(cursor.getString(cursor.getColumnIndexOrThrow(COL_TAG_NUMBER)));
        animal.setAnimalType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE_BREED)));
        animal.setDateAcquired(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_ACQUIRED)));
        animal.setSex(cursor.getString(cursor.getColumnIndexOrThrow(COL_SEX)));
        animal.setAge(cursor.getString(cursor.getColumnIndexOrThrow(COL_AGE)));

        animal.setVariety(safeString(cursor, COL_VARIETY));
        animal.setQuantity(safeString(cursor, COL_QUANTITY));
        animal.setLayingCount(safeString(cursor, COL_LAYING_COUNT));
        animal.setTraysCollected(safeString(cursor, COL_TRAYS_COLLECTED));
        animal.setSold(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SOLD)) == 1);
        animal.setSoldDate(safeString(cursor, COL_SOLD_DATE));
        animal.setSaleAmount(safeString(cursor, COL_SALE_AMOUNT));

        return animal;
    }

    private String safeString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndexOrThrow(column);
        return cursor.isNull(idx) ? "" : cursor.getString(idx);
    }

    // ---------------- HEALTH / FEEDING EVENT CRUD ----------------

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
                new String[]{String.valueOf(animalId)}, null, null, COL_EVENT_DATE + " DESC");
        if (cursor.moveToFirst()) {
            do { events.add(cursorToEvent(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return events;
    }

    public HealthEvent getMostRecentEvent(long animalId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COL_EVENT_ANIMAL_ID + " = ?",
                new String[]{String.valueOf(animalId)}, null, null, COL_EVENT_DATE + " DESC", "1");
        HealthEvent event = null;
        if (cursor.moveToFirst()) event = cursorToEvent(cursor);
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

    // ---------------- SUMMARY QUERIES ----------------

    public int getTotalAnimalCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ANIMALS + " WHERE " + COL_IS_SOLD + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getDistinctTypeCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COL_TYPE_BREED + ") FROM " + TABLE_ANIMALS +
                " WHERE " + COL_IS_SOLD + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getTotalHeadCount() {
        int total = 0;
        for (Animal animal : getAllAnimals(null)) {
            total += parseQuantity(animal.getQuantity());
        }
        return total;
    }

    public Map<String, Integer> getHeadCountByType() {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Animal animal : getAllAnimals(null)) {
            int qty = parseQuantity(animal.getQuantity());
            totals.put(animal.getAnimalType(), totals.getOrDefault(animal.getAnimalType(), 0) + qty);
        }
        return totals;
    }

    public double getTotalSalesRevenue() {
        double total = 0;
        for (Animal animal : getSoldAnimals(null)) {
            String amount = animal.getSaleAmount();
            if (amount != null && !amount.trim().isEmpty()) {
                try { total += Double.parseDouble(amount.trim()); } catch (NumberFormatException ignored) { }
            }
        }
        return total;
    }

    private int parseQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) return 1;
        try { return Integer.parseInt(quantity.trim()); } catch (NumberFormatException e) { return 1; }
    }

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