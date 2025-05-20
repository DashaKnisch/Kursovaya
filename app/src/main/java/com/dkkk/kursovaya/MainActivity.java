package com.dkkk.kursovaya;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    LinearLayout loginLayout;
    BottomNavigationView bottomNav;
    AdminDatabaseHelper dbHelper;
    SharedPreferences prefs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginLayout = findViewById(R.id.loginLayout);
        bottomNav = findViewById(R.id.bottomNavigation);
        EditText login = findViewById(R.id.editLogin);
        EditText password = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        dbHelper = new AdminDatabaseHelper(this);
        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> {
            String user = login.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (dbHelper.checkAdmin(user, pass)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", user);
                editor.apply();

                loginLayout.setVisibility(View.GONE);
                bottomNav.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MoviesFragment()).commit();
            } else {
                Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_movies) selected = new MoviesFragment();
            else if (item.getItemId() == R.id.nav_sessions) selected = new SessionsFragment();
            else if (item.getItemId() == R.id.nav_profile) selected = new ProfileFragment();
            if (selected != null)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
            return true;
        });

        bottomNav.setVisibility(View.GONE);
    }
}
