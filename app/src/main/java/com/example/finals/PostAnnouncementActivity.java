package com.example.finals;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class PostAnnouncementActivity extends AppCompatActivity {

    EditText etTitle, etMessage;
    Spinner spnType;
    String selectedType = "info";

    // ✅ CHANGED: Using Realtime Database reference instead of Firestore
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_announcement);

        // ✅ INITIALIZE: Realtime Database point
        databaseReference = FirebaseDatabase.getInstance().getReference("Announcements");

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        spnType = findViewById(R.id.spnType);

        String[] types = {"info", "payment", "maintenance", "alert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);

        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = types[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnSend).setOnClickListener(v -> sendAnnouncement());
    }

    private void sendAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("type", selectedType);
        announcement.put("timestamp", System.currentTimeMillis());

        // ✅ FIXED: Generates a unique ID under "Announcements" table node
        String announcementId = databaseReference.push().getKey();

        if (announcementId != null) {
            databaseReference.child(announcementId).setValue(announcement)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostAnnouncementActivity.this, "Announcement Sent ✅", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PostAnnouncementActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}