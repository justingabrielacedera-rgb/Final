package com.example.finals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageRoomsActivity extends AppCompatActivity {

    DatabaseReference roomsDatabaseReference;
    ListView listRooms;
    ArrayList<Map<String, String>> roomList;
    RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rooms);

        listRooms = findViewById(R.id.listRooms);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddRoom);

        roomsDatabaseReference = FirebaseDatabase.getInstance().getReference("Rooms");

        loadRooms();

        fabAdd.setOnClickListener(v -> showAddRoomDialog());

        listRooms.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> selected = roomList.get(position);
            String roomId = selected.get("id");
            if (roomId != null && !roomId.isEmpty()) {
                showRoomOptionsDialog(roomId, selected);
            }
        });
    }

    private void loadRooms() {
        roomList = new ArrayList<>();

        roomsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();

                if (!snapshot.exists()) {
                    // ✅ AUTO-POPULATE DATABASE WITH YOUR EXACT RULES IF EMPTY
                    generateYourExactDefaultRooms();
                    return;
                }

                for (DataSnapshot doc : snapshot.getChildren()) {
                    Map<String, String> room = new HashMap<>();
                    room.put("id", doc.getKey());
                    room.put("property", doc.child("property").getValue(String.class));
                    room.put("room_name", doc.child("room_name").getValue(String.class));
                    room.put("floor", doc.child("floor").getValue(String.class));
                    room.put("type", doc.child("type").getValue(String.class));
                    room.put("price", doc.child("price").getValue(String.class));
                    room.put("amenities", doc.child("amenities").getValue(String.class));

                    Object cap = doc.child("capacity").getValue();
                    Object occ = doc.child("occupied").getValue();
                    room.put("capacity", cap != null ? String.valueOf(cap) : "0");
                    room.put("occupied", occ != null ? String.valueOf(occ) : "0");

                    roomList.add(room);
                }

                adapter = new RoomAdapter();
                listRooms.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageRoomsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ AUTOMATIC DATA SEEDING MATCHING YOUR EXACT BREAKDOWN
    private void generateYourExactDefaultRooms() {
        // --- 1. SD DORM 2 ---
        for(int i=1; i<=4; i++) {
            insertRoomData("SD Dorm 2", "Room " + i, "2nd Floor", "Bedspace", 4, "₱3,300/mo", "Standard (1mo Dep / 1mo Adv)");
        }
        for(int i=1; i<=4; i++) {
            insertRoomData("SD Dorm 2", "Room " + i + " (Solo)", "3rd Floor", "Transient", 1, "₱550/day", "Electric Fan");
        }

        // --- 2. THE DORMITORY - PALAR ---
        for(int i=1; i<=2; i++) insertRoomData("The Dormitory", "Room " + i, "1st Floor", "Bedspace", 4, "Contact Admin", "Standard");
        for(int i=1; i<=3; i++) insertRoomData("The Dormitory", "Room " + i, "2nd Floor", "Bedspace", 4, "Contact Admin", "Standard");
        for(int i=1; i<=3; i++) insertRoomData("The Dormitory", "Room " + i, "3rd Floor", "Bedspace", 4, "Contact Admin", "Standard");
        insertRoomData("The Dormitory", "Room 1 (Solo)", "4th Floor", "Transient", 1, "Contact Admin", "Standard");
        insertRoomData("The Dormitory", "Room 2 (Solo)", "4th Floor", "Transient", 1, "Contact Admin", "Standard");

        // --- 3. MILFLORES ---
        insertRoomData("Milflores Boarding House", "Room 1", "1st Floor", "Bedspace", 2, "Contact Admin", "Standard");
        insertRoomData("Milflores Boarding House", "Room 2", "1st Floor", "Bedspace", 2, "Contact Admin", "Standard");
        for(int i=1; i<=5; i++) {
            insertRoomData("Milflores Boarding House", "Room " + i, "2nd Floor", "Bedspace", 2, "Contact Admin", "Standard");
        }

        // --- 4. LOFT 22 ---
        insertRoomData("Loft 22", "Room 1", "3rd Floor", "Transient", 1, "Contact Admin", "Airconditioned");
        insertRoomData("Loft 22", "Room 2", "3rd Floor", "Transient", 1, "Contact Admin", "Airconditioned");
        insertRoomData("Loft 22", "Room 1", "4th Floor", "Transient", 1, "Contact Admin", "Airconditioned");
        insertRoomData("Loft 22", "Room 2", "4th Floor", "Transient", 1, "Contact Admin", "Airconditioned");
        insertRoomData("Loft 22", "Room 1", "5th Floor", "Transient", 1, "Contact Admin", "Airconditioned");
    }

    private void insertRoomData(String prop, String name, String floor, String type, int cap, String price, String amenities) {
        String key = roomsDatabaseReference.push().getKey();
        if (key != null) {
            Map<String, Object> room = new HashMap<>();
            room.put("property", prop);
            room.put("room_name", name);
            room.put("floor", floor);
            room.put("type", type);
            room.put("capacity", cap);
            room.put("price", price);
            room.put("amenities", amenities);
            room.put("occupied", 0);
            roomsDatabaseReference.child(key).setValue(room);
        }
    }

    private void showAddRoomDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        Spinner spnProperty = dialogView.findViewById(R.id.spnProperty);
        EditText etRoomName = dialogView.findViewById(R.id.etRoomName);
        EditText etFloor = dialogView.findViewById(R.id.etFloor);
        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etAmenities = dialogView.findViewById(R.id.etAmenities);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);

        String[] properties = {"The Dormitory", "SD Dorm 2", "Palar Dormitory", "Milflores Boarding House", "Loft 22"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, properties);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnProperty.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Room Entry")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String prop = spnProperty.getSelectedItem().toString();
                    String name = etRoomName.getText().toString().trim();
                    String floor = etFloor.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    String price = etPrice.getText().toString().trim();
                    String am = etAmenities.getText().toString().trim();
                    String capStr = etCapacity.getText().toString().trim();

                    if (name.isEmpty() || floor.isEmpty() || type.isEmpty() || capStr.isEmpty()) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int capacity = Integer.parseInt(capStr);
                    Map<String, Object> room = new HashMap<>();
                    room.put("property", prop);
                    room.put("room_name", name);
                    room.put("floor", floor);
                    room.put("type", type);
                    room.put("capacity", capacity);
                    room.put("price", price.isEmpty() ? "Contact Admin" : price);
                    room.put("amenities", am.isEmpty() ? "Standard" : am);
                    room.put("occupied", 0);

                    String key = roomsDatabaseReference.push().getKey();
                    if (key != null) {
                        roomsDatabaseReference.child(key).setValue(room);
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showRoomOptionsDialog(String roomId, Map<String, String> data) {
        String[] options = {"Mark as Available/Occupied", "Delete Room"};
        new AlertDialog.Builder(this)
                .setTitle(data.get("property") + " - " + data.get("room_name"))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        int newStatus = "0".equals(data.get("occupied")) ? 1 : 0;
                        roomsDatabaseReference.child(roomId).child("occupied").setValue(newStatus);
                    } else if (which == 1) {
                        roomsDatabaseReference.child(roomId).removeValue();
                    }
                }).show();
    }

    // ✅ PREMIUM LIST ADAPTER GENERATING DASHBOARD CARDS
    private class RoomAdapter extends BaseAdapter {
        @Override public int getCount() { return roomList.size(); }
        @Override public Object getItem(int position) { return roomList.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ManageRoomsActivity.this).inflate(R.layout.item_room, parent, false);
            }

            Map<String, String> room = roomList.get(position);

            TextView tvName = convertView.findViewById(R.id.tvRoomName);
            TextView tvProperty = convertView.findViewById(R.id.tvProperty);
            TextView tvType = convertView.findViewById(R.id.tvType);
            TextView tvCapacity = convertView.findViewById(R.id.tvCapacity);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            View statusDot = convertView.findViewById(R.id.statusDot);

            // Display combinations matching your exact guidelines
            tvName.setText(room.get("room_name") + " (" + room.get("floor") + ")");
            tvProperty.setText(room.get("property"));
            tvType.setText(room.get("type") + " • " + room.get("amenities"));
            tvCapacity.setText("Max Capacity: " + room.get("capacity") + " tenants | " + room.get("price"));

            android.graphics.drawable.GradientDrawable dotDrawable = new android.graphics.drawable.GradientDrawable();
            dotDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);

            if ("0".equals(room.get("occupied"))) {
                tvStatus.setText("FREE");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#38A169"));
                dotDrawable.setColor(android.graphics.Color.parseColor("#38A169"));
            } else {
                tvStatus.setText("BOOKED");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#E53E3E"));
                dotDrawable.setColor(android.graphics.Color.parseColor("#E53E3E"));
            }
            statusDot.setBackground(dotDrawable);

            return convertView;
        }
    }
}