package com.example.finals;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {
    TextView txtReceipt;
    Button btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Initialize views
        txtReceipt = findViewById(R.id.txtReceipt);
        btnHome = findViewById(R.id.btnHome);

        // ✅ GET ALL DATA PASSED FROM USER PAYMENT (Firebase / Google Login Data)
        Intent i = getIntent();

        // User Info (From Google Login)
        String fullname       = getIntentExtra(i, "name", "Google User");
        String emailUser      = getIntentExtra(i, "name", "N/A"); // We use Email as Name/Username
        String contact        = "N/A"; // Google Sign-In does NOT give contact number automatically

        // Booking / Property Info
        String property       = getIntentExtra(i, "property", "N/A");
        String location       = getIntentExtra(i, "location", "N/A");
        String roomType       = getIntentExtra(i, "room_type", "N/A");
        String checkinDate    = getIntentExtra(i, "checkin_date", "N/A"); // Add this if you pass it
        String checkoutDate   = getIntentExtra(i, "checkout_date", "N/A"); // Add this if you pass it

        // Payment Info
        int amountInt         = i.getIntExtra("amount", 0); // Comes as Integer from UserPayment
        double amount         = (double) amountInt; // Convert to double for formatting
        String status         = getIntentExtra(i, "status", "Paid");
        String paymentMethod  = getIntentExtra(i, "payment_method", "Cash");
        String referenceNo    = getIntentExtra(i, "reference_number", "N/A");

        // Get current date/time for receipt issuance
        String currentDate = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
                .format(new Date());

        // ✅ BUILD FORMATTED RECEIPT TEXT (Clean & Professional)
        String receiptText =
                "               RECEIPT\n" +
                        "Issued On : " + currentDate + "\n\n" +

                        "INFORMATION\n" +
                        "Full Name : " + fullname + "\n" +
                        "Email    : " + emailUser + "\n" +
                        "Contact   : " + contact + "\n\n" +

                        "BOOKING DETAILS\n" +
                        "Property  : " + property + "\n" +
                        "Location  : " + location + "\n" +
                        "Room Type : " + roomType + "\n" +
                        "Check-in  : " + checkinDate + "\n" +
                        "Check-out : " + checkoutDate + "\n\n" +

                        "PAYMENT DETAILS\n" +
                        "Amount    : ₱ " + String.format("%,.2f", amount) + "\n" +
                        "Status    : " + status + "\n" +
                        "Method    : " + paymentMethod + "\n" +
                        "Ref No.   : " + referenceNo + "\n\n" +

                        "Transaction Complete ✅\n" +
                        "Thank you for booking with us!\n" +
                        "We hope you have a great stay.";

        // Show receipt
        txtReceipt.setText(receiptText);

        // ✅ Home button action — clear history so user can't go back
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ✅ Helper method to safely get string extras
    private String getIntentExtra(Intent intent, String key, String defaultValue) {
        String value = intent.getStringExtra(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    // ✅ Handle back button same as home button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}