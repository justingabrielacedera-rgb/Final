package com.example.finals;

import android.widget.AdapterView;
import android.view.View;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ManageUsersActivity extends AppCompatActivity {

    DatabaseReference usersDbRef;
    DatabaseReference paymentsDbRef;

    ListView listTenants;
    ArrayList<Map<String, String>> tenantList;
    TenantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        listTenants = findViewById(R.id.listTenants);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddTenant);

        usersDbRef = FirebaseDatabase.getInstance().getReference("Users");
        paymentsDbRef = FirebaseDatabase.getInstance().getReference("payments");

        loadTenants();

        fabAdd.setOnClickListener(v -> showAddTenantDialog());

        listTenants.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> selected = tenantList.get(position);
            String userId = selected.get("id");
            if (userId != null && !userId.isEmpty()) {
                showTenantOptions(userId, selected);
            }
        });
    }

    private void loadTenants() {
        tenantList = new ArrayList<>();

        usersDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenantList.clear();

                if (!snapshot.exists()) {
                    showEmptyPlaceholder();
                } else {
                    for (DataSnapshot doc : snapshot.getChildren()) {
                        String role = doc.child("role").getValue(String.class);

                        if ("tenant".equalsIgnoreCase(role)) {
                            Map<String, String> tenant = new HashMap<>();
                            tenant.put("id", doc.getKey());
                            tenant.put("name", doc.child("name").getValue(String.class));
                            tenant.put("username", doc.child("username").getValue(String.class));
                            tenant.put("contact", doc.child("contact").getValue(String.class));
                            tenant.put("room_id", doc.child("room_id").getValue(String.class) != null ? doc.child("room_id").getValue(String.class) : "");
                            tenant.put("status", doc.child("status").getValue(String.class));

                            // ✅ NEW FIELDS FOR DYNAMIC TRACKING RULES
                            tenant.put("tenant_type", doc.child("tenant_type").getValue(String.class) != null ? doc.child("tenant_type").getValue(String.class) : "Bedspace");
                            tenant.put("next_payment_date", doc.child("next_payment_date").getValue(String.class) != null ? doc.child("next_payment_date").getValue(String.class) : "Not Set");
                            tenant.put("days_left", doc.child("days_left").getValue(String.class) != null ? doc.child("days_left").getValue(String.class) : "0");

                            tenantList.add(tenant);
                        }
                    }

                    if (tenantList.isEmpty()) {
                        showEmptyPlaceholder();
                    }
                }

                adapter = new TenantAdapter();
                listTenants.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageUsersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyPlaceholder() {
        Map<String, String> empty = new HashMap<>();
        empty.put("id", "");
        empty.put("name", "No tenants registered yet");
        empty.put("username", "");
        empty.put("contact", "");
        empty.put("room_id", "");
        empty.put("status", "");
        empty.put("tenant_type", "");
        empty.put("next_payment_date", "");
        empty.put("days_left", "");
        tenantList.add(empty);
    }

    private void showAddTenantDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tenant, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etUser = dialogView.findViewById(R.id.etUsername);
        EditText etPass = dialogView.findViewById(R.id.etPassword);
        EditText etContact = dialogView.findViewById(R.id.etContact);
        EditText etRoomId = dialogView.findViewById(R.id.etRoomId);
        Spinner spnStatus = dialogView.findViewById(R.id.spnStatus);

        // Make sure you add these fields to dialog_add_tenant.xml
        Spinner spnTenantType = dialogView.findViewById(R.id.spnTenantType);
        EditText etNextPayment = dialogView.findViewById(R.id.etNextPayment);
        EditText etDaysLeft = dialogView.findViewById(R.id.etDaysLeft);

        // Simple visibility helper toggle based on what's picked
        spnTenantType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String selected = spnTenantType.getSelectedItem().toString();
                if (selected.equals("Transient")) {
                    etDaysLeft.setVisibility(View.VISIBLE);
                    etNextPayment.setVisibility(View.GONE);
                } else {
                    etDaysLeft.setVisibility(View.GONE);
                    etNextPayment.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Add New Tenant Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String user = etUser.getText().toString().trim();
                    String pass = etPass.getText().toString().trim();
                    String contact = etContact.getText().toString().trim();
                    String room = etRoomId.getText().toString().trim();
                    String status = spnStatus.getSelectedItem().toString();
                    String type = spnTenantType.getSelectedItem().toString();

                    if (name.isEmpty() || user.isEmpty() || pass.isEmpty() || contact.isEmpty()) {
                        Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> tenant = new HashMap<>();
                    tenant.put("name", name);
                    tenant.put("username", user);
                    tenant.put("password", pass);
                    tenant.put("contact", contact);
                    tenant.put("room_id", room);
                    tenant.put("status", status);
                    tenant.put("role", "tenant");
                    tenant.put("tenant_type", type);

                    // Conditional value gathering based on selection parameters
                    if (type.equals("Transient")) {
                        tenant.put("days_left", etDaysLeft.getText().toString().trim());
                        tenant.put("next_payment_date", "");
                    } else {
                        tenant.put("days_left", "");
                        tenant.put("next_payment_date", etNextPayment.getText().toString().trim());
                    }

                    String newUserId = usersDbRef.push().getKey();
                    if (newUserId != null) {
                        usersDbRef.child(newUserId).setValue(tenant)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Tenant Created! ✅", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showTenantOptions(String userId, Map<String, String> data) {
        String[] options = {"Change Status", "View Payments", "Delete Tenant"};
        new AlertDialog.Builder(this)
                .setTitle("Tenant Options: " + data.get("name"))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        String newStatus = "Active".equals(data.get("status")) ? "Inactive" : "Active";
                        usersDbRef.child(userId).child("status").setValue(newStatus);
                    } else if (which == 1) {
                        viewTenantPayments(data.get("username"));
                    } else if (which == 2) {
                        usersDbRef.child(userId).removeValue();
                    }
                }).show();
    }

    private void viewTenantPayments(String tenantUsername) {
        paymentsDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0; double total = 0;
                for (DataSnapshot paymentDoc : snapshot.getChildren()) {
                    String pUser = paymentDoc.child("username").getValue(String.class);
                    if (tenantUsername != null && tenantUsername.equalsIgnoreCase(pUser)) {
                        count++;
                        Object amtObj = paymentDoc.child("amount").getValue();
                        if (amtObj != null) total += Double.parseDouble(String.valueOf(amtObj));
                    }
                }
                new AlertDialog.Builder(ManageUsersActivity.this)
                        .setTitle("Payment Summary")
                        .setMessage("Total Payments: " + count + "\nTotal Amount Received: ₱" + total)
                        .setPositiveButton("OK", null).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ✅ PREMIUM LIST VISUAL OVERRIDE IMPLEMENTING CONDITIONAL METRICS
    private class TenantAdapter extends BaseAdapter {
        @Override public int getCount() { return tenantList.size(); }
        @Override public Object getItem(int position) { return tenantList.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ManageUsersActivity.this).inflate(R.layout.item_tenant, parent, false);
            }

            Map<String, String> t = tenantList.get(position);

            TextView tvName = convertView.findViewById(R.id.tvTenantName);
            TextView tvUser = convertView.findViewById(R.id.tvUsername);
            TextView tvContact = convertView.findViewById(R.id.tvContact);
            TextView tvRoom = convertView.findViewById(R.id.tvRoom);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            View dot = convertView.findViewById(R.id.statusDot);

            // Fetch custom fields
            String type = t.get("tenant_type");
            String nextPayment = t.get("next_payment_date");
            String daysLeft = t.get("days_left");

            tvName.setText(t.get("name"));
            tvUser.setText(t.get("username") != null && !t.get("username").isEmpty() ? "@" + t.get("username") + " (" + type + ")" : "");
            tvContact.setText(t.get("contact"));

            // ✅ CRITICAL LOGIC: If Bedspace show Next Payment | If Transient show Remaining Stay Days
            if ("Transient".equalsIgnoreCase(type)) {
                tvRoom.setText("Room: " + t.get("room_id") + " • 🗓️ Stay Left: " + daysLeft + " Days");
            } else {
                tvRoom.setText("Room: " + t.get("room_id") + " • 💳 Next Due: " + nextPayment);
            }

            android.graphics.drawable.GradientDrawable statusCircle = new android.graphics.drawable.GradientDrawable();
            statusCircle.setShape(android.graphics.drawable.GradientDrawable.OVAL);

            if (t.get("status") != null && !t.get("status").isEmpty()) {
                dot.setVisibility(View.VISIBLE);
                if (t.get("status").equalsIgnoreCase("Active")) {
                    tvStatus.setText("ACTIVE");
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#38A169"));
                    statusCircle.setColor(android.graphics.Color.parseColor("#38A169"));
                    dot.setBackground(statusCircle);
                } else {
                    tvStatus.setText("INACTIVE");
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#E53E3E"));
                    statusCircle.setColor(android.graphics.Color.parseColor("#E53E3E"));
                    dot.setBackground(statusCircle);
                }
            } else {
                tvStatus.setText("");
                dot.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}