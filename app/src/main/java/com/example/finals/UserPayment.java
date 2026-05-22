package com.example.finals;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserPayment extends AppCompatActivity {

    Spinner spnMethod;
    EditText etAmount, etRefNumber;
    ImageView ivProof;
    Button btnSubmit;

    String selectedMethod = "Cash";
    Uri imageUri;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUsername = "";

    String property, location, roomType, priceFromBooking;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_payment);

        // --- Firebase init ---
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUsername = currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUid();
        }

        // --- Views ---
        spnMethod   = findViewById(R.id.spnMethod);
        etAmount    = findViewById(R.id.etAmount);
        etRefNumber = findViewById(R.id.etRefNumber);
        ivProof     = findViewById(R.id.ivProof);
        btnSubmit   = findViewById(R.id.btnSubmit);

        // --- Intent extras ---
        Intent i = getIntent();
        property         = i.getStringExtra("property");
        location         = i.getStringExtra("location");
        roomType         = i.getStringExtra("room_type");
        priceFromBooking = i.getStringExtra("price");

        // --- Spinner / Dropdown setup ---
        String[] methods = { "Cash", "GCash" };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                methods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMethod.setAdapter(adapter);

        spnMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMethod = methods[position];

                boolean isGcash = selectedMethod.equals("GCash");
                etRefNumber.setVisibility(isGcash ? View.VISIBLE : View.GONE);
                findViewById(R.id.btnUpload).setVisibility(isGcash ? View.VISIBLE : View.GONE);
                ivProof.setVisibility(isGcash && imageUri != null ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // --- Buttons ---
        findViewById(R.id.btnUpload).setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> submitPayment());

        if (priceFromBooking != null) {
            etAmount.setText(priceFromBooking);
            etAmount.setEnabled(false);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProof.setImageURI(imageUri);
            ivProof.setVisibility(View.VISIBLE);
        }
    }

    private void submitPayment() {
        String amount = etAmount.getText().toString().trim();
        String ref    = etRefNumber.getText().toString().trim();

        if (amount.isEmpty()) {
            etAmount.setError("Ilagay ang halaga");
            return;
        }
        if (selectedMethod.equals("GCash") && ref.isEmpty()) {
            etRefNumber.setError("Ilagay ang Reference / Transaction ID");
            return;
        }
        if (selectedMethod.equals("GCash") && imageUri == null) {
            Toast.makeText(this, "Mag-upload ng proof of payment (screenshot)", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Nagse-save...");

        String dateNow = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(new Date());

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("username",       currentUsername);
        paymentData.put("amount",         amount);
        paymentData.put("payment_method", selectedMethod);
        paymentData.put("reference",      ref);
        paymentData.put("image_uri",      imageUri != null ? imageUri.toString() : "");
        paymentData.put("status",         "Paid");
        paymentData.put("payment_date",   dateNow);
        paymentData.put("property",       property != null ? property : "");
        paymentData.put("location",       location != null ? location : "");
        paymentData.put("room_type",      roomType != null ? roomType : "");

        db.collection("payments")
                .add(paymentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Payment Successful ✅ Opening Receipt...", Toast.LENGTH_LONG).show();

                    int amountInt = 0;
                    try {
                        amountInt = Integer.parseInt(amount.replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        amountInt = 0;
                    }

                    Intent receiptIntent = new Intent(UserPayment.this, ReceiptActivity.class);
                    receiptIntent.putExtra("name",           currentUsername);
                    receiptIntent.putExtra("property",       property != null ? property : "N/A");
                    receiptIntent.putExtra("location",       location != null ? location : "N/A");
                    receiptIntent.putExtra("room_type",      roomType != null ? roomType : "N/A");
                    receiptIntent.putExtra("amount",         amountInt);
                    receiptIntent.putExtra("status",         "Paid");
                    receiptIntent.putExtra("payment_method", selectedMethod);

                    startActivity(receiptIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Payment");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}