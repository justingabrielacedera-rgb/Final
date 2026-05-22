package com.example.finals;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    TextView tvRoomName, tvPrice, tvDetails, tvTerms, tvCheckInTime, tvCheckOutTime;
    Button btnProceedPayment;

    String property, location, roomType, price, details;

    // ✅ FIREBASE INSTANCE
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // ✅ INIT FIREBASE
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvRoomName = findViewById(R.id.tvRoomName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDetails = findViewById(R.id.tvDetails);
        tvTerms = findViewById(R.id.tvTerms);
        tvCheckInTime = findViewById(R.id.tvCheckInTime);
        tvCheckOutTime = findViewById(R.id.tvCheckOutTime);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);

        Intent i = getIntent();
        property = i.getStringExtra("property");
        location = i.getStringExtra("location");
        roomType = i.getStringExtra("room_type");
        price = i.getStringExtra("price");
        details = i.getStringExtra("details");
        // ✅ KUNG MAY DINADALANG CHECK-IN / CHECK-OUT MULA SA NAUNANG SCREEN
        String checkin = i.getStringExtra("checkin_date");
        String checkout = i.getStringExtra("checkout_date");


        if (property != null) tvRoomName.setText(property);
        if (price != null) {
            // ✅ INAYOS: DATI MAY MALI "Transient" -> DAPAT "Transient" pa rin pero sigurado tayo
            if ("Transient".equals(roomType)) {
                tvPrice.setText("₱" + price + " / night");
            } else {
                tvPrice.setText("₱" + price + " / month");
            }
        }
        if (details != null) tvDetails.setText(details);

        // ✅ ILAGAY ANG TERMS AT ORAS DEPENDE KUNG TRANSIENT O BEDSPACE
        setTermsAndSchedule();

        btnProceedPayment.setOnClickListener(v -> {

            // ✅ TAWAGIN ANG FUNCTION NA MAG-I-SAVE SA DATABASE
            saveBookingToFirebase(checkin, checkout);

            // ✅ TULUYAN SA PAYMENT SCREEN
            Intent intent = new Intent(BookingActivity.this, UserPayment.class);
            intent.putExtra("property", property);
            intent.putExtra("location", location);
            intent.putExtra("room_type", roomType);
            intent.putExtra("price", price);

            // ✅ DAGDAG: IPADALA ANG DETALYE NG USER PAPUNTA SA PAYMENT
            if (mAuth.getCurrentUser() != null) {
                // ✅ INAYOS: DATI "user_id", TAMA ITO, PERO TINAWAG NG USERPAYMENT BILANG "user_uid" KANINA
                // ✅ PARA TUMUGMA, Gawin nating "user_uid" para hindi mag-error yung UserPayment.java mo
                intent.putExtra("user_uid", mAuth.getCurrentUser().getUid());
                intent.putExtra("username", mAuth.getCurrentUser().getEmail());
            }

            // 🔴🔴🔴 ANG KULANG DITO KANINA: SIGURADUHIN MAY STARTACTIVITY 🔴🔴🔴
            startActivity(intent);

        });
    }


    // ✅ BAGONG LOGIKA: ITATAGO ANG ORAS KUNG BEDSPACE
    private void setTermsAndSchedule() {
        if ("Transient".equals(roomType)) {
            // 🟡 PARA SA TRANSIENT / ARAWAN -> MAY ORAS
            tvTerms.setText("Terms & Conditions (Transient):\n" +
                    "• Valid ID is required upon arrival.\n" +
                    "• No smoking, drinking, or pets allowed inside the premises.\n" +
                    "• Visitors are only allowed until 8:00 PM.\n" +
                    "• Strictly no parties or loud noises.\n" +
                    "• Cancellation must be done 24 hours before check-in.\n" +
                    "• Rates are good for 1 night stay only.");

            tvCheckInTime.setText("Check-In Time: 2:00 PM - 10:00 PM");
            tvCheckOutTime.setText("Check-Out Time: 11:00 AM (Next Day)");

            // ✅ IPAKITA ANG ORAS
            tvCheckInTime.setVisibility(View.VISIBLE);
            tvCheckOutTime.setVisibility(View.VISIBLE);

        } else {
            // 🟢 PARA SA BEDSPACE / BUWANAN -> WALANG ORAS
            tvTerms.setText("Terms & Conditions (Bedspace):\n" +
                    "• 1 Month Advance Payment is strictly required.\n" +
                    "• Valid ID and Proof of Enrollment / Employment needed.\n" +
                    "• Security Deposit is refundable upon end of contract.\n" +
                    "• 30-day notice is required before moving out.\n" +
                    "• Strict curfew: 10:00 PM on weekdays, 11:59 PM on weekends.\n" +
                    "• No overnight visitors allowed.\n" +
                    "• Monthly rental is due every 1st day of the month.");

            // ✅ ITAGO ANG ORAS NG CHECK-IN AT CHECK-OUT
            tvCheckInTime.setVisibility(View.GONE);
            tvCheckOutTime.setVisibility(View.GONE);
        }
    }


    // ✅ FUNCTION: MAGLAGAY NG BAGONG BOOKING SA FIREBASE
    private void saveBookingToFirebase(String checkInDate, String checkOutDate) {

        // Kunin ang kasalukuyang naka-login na user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: No user logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. BUUIN ANG DATOS NA ILALAGAY
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("user_id", currentUser.getUid());
        bookingData.put("user_email", currentUser.getEmail());
        bookingData.put("property_name", property);
        bookingData.put("location", location);
        bookingData.put("stay_type", roomType);
        bookingData.put("price", price);
        bookingData.put("check_in_date", checkInDate != null ? checkInDate : "N/A");
        bookingData.put("check_out_date", checkOutDate != null ? checkOutDate : "N/A");

        // ✅ Ilalagay lang ang oras kung Transient, kung Bedspace ay N/A
        if ("Transient".equals(roomType)) {
            bookingData.put("check_in_time_schedule", tvCheckInTime.getText().toString());
            bookingData.put("check_out_time_schedule", tvCheckOutTime.getText().toString());
        } else {
            bookingData.put("check_in_time_schedule", "N/A");
            bookingData.put("check_out_time_schedule", "N/A");
        }

        bookingData.put("terms_applied", tvTerms.getText().toString());
        bookingData.put("status", "Pending");
        bookingData.put("booked_at", System.currentTimeMillis());

        // 2. ILAGAY SA COLLECTION NA "bookings"
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BookingActivity.this, "Booking Saved Successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingActivity.this, "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}