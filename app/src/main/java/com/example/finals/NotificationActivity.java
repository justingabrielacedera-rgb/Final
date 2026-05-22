package com.example.finals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Map<String, String>> notifList;
    NotificationAdapter adapter;
    DatabaseReference notifRef;

    // GET THIS FROM LOGIN / CURRENT USER
    int currentTenantId = 1; // ✅ Will get dynamically from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // GET TENANT ID FROM INTENT
        if (getIntent().hasExtra("TENANT_ID")) {
            currentTenantId = getIntent().getIntExtra("TENANT_ID", 1);
        }

        listView = findViewById(R.id.listNotif);

        // INIT FIREBASE DATABASE REFERENCE
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        loadNotifications();
    }

    private void loadNotifications() {
        notifList = new ArrayList<>();

        // QUERY: GET NOTIFICATIONS FOR CURRENT TENANT, ORDERED BY NEWEST FIRST
        notifRef.orderByChild("tenantId").equalTo(currentTenantId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        notifList.clear();

                        if (dataSnapshot.exists()) {
                            // LOOP THROUGH ALL NOTIFICATIONS
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Map<String, String> notif = new HashMap<>();
                                notif.put("id", snapshot.getKey()); // Firebase unique ID
                                notif.put("title", snapshot.child("title").getValue(String.class));
                                notif.put("message", snapshot.child("message").getValue(String.class));
                                notif.put("type", snapshot.child("type").getValue(String.class));
                                notif.put("date", snapshot.child("date").getValue(String.class));
                                notif.put("is_read", snapshot.child("isRead").getValue(Boolean.class) ? "1" : "0");

                                notifList.add(notif);
                            }

                            // REVERSE LIST TO SHOW NEWEST FIRST
                            java.util.Collections.reverse(notifList);

                        } else {
                            // NO NOTIFICATIONS
                            Map<String, String> empty = new HashMap<>();
                            empty.put("id", "0");
                            empty.put("title", "📭 No Notifications");
                            empty.put("message", "You don't have any updates yet.");
                            empty.put("type", "info");
                            empty.put("date", "");
                            empty.put("is_read", "1");
                            notifList.add(empty);
                        }

                        adapter = new NotificationAdapter();
                        listView.setAdapter(adapter);

                        // ✅ MARK AS READ WHEN CLICKED
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Map<String, String> selected = notifList.get(position);
                            String notifId = selected.get("id");
                            String isRead = selected.get("is_read");

                            if (isRead != null && isRead.equals("0")) {
                                markAsRead(notifId);
                                selected.put("is_read", "1");
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ERROR HANDLING
                        notifList.clear();
                        Map<String, String> error = new HashMap<>();
                        error.put("id", "-1");
                        error.put("title", "⚠️ Error");
                        error.put("message", "Failed to load: " + databaseError.getMessage());
                        error.put("type", "error");
                        error.put("date", "");
                        error.put("is_read", "1");
                        notifList.add(error);

                        adapter = new NotificationAdapter();
                        listView.setAdapter(adapter);
                    }
                });
    }

    // ✅ UPDATE FIREBASE: SET isRead = true
    private void markAsRead(String notifId) {
        notifRef.child(notifId).child("isRead").setValue(true);
    }

    // ✅ CUSTOM ADAPTER (SAME LOGIC, NO CHANGE NEEDED)
    private class NotificationAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return notifList.size();
        }

        @Override
        public Object getItem(int position) {
            return notifList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(NotificationActivity.this)
                        .inflate(R.layout.item_notification, parent, false);
            }

            Map<String, String> notif = notifList.get(position);

            TextView tvNotifTitle = convertView.findViewById(R.id.tvNotifTitle);
            TextView tvNotifMessage = convertView.findViewById(R.id.tvNotifMessage);
            TextView tvNotifDate = convertView.findViewById(R.id.tvNotifDate);
            View dotIndicator = convertView.findViewById(R.id.dotIndicator);

            String title = notif.get("title");
            String message = notif.get("message");
            String type = notif.get("type");
            String date = notif.get("date");
            String isRead = notif.get("is_read");

            tvNotifTitle.setText(title);
            tvNotifMessage.setText(message);
            tvNotifDate.setText(date);

            // ✅ UNREAD = BOLD TEXT + SHOW DOT
            if (isRead.equals("0")) {
                tvNotifTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                dotIndicator.setVisibility(View.VISIBLE);
            } else {
                tvNotifTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
                dotIndicator.setVisibility(View.GONE);
            }

            // ✅ COLOR CODING BY TYPE
            int color;
            if (type == null) type = "";
            switch (type.toLowerCase()) {
                case "payment":
                    color = ContextCompat.getColor(NotificationActivity.this, android.R.color.holo_green_dark);
                    break;
                case "maintenance":
                    color = ContextCompat.getColor(NotificationActivity.this, android.R.color.holo_orange_dark);
                    break;
                case "alert":
                case "error":
                    color = ContextCompat.getColor(NotificationActivity.this, android.R.color.holo_red_dark);
                    break;
                case "info":
                default:
                    color = ContextCompat.getColor(NotificationActivity.this, android.R.color.darker_gray);
                    break;
            }

            tvNotifTitle.setTextColor(color);
            dotIndicator.setBackgroundColor(color);

            return convertView;
        }
    }
}