package com.example.finals;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message extends AppCompatActivity {

    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private RecyclerView rvChatMessages;

    private DatabaseReference messageDatabaseReference;
    private String username; // Passed from MainActivity2 (Sender's email)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message); // Ensure this matches your layout file name

        // 1. Get the username/email passed from MainActivity2
        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "Anonymous User";
        }

        // 2. Initialize XML Views
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        rvChatMessages = findViewById(R.id.rvChatMessages);

        // Setup LayoutManager for chat lists (stacks messages from bottom up)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        // 3. ✅ TABLE NAME FIXED: Points to "ChatRooms" table node instead of "messages"
        messageDatabaseReference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        Toast.makeText(this, "Connected to ChatRooms Table", Toast.LENGTH_SHORT).show();

        // 4. Send Message Button Click
        btnSendMessage.setOnClickListener(v -> {
            String messageText = etMessageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessageToFirebase(messageText);
            }
        });

        // 5. Start listening for incoming database messages
        listenForMessages();
    }

    // Function to push data into the new table name node structure
    private void sendMessageToFirebase(String text) {
        // Create a distinct auto-generated timestamp/ID key under ChatRooms node
        String messageId = messageDatabaseReference.push().getKey();

        if (messageId != null) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("sender", username);
            messageMap.put("message", text);
            messageMap.put("timestamp", System.currentTimeMillis());

            messageDatabaseReference.child(messageId).setValue(messageMap)
                    .addOnSuccessListener(aVoid -> {
                        etMessageInput.setText(""); // Clear text input bar on successful send
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Message.this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Listens for real-time changes inside the database root "ChatRooms"
    private void listenForMessages() {
        messageDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This updates automatically whenever a user sends a new chat message
                if (snapshot.exists()) {
                    // Chat loading implementation / parsing mapping goes here
                    // e.g., refreshing your ChatAdapter dataset list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Message.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}