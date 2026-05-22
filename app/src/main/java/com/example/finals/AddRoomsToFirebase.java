package com.example.finals;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddRoomsToFirebase extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wala tayong layout, tatakbo lang ito
        db = FirebaseFirestore.getInstance();

        // Ilagay lahat ng kwarto
        addAllRoomsToFirebase();

        Toast.makeText(this, "✅ TAPOS NA! PUNTA KA NA SA DETAILS, MERON NA.", Toast.LENGTH_LONG).show();
    }


    private void addAllRoomsToFirebase() {

        // ============= SD DORM 2 =============
        addRoom("SD Dorm 2", "Room 1 - 2F", "Bedspace", 4, 0);
        addRoom("SD Dorm 2", "Room 2 - 2F", "Bedspace", 4, 0);
        addRoom("SD Dorm 2", "Room 3 - 2F", "Bedspace", 4, 0);
        addRoom("SD Dorm 2", "Room 4 - 2F", "Bedspace", 4, 0);
        addRoom("SD Dorm 2", "Room 1 - 3F", "Transient", 1, 0);
        addRoom("SD Dorm 2", "Room 2 - 3F", "Transient", 1, 0);
        addRoom("SD Dorm 2", "Room 3 - 3F", "Transient", 1, 0);
        addRoom("SD Dorm 2", "Room 4 - 3F", "Transient", 1, 0);

        // ============= PALAR DORMITORY =============
        addRoom("Palar Dormitory", "Room 1 - 1F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 2 - 1F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 1 - 2F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 2 - 2F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 3 - 2F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 1 - 3F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 2 - 3F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 3 - 3F", "Bedspace", 4, 0);
        addRoom("Palar Dormitory", "Room 1 - 4F", "Transient", 1, 0);
        addRoom("Palar Dormitory", "Room 2 - 4F", "Transient", 1, 0);

        // ============= MILFLORES =============
        addRoom("Milflores Boarding House", "Room 1 - 1F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 2 - 1F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 1 - 2F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 2 - 2F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 3 - 2F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 4 - 2F", "Bedspace", 2, 0);
        addRoom("Milflores Boarding House", "Room 5 - 2F", "Bedspace", 2, 0);

        // ============= LOFT 22 =============
        addRoom("Loft 22", "Room 1 - 3F", "Transient", 1, 0);
        addRoom("Loft 22", "Room 2 - 3F", "Transient", 1, 0);
        addRoom("Loft 22", "Room 1 - 4F", "Transient", 1, 0);
        addRoom("Loft 22", "Room 2 - 4F", "Transient", 1, 0);
        addRoom("Loft 22", "Room 1 - 5F", "Transient", 1, 0);

        // ============= THE DORMITORY =============
        addRoom("The Dormitory", "Room 1", "Transient", 2, 0);
        addRoom("The Dormitory", "Room 2", "Bedspace", 4, 0);

    }


    private void addRoom(String property, String roomName, String type, int cap, int occ) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("property", property);
        roomData.put("room_name", roomName);
        roomData.put("type", type);
        roomData.put("capacity", cap);
        roomData.put("occupied", occ);

        // I-save sa Firebase
        db.collection("rooms")
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}