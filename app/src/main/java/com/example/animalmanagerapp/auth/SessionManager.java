package com.example.animalmanagerapp.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks whether a farmer is currently logged in, using SharedPreferences
 * (simple and sufficient for a single-user offline app on one device).
 */
public class SessionManager {

    private static final String PREFS_NAME = "animal_manager_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId, String username) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
}