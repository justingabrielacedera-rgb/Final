package com.example.finals;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TenantsProfileActivity extends AppCompatActivity {

    EditText etFullname, etUsername, etAge, etGender, etContact, etEmail;
    EditText etCurrentPass, etNewPass, etConfirmPass;
    ImageView ivProfilePhoto;
    Button btnSave, btnCancel, btnChangePhoto;

    // Firebase
    DatabaseReference userRef;
    StorageReference storageRef;
    FirebaseUser firebaseUser;
    String userUID;
    String username; // ✅ Declared globally to fix syntax errors
    Uri selectedImageUri;
    ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenants_profile);

        // Fetch intent data passed from MainActivity2
        username = getIntent().getStringExtra("username");

        // Initialize Views
        etFullname = findViewById(R.id.etFullname);
        etUsername = findViewById(R.id.etUsername);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);

        etCurrentPass = findViewById(R.id.etCurrentPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirmPass = findViewById(R.id.etConfirmPass);

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Get Current Logged-in Firebase User
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userUID = firebaseUser.getUid();

        // ✅ TABLE NAME FIXED: Points to "Tenants" table node instead of "Users"
        userRef = FirebaseDatabase.getInstance().getReference("Tenants").child(userUID);
        storageRef = FirebaseStorage.getInstance().getReference("profile_photos");

        Toast.makeText(this, "Connected to Tenants Table", Toast.LENGTH_SHORT).show();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");

        // Load data from Firebase
        loadUserProfile();

        // Change Photo Action
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        // Save Profiles Action
        btnSave.setOnClickListener(v -> {
            if (validateAllInputs()) {
                updateUserProfile();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfilePhoto.setImageURI(selectedImageUri);
        }
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullname = snapshot.child("fullname").getValue(String.class);
                    String usernameDisplay = firebaseUser.getEmail();
                    String age = snapshot.child("age").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String photoUrl = snapshot.child("profilePhotoUrl").getValue(String.class);

                    etFullname.setText(fullname != null ? fullname : "");
                    etUsername.setText(usernameDisplay);
                    etAge.setText(age != null ? age : "");
                    etGender.setText(gender != null ? gender : "");
                    etContact.setText(contact != null ? contact : "");
                    etEmail.setText(email != null ? email : "");
                    etUsername.setEnabled(false);

                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(TenantsProfileActivity.this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_profile)
                                .centerCrop()
                                .into(ivProfilePhoto);
                    } else {
                        ivProfilePhoto.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    // Creating an empty structural baseline if profile node is empty
                    etUsername.setText(firebaseUser.getEmail());
                    etEmail.setText(firebaseUser.getEmail());
                    etUsername.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TenantsProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateAllInputs() {
        String fullname = etFullname.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (fullname.isEmpty()) {
            etFullname.setError("Full name required");
            etFullname.requestFocus();
            return false;
        }
        if (ageStr.isEmpty()) {
            etAge.setError("Age required");
            etAge.requestFocus();
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age < 12 || age > 100) {
                etAge.setError("Enter valid age (12-100)");
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Age must be a number");
            etAge.requestFocus();
            return false;
        }
        if (gender.isEmpty()) {
            etGender.setError("Gender required");
            etGender.requestFocus();
            return false;
        }
        if (contact.isEmpty() || contact.length() != 11 || !contact.matches("\\d+")) {
            etContact.setError("Enter valid 11-digit contact");
            etContact.requestFocus();
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email address");
            etEmail.requestFocus();
            return false;
        }

        String currentPass = etCurrentPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        if (!currentPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
            if (currentPass.isEmpty()) {
                etCurrentPass.setError("Enter current password");
                etCurrentPass.requestFocus();
                return false;
            }
            if (newPass.isEmpty()) {
                etNewPass.setError("Enter new password");
                etNewPass.requestFocus();
                return false;
            }
            if (newPass.length() < 6) {
                etNewPass.setError("Password must be at least 6 characters");
                etNewPass.requestFocus();
                return false;
            }
            if (confirmPass.isEmpty()) {
                etConfirmPass.setError("Confirm new password");
                etConfirmPass.requestFocus();
                return false;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPass.setError("New passwords do not match");
                etConfirmPass.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void updateUserProfile() {
        progressDialog.show();

        if (selectedImageUri != null) {
            String photoName = UUID.randomUUID().toString();
            StorageReference photoRef = storageRef.child(photoName);

            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> downloadUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                        downloadUrlTask.addOnSuccessListener(uri -> {
                            String photoUrl = uri.toString();
                            saveAllDataToDatabase(photoUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(TenantsProfileActivity.this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveAllDataToDatabase(null);
        }
    }

    private void saveAllDataToDatabase(String newPhotoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullname", etFullname.getText().toString().trim());
        updates.put("age", etAge.getText().toString().trim());
        updates.put("gender", etGender.getText().toString().trim());
        updates.put("contact", etContact.getText().toString().trim());
        updates.put("email", etEmail.getText().toString().trim());

        if (!etNewPass.getText().toString().trim().isEmpty()) {
            updates.put("password", etNewPass.getText().toString().trim());
            firebaseUser.updatePassword(etNewPass.getText().toString().trim());
        }

        if (newPhotoUrl != null) {
            updates.put("profilePhotoUrl", newPhotoUrl);
        }

        // Saves under the newly defined path root "Tenants"
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(TenantsProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(TenantsProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}