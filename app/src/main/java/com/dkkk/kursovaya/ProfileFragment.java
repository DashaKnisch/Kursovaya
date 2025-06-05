package com.dkkk.kursovaya;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore firestore;
    private CollectionReference adminsCollection;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView textUser = view.findViewById(R.id.textUser);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        MaterialButton btnAddAdmin = view.findViewById(R.id.btnAddAdmin);
        MaterialButton btnDeleteAdmin = view.findViewById(R.id.btnDeleteAdmin);

        SharedPreferences prefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "неизвестный");

        textUser.setText("Вы вошли как: " + username);

        firestore = FirebaseFirestore.getInstance();
        adminsCollection = firestore.collection("admins");

        btnLogout.setOnClickListener(v -> {
            requireActivity().finish();
            startActivity(requireActivity().getIntent());
        });

        btnAddAdmin.setOnClickListener(v -> showAddAdminDialog());

        btnDeleteAdmin.setOnClickListener(v -> showDeleteAdminDialog());

        return view;
    }

    private void showAddAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить администратора");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText inputLogin = new EditText(requireContext());
        inputLogin.setHint("Логин");
        inputLogin.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(inputLogin);

        EditText inputPassword = new EditText(requireContext());
        inputPassword.setHint("Пароль");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton("+", (dialog, which) -> {
            String login = inputLogin.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // Добавляем или обновляем документ с ID = login
            adminsCollection.document(login)
                    .set(new Admin(login, password))
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(requireContext(), "Админ добавлен", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Удалить администратора");

        EditText inputLogin = new EditText(requireContext());
        inputLogin.setHint("Введите логин");
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        inputLogin.setPadding(padding, padding, padding, padding);
        inputLogin.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(inputLogin);

        builder.setPositiveButton("-", (dialog, which) -> {
            String login = inputLogin.getText().toString().trim();

            if (login.isEmpty()) {
                Toast.makeText(requireContext(), "Введите логин", Toast.LENGTH_SHORT).show();
                return;
            }

            adminsCollection.document(login).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        adminsCollection.document(login).delete()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(requireContext(), "Админ удалён", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    } else {
                        Toast.makeText(requireContext(), "Админ с таким логином не найден", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Ошибка получения данных: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static class Admin {
        public String login;
        public String password;

        public Admin() {}

        public Admin(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }
}
