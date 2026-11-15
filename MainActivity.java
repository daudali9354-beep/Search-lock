package com.searchlock.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.*;

public class MainActivity extends Activity {
    EditText phrase, message, pin;
    Switch active;
    android.content.SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("searchlock", MODE_PRIVATE);
        createChannel();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 32, 36, 24);
        layout.setBackgroundColor(0xFF101827);

        TextView title = new TextView(this);
        title.setText("🔐  SEARCH LOCK");
        title.setTextSize(26); title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        layout.addView(title, params(-1, -2, 0, 0, 0, 24));

        phrase = field("Phrase to watch (private)");
        phrase.setText(prefs.getString("phrase", ""));
        layout.addView(phrase, params(-1, -2, 0, 0, 0, 12));

        message = field("Your notification message");
        message.setMinLines(2);
        message.setText(prefs.getString("message", "You asked Search Lock to remind you about this search."));
        layout.addView(message, params(-1, -2, 0, 0, 0, 12));

        pin = field("Create a 4–8 digit PIN");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setText(prefs.getString("pin", ""));
        layout.addView(pin, params(-1, -2, 0, 0, 0, 18));

        Button save = new Button(this); save.setText("SAVE SETTINGS");
        layout.addView(save, params(-1, -2, 0, 0, 0, 8));
        save.setOnClickListener(v -> saveSettings());

        active = new Switch(this);
        active.setText("  Activate Chrome notifications");
        active.setTextColor(0xFFFFFFFF);
        active.setTextSize(16);
        active.setChecked(prefs.getBoolean("active", false));
        layout.addView(active, params(-1, -2, 0, 10, 0, 12));
        active.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                saveSettings();
                if (prefs.getString("phrase", "").trim().isEmpty()) {
                    active.setChecked(false);
                    Toast.makeText(this, "Pehle phrase save karo", Toast.LENGTH_LONG).show();
                    return;
                }
                prefs.edit().putBoolean("active", true).apply();
                showAccessibilityInstructions();
            } else {
                if (verifyPin()) prefs.edit().putBoolean("active", false).apply();
                else active.setChecked(true);
            }
        });

        Button access = new Button(this); access.setText("ENABLE CHROME MONITORING");
        layout.addView(access, params(-1, -2, 0, 8, 0, 8));
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        TextView note = new TextView(this);
        note.setText("Works only when Android Accessibility access is enabled. Detection depends on what Chrome exposes to Android; it may miss some searches and cannot guarantee blocking. Matching is done on this phone; nothing is uploaded.");
        note.setTextColor(0xFFCFD8E3); note.setTextSize(13);
        layout.addView(note, params(-1, -2, 0, 18, 0, 0));

        ScrollView scroll = new ScrollView(this); scroll.addView(layout); setContentView(scroll);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 10);
    }

    EditText field(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setTextColor(0xFFFFFFFF);
        e.setHintTextColor(0xFF9AA8BA); e.setSingleLine(false);
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF65B5FF));
        return e;
    }
    LinearLayout.LayoutParams params(int w,int h,int l,int t,int r,int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h);
        p.setMargins(l,t,r,b); return p;
    }
    void saveSettings() {
        String p = phrase.getText().toString().trim();
        String m = message.getText().toString().trim();
        String code = pin.getText().toString().trim();
        if (p.isEmpty() || m.isEmpty() || code.length() < 4 || code.length() > 8) {
            Toast.makeText(this, "Phrase, message aur 4–8 digit PIN zaroori hai", Toast.LENGTH_LONG).show(); return;
        }
        prefs.edit().putString("phrase", p).putString("message", m).putString("pin", code).apply();
        Toast.makeText(this, "Settings saved on this phone", Toast.LENGTH_SHORT).show();
    }
    boolean verifyPin() {
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(this).setTitle("Enter PIN to deactivate").setView(input)
            .setPositiveButton("Unlock", (d,w) -> {
                if (input.getText().toString().equals(prefs.getString("pin",""))) {
                    prefs.edit().putBoolean("active", false).apply();
                    active.setChecked(false);
                } else Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", (d,w) -> active.setChecked(true)).show();
        return false;
    }
    void showAccessibilityInstructions() {
        new AlertDialog.Builder(this).setTitle("One more step")
            .setMessage("Android Settings mein Search Lock ko Accessibility access do. Phir Chrome mein phrase type karke test karo. Android permission tumhe khud enable karni hogi.")
            .setPositiveButton("Open settings", (d,w) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
            .setNegativeButton("Later", null).show();
    }
    void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("search_alerts", "Search Lock alerts", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Your custom Chrome search reminders");
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }
}
