package com.example.finals;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DormDetailsActivity extends AppCompatActivity {

    ImageView imgRoom;
    TextView txtRoomName, txtPrice, txtType, txtDetails, txtLocation, txtAvailability;
    EditText etCheckIn, etCheckOut;
    Button btnBookNow;

    RadioGroup radioStayType;
    RadioButton rbTransient, rbBedspace;
    CheckBox cbAdvancePay;

    String property = "";
    String location = "";

    FirebaseFirestore db;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    int availableSlots = 0;
    String finalPrice = "";

    // ✅ DITO NAKA-SAVE ANG MGA KWARTO PARA SIGURADONG MERON
    int totalCapacity_Transient = 0;
    int totalCapacity_Bedspace = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dorm_details);

        db = FirebaseFirestore.getInstance();

        imgRoom = findViewById(R.id.imgRoom);
        txtRoomName = findViewById(R.id.txtRoomName);
        txtPrice = findViewById(R.id.txtPrice);
        txtType = findViewById(R.id.txtType);
        txtDetails = findViewById(R.id.txtDetails);
        txtLocation = findViewById(R.id.txtLocation);
        txtAvailability = findViewById(R.id.txtAvailability);
        etCheckIn = findViewById(R.id.etCheckIn);
        etCheckOut = findViewById(R.id.etCheckOut);
        btnBookNow = findViewById(R.id.btnBookNow);

        radioStayType = findViewById(R.id.radioStayType);
        rbTransient = findViewById(R.id.rbTransient);
        rbBedspace = findViewById(R.id.rbBedspace);
        cbAdvancePay = findViewById(R.id.cbAdvancePay);

        Intent intent = getIntent();
        property = intent.getStringExtra("property");
        location = intent.getStringExtra("location");
        int imageRes = intent.getIntExtra("image", R.drawable.bedspace);

        // ✅ AYUSIN ANG PANGALAN PARA TUMPAK
        if (property.equalsIgnoreCase("The Dormitory")) {
            property = "The Dormitory"; // Siguradong tama ang spelling
        }

        txtRoomName.setText(property);
        txtLocation.setText(location);
        imgRoom.setImageResource(imageRes);

        // ✅ ITAKDA ANG KAKAYAHAN NG BAWAT DORM (PARA MERON AGAD)
        setDefaultCapacities();

        // Default view
        rbTransient.setChecked(true);
        updateUIAndData("Transient");

        // Date Pickers
        etCheckIn.setOnClickListener(v -> showDatePicker(etCheckIn, true));
        etCheckOut.setOnClickListener(v -> showDatePicker(etCheckOut, false));

        // Switch Type
        radioStayType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTransient) {
                updateUIAndData("Transient");
                cbAdvancePay.setVisibility(View.GONE);
                etCheckIn.setVisibility(View.VISIBLE);
                etCheckOut.setVisibility(View.VISIBLE);
            } else {
                updateUIAndData("Bedspace");
                cbAdvancePay.setVisibility(View.VISIBLE);
                etCheckIn.setVisibility(View.GONE);
                etCheckOut.setVisibility(View.GONE);
                etCheckIn.setText("");
                etCheckOut.setText("");
            }
        });

        // BOOK BUTTON
        btnBookNow.setOnClickListener(v -> {
            String type = rbTransient.isChecked() ? "Transient" : "Bedspace";

            if ("Bedspace".equals(type) && !cbAdvancePay.isChecked()) {
                Toast.makeText(this, "Kailangan mong sumang-ayon sa advance payment!", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("Transient".equals(type) && (etCheckIn.getText().toString().isEmpty() || etCheckOut.getText().toString().isEmpty())) {
                Toast.makeText(this, "Pumili ng petsa ng Check-in at Check-out!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (availableSlots <= 0) {
                Toast.makeText(this, "Puno na po ang kwarto, pasensya na!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(DormDetailsActivity.this, BookingActivity.class);
            i.putExtra("property", property);
            i.putExtra("location", location);
            i.putExtra("room_type", type);
            i.putExtra("price", finalPrice);
            i.putExtra("details", txtDetails.getText().toString());
            i.putExtra("checkin_date", etCheckIn.getText().toString());
            i.putExtra("checkout_date", etCheckOut.getText().toString());
            startActivity(i);
        });
    }


    // ✅ DITO NAMAN ANG BAGONG SISTEMA: BABASAHIN ANG DEFAULT NA HALAGA KUNG WALANG MAKITANG DATA SA FIREBASE
    private void updateUIAndData(String type) {
        db.collection("rooms")
                .whereEqualTo("property", property)
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int totalCap = 0;
                    int totalOcc = 0;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Long cap = doc.getLong("capacity");
                            Long occ = doc.getLong("occupied");
                            if (cap != null) totalCap += cap;
                            if (occ != null) totalOcc += occ;
                        }
                    } else {
                        // ✅ KUNG WALANG LAMAN ANG FIREBASE, GAMITIN ANG DEFAULT NA NAKASET NATIN
                        if (type.equals("Transient")) {
                            totalCap = totalCapacity_Transient;
                        } else {
                            totalCap = totalCapacity_Bedspace;
                        }
                        totalOcc = 0; // Walang nakatira
                    }

                    availableSlots = totalCap - totalOcc;

                    // DISPLAY STATUS
                    if (availableSlots > 0) {
                        txtAvailability.setText("AVAILABLE (" + availableSlots + " slots left)");
                        txtAvailability.setTextColor(0xFF2E7D32);
                        btnBookNow.setEnabled(true);
                    } else {
                        txtAvailability.setText("FULLY BOOKED");
                        txtAvailability.setTextColor(0xFFD32F2F);
                        btnBookNow.setEnabled(false);
                    }

                    // ILAGAY ANG DETALYE
                    setRoomInfo(type);
                })
                .addOnFailureListener(e -> {
                    // KUNG MAY ERROR SA INTERNET O FIREBASE, GAMITIN ANG LOCAL DATA
                    int totalCap = (type.equals("Transient")) ? totalCapacity_Transient : totalCapacity_Bedspace;
                    availableSlots = totalCap;
                    txtAvailability.setText("AVAILABLE (" + availableSlots + " slots left)");
                    txtAvailability.setTextColor(0xFF2E7D32);
                    btnBookNow.setEnabled(true);
                    setRoomInfo(type);
                });
    }


    // ✅ ITO ANG DEPENISYON NG KAKAYAHAN NG BAWAT DORM
    private void setDefaultCapacities() {
        switch (property) {
            case "SD Dorm 2":
                totalCapacity_Transient = 4;  // 4 na kwarto
                totalCapacity_Bedspace = 16;  // 4 na kwarto x 4 tao
                break;
            case "Palar Dormitory":
                totalCapacity_Transient = 2;
                totalCapacity_Bedspace = 28;
                break;
            case "Milflores Boarding House":
                totalCapacity_Transient = 0; // Walang transient
                totalCapacity_Bedspace = 14;
                break;
            case "Loft 22":
                totalCapacity_Transient = 3;
                totalCapacity_Bedspace = 0; // Walang bedspace
                break;
            case "The Dormitory": // ✅ ITO YUNG NASA PICTURE MO
                totalCapacity_Transient = 2; // 2 kama
                totalCapacity_Bedspace = 4; // 4 na tao
                break;
            default:
                totalCapacity_Transient = 2;
                totalCapacity_Bedspace = 4;
                break;
        }
    }


    // ✅ DETALYE AT PRESYO
    private void setRoomInfo(String type) {
        switch (property) {
            case "SD Dorm 2":
                if ("Transient".equals(type)) {
                    txtType.setText("Transient Stay");
                    txtPrice.setText("₱550 / night");
                    txtDetails.setText("Daily stay • Electric fan only • Check-in required");
                    finalPrice = "550";
                } else {
                    txtType.setText("Bedspace");
                    txtPrice.setText("₱3,300 / month");
                    txtDetails.setText("Long-term • Shared room • No check-in needed");
                    finalPrice = "3300";
                }
                break;

            case "Palar Dormitory":
                if ("Transient".equals(type)) {
                    txtType.setText("Transient Stay");
                    txtPrice.setText("₱600 / night");
                    txtDetails.setText("Daily stay • Electric fan • Spacious room • Check-in required");
                    finalPrice = "600";
                } else {
                    txtType.setText("Bedspace");
                    txtPrice.setText("₱3,500 / month");
                    txtDetails.setText("Long-term • Shared room • Near university • No check-in needed");
                    finalPrice = "3500";
                }
                break;

            case "Milflores Boarding House":
                if ("Transient".equals(type)) {
                    txtType.setText("Transient Stay");
                    txtPrice.setText("₱500 / night");
                    txtDetails.setText("Daily stay • Electric fan • Quiet area • Check-in required");
                    finalPrice = "500";
                } else {
                    txtType.setText("Bedspace");
                    txtPrice.setText("₱3,000 / month");
                    txtDetails.setText("Long-term • Shared room • Affordable • No check-in needed");
                    finalPrice = "3000";
                }
                break;

            case "Loft 22":
                if ("Transient".equals(type)) {
                    txtType.setText("Transient Stay");
                    txtPrice.setText("₱700 / night");
                    txtDetails.setText("Daily stay • Aircon • Modern room • Check-in required");
                    finalPrice = "700";
                } else {
                    txtType.setText("Bedspace");
                    txtPrice.setText("₱4,000 / month");
                    txtDetails.setText("Long-term • Private room • City view • No check-in needed");
                    finalPrice = "4000";
                }
                break;

            // ✅ TAMA NA TAMA ITO PARA SA "THE DORMITORY"
            case "The Dormitory":
                if ("Transient".equals(type)) {
                    txtType.setText("Transient Stay");
                    txtPrice.setText("₱650 / night");
                    txtDetails.setText("Daily stay • Aircon & Fan • Cozy room • Check-in required");
                    finalPrice = "650";
                } else {
                    txtType.setText("Bedspace");
                    txtPrice.setText("₱3,800 / month");
                    txtDetails.setText("Long-term • Shared room • Good location • No check-in needed");
                    finalPrice = "3800";
                }
                break;
        }
    }


    // ✅ DATE PICKER
    private void showDatePicker(EditText editText, boolean isCheckIn) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                    if (isCheckIn) {
                        calendar.add(Calendar.DATE, 1);
                        etCheckOut.setText(dateFormat.format(calendar.getTime()));
                        calendar.add(Calendar.DATE, -1);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}