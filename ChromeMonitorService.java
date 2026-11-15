package com.searchlock.app;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

public class ChromeMonitorService extends AccessibilityService {
    private long lastAlert = 0;
    private String lastMatch = "";

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !"com.android.chrome".contentEquals(event.getPackageName())) return;
        android.content.SharedPreferences p = getSharedPreferences("searchlock", MODE_PRIVATE);
        if (!p.getBoolean("active", false)) return;
        String target = p.getString("phrase", "").trim().toLowerCase(Locale.ROOT);
        if (target.isEmpty()) return;

        StringBuilder visible = new StringBuilder();
        if (event.getText() != null) for (CharSequence s : event.getText()) if (s != null) visible.append(s).append(' ');
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            collectText(root, visible, 0);
            root.recycle();
        }
        String current = visible.toString().toLowerCase(Locale.ROOT);
        if (current.contains(target)) {
            long now = SystemClock.elapsedRealtime();
            if (now - lastAlert > 8000 || !target.equals(lastMatch)) {
                lastAlert = now; lastMatch = target;
                String custom = p.getString("message", "You asked Search Lock to remind you.");
                Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_lock_lock)
                    .setContentTitle("Search Lock reminder")
                    .setContentText(custom)
                    .setStyle(new Notification.BigTextStyle().bigText(custom))
                    .setAutoCancel(true);
                if (android.os.Build.VERSION.SDK_INT >= 26) builder.setChannelId("search_alerts");
                ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(401, builder.build());
            }
        }
    }
    private void collectText(AccessibilityNodeInfo node, StringBuilder out, int depth) {
        if (node == null || depth > 18) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null) out.append(text).append(' ');
        if (desc != null) out.append(desc).append(' ');
        for (int i=0; i<node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) { collectText(child, out, depth+1); child.recycle(); }
        }
    }
    @Override public void onInterrupt() {}
}
