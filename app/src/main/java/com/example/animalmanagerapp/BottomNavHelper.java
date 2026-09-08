package com.example.animalmanagerapp;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Shared bottom navigation wiring for Home, Animals, Archive, and Yields. */
public class BottomNavHelper {

    public static void setup(BottomNavigationView bottomNav, Activity current, int selectedId) {
        bottomNav.setSelectedItemId(selectedId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }
            Class<?> target = null;
            if (id == R.id.nav_home) {
                target = MainActivity.class;
            } else if (id == R.id.nav_animals) {
                target = AnimalListActivity.class;
            } else if (id == R.id.nav_archive) {
                target = ArchiveActivity.class;
            } else if (id == R.id.nav_yields) {
                target = YieldsActivity.class;
            }
            if (target != null) {
                current.startActivity(new Intent(current, target));
                current.finish();
            }
            return true;
        });
    }
}