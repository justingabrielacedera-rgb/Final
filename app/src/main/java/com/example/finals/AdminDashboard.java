package com.example.finals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboard extends AppCompatActivity {

    // ✅ EKSAKTONG PANGALAN NG MGA ID MO
    CardView cardViewPayments, cardReports, cardAnnouncements, cardManageRooms, cardManageUsers, cardMaintenance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ SIGURADUHIN NA MAKAKAPASOK KAHIT MAY ANONG ERROR
        try {
            setContentView(R.layout.activity_admin_dashboard);
            Toast.makeText(this, "✅ NAKAPASOK SA ADMIN PANEL!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ ERROR SA LAYOUT: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ✅ KUNIN ANG MGA BUTTON / CARD
        initViews();

        // ✅ LAGYAN NG GAGAWIN KAPAG PININDOT
        setupClicks();
    }

    // ✅ HANAPIN ANG MGA ELEMENTO
    private void initViews() {
        cardViewPayments = findViewById(R.id.cardViewPayments);
        cardReports = findViewById(R.id.cardReports);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardMaintenance = findViewById(R.id.cardMaintenance);
    }

    // ✅ GAGANA ANG PAG PINDOT
    private void setupClicks() {
        cardViewPayments.setOnClickListener(v -> {
            // ✅ Opens the announcement posting screen
            Intent intent = new Intent(this, PaymentHistoryActivity .class);
            startActivity(intent);
        });

        cardReports.setOnClickListener(v ->
                Toast.makeText(this, "Reports Clicked", Toast.LENGTH_SHORT).show()
        );

        cardAnnouncements.setOnClickListener(v -> {
            // ✅ Opens the announcement posting screen
            Intent intent = new Intent(this, PostAnnouncementActivity.class);
            startActivity(intent);
        });

        cardManageRooms.setOnClickListener(v -> {
            // ✅ Opens the announcement posting screen
            Intent intent = new Intent(this, ManageRoomsActivity.class);
            startActivity(intent);
        });

        cardManageUsers.setOnClickListener(v -> {
            // ✅ Opens the announcement posting screen
            Intent intent = new Intent(this, ManageUsersActivity.class);
            startActivity(intent);
        });

        cardMaintenance.setOnClickListener(v ->
                Toast.makeText(this, "Maintenance Clicked", Toast.LENGTH_SHORT).show()
        );
    }
}