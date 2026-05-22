package com.example.finals;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

    CardView cardSD, cardLoft, cardPalar, cardMilflores;
    TextView txtWelcome;

    // ✅ TINANGGAL NA ANG SQLite, FIREBASE NA LANG
    // SQLiteDatabase db; <-- BINURA NA
    String username; // Ito ay email na galing sa login
    String fullName; // Ito ay pangalan galing sa Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        // ✅ KUNIN ANG DATA GALING LOGIN
        username = getIntent().getStringExtra("username"); // Ito ang Email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fullName = user.getDisplayName(); // Ito ang Pangalan galing Google
        }

        // ✅ INITIALIZE VIEWS
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigationView = findViewById(R.id.bottomNav);
        toolbar = findViewById(R.id.toolbar);
        txtWelcome = findViewById(R.id.txtWelcome);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Find Your Dorm");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open, R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // ✅ Bottom Navigation Click Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_message) {
                Intent intent = new Intent(this, Message.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, TenantsProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // ✅ TINANGGAL NA ANG SQLITE QUERY, GANITO NA LANG ANG PAGBATI
        showUser();

        // ✅ DORM CARDS - NANDITO PA RIN AT GUMAGANA
        cardSD = findViewById(R.id.cardSD);
        cardLoft = findViewById(R.id.cardLoft);
        cardPalar = findViewById(R.id.cardPalar);
        cardMilflores = findViewById(R.id.cardMilflores);

        cardSD.setOnClickListener(v -> openDorm(
                "SD Dorm 2",
                "19 Blueberry ext., St. Aranai Village, Brgy. Ususan City",
                R.drawable.bedspace,
                "Bedspace • ₱2,500/month",
                "4.8"
        ));

        cardLoft.setOnClickListener(v -> openDorm(
                "Loft 22",
                "15 Blue Falcon St., Rizal Taguig City",
                R.drawable.trans,
                "Transient • Aircon • ₱500/night",
                "4.9"
        ));

        cardPalar.setOnClickListener(v -> openDorm(
                "The Dormitory",
                "C2 Mt.Apo St., Palar Village, Brgy.Pinagsama Taguig City",
                R.drawable.bedspace1,
                "Bedspace • ₱2,200/month",
                "4.7"
        ));

        cardMilflores.setOnClickListener(v -> openDorm(
                "Milflores Boarding House",
                "B70 L30 Milflores St., Brgy. Taguig City",
                R.drawable.bedspace2,
                "Bedspace • ₱2,300/month",
                "4.6"
        ));
    }

    private void openDorm(String name, String address, int image, String details, String rating) {
        Intent intent = new Intent(this, DormDetailsActivity.class);
        intent.putExtra("property", name);
        intent.putExtra("location", address);
        intent.putExtra("image", image);
        intent.putExtra("details", details);
        intent.putExtra("rating", rating);
        startActivity(intent);
    }

    // ✅ BINAGO KO ITO: Kukunin na lang natin ang pangalan mula sa Google
    private void showUser() {
        if (fullName != null && !fullName.isEmpty()) {
            txtWelcome.setText("Hi, " + fullName + "!");
        } else if (username != null && !username.isEmpty()) {
            txtWelcome.setText("Hi, " + username + "!"); // Kung walang pangalan, email na lang
        } else {
            txtWelcome.setText("Hello!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity2.class));
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, TenantsProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_notif) {
            startActivity(new Intent(this, NotificationActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut(); // ✅ Mag-logout din sa Firebase
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawers();
        return true;
    }
}