package com.dkkk.kursovaya;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    LinearLayout loginLayout;
    BottomNavigationView bottomNav;
    EditText login;
    EditText password;
    Button btnLogin;

    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginLayout = findViewById(R.id.loginLayout);
        bottomNav = findViewById(R.id.bottomNavigation);
        login = findViewById(R.id.editLogin);
        password = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        bottomNav.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String user = login.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            checkAdminInFirestore(user, pass);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_movies) selected = new MoviesFragment();
            else if (item.getItemId() == R.id.nav_sessions) selected = new SessionsFragment();
            else if (item.getItemId() == R.id.nav_profile) selected = new ProfileFragment();
            else if (item.getItemId() == R.id.nav_sales) selected = new SalesFragment();
            if (selected != null)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
            return true;
        });
    }

    private void checkAdminInFirestore(String user, String pass) {
        // Коллекция "admins"
        db.collection("admins")
                .whereEqualTo("login", user)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean found = false;
                        for (QueryDocumentSnapshot document : snapshots) {
                            String passwordFromDb = document.getString("password");
                            if (passwordFromDb != null && passwordFromDb.equals(pass)) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            Toast.makeText(MainActivity.this, "Вход успешный", Toast.LENGTH_SHORT).show();
                            loginLayout.setVisibility(View.GONE);
                            bottomNav.setVisibility(View.VISIBLE);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MoviesFragment()).commit();
                        } else {
                            Toast.makeText(MainActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
