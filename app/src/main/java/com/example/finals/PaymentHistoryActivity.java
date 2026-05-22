package com.example.finals;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class PaymentHistoryActivity extends AppCompatActivity {

    ListView listPayments;
    FirebaseFirestore db;
    ArrayList<String> paymentList;
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        listPayments = findViewById(R.id.listPayments);
        db = FirebaseFirestore.getInstance();

        // Get username passed from previous screen
        username = getIntent().getStringExtra("username");
        paymentList = new ArrayList<>();

        loadPaymentsFromFirestore();
    }

    private void loadPaymentsFromFirestore() {
        db.collection("payments")
                .whereEqualTo("username", username)
                .orderBy("payment_id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    paymentList.clear();

                    if (querySnapshot.isEmpty()) {
                        paymentList.add("❌ No payment history found for this user.");
                    } else {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            // Get fields (matches your old column names)
                            String amount = doc.getString("amount");
                            String method = doc.getString("method");
                            String date = doc.getString("date");
                            String status = doc.getString("status");

                            // Format exactly like before
                            String paymentItem =
                                    "💰 ₱" + amount +
                                            "\nMethod: " + method +
                                            "\nDate: " + date +
                                            "\nStatus: " + status;

                            paymentList.add(paymentItem);
                        }
                    }

                    // Update adapter
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1,
                                    paymentList);
                    listPayments.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    paymentList.clear();
                    paymentList.add("⚠️ Error loading payment history.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1,
                                    paymentList);
                    listPayments.setAdapter(adapter);
                });
    }
}