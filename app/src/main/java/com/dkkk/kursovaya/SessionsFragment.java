package com.dkkk.kursovaya;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SessionsFragment extends Fragment {

    private EditText etMovieName, etDate, etTime, etHallNumber;
    private Button btnAddSession, btnShowSessions, btnDeleteSession;
    private TextView tvSessionsList;
    private EditText etDeleteSessionId;

    private SessionDatabaseHelper dbHelper;
    private boolean isSessionsVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sessions, container, false);

        etMovieName = view.findViewById(R.id.etMovieName);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        etHallNumber = view.findViewById(R.id.etHallNumber);

        btnAddSession = view.findViewById(R.id.btnAddSession);
        btnShowSessions = view.findViewById(R.id.btnShowSessions);

        etDeleteSessionId = view.findViewById(R.id.etDeleteSessionId);
        btnDeleteSession = view.findViewById(R.id.btnDeleteSession);

        tvSessionsList = view.findViewById(R.id.tvSessionsList);
        tvSessionsList.setVisibility(View.GONE); // по умолчанию скрыт

        dbHelper = new SessionDatabaseHelper(requireContext());

        btnAddSession.setOnClickListener(v -> {
            String movieName = etMovieName.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String hallStr = etHallNumber.getText().toString().trim();

            if (TextUtils.isEmpty(movieName) || TextUtils.isEmpty(date) ||
                    TextUtils.isEmpty(time) || TextUtils.isEmpty(hallStr)) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!date.matches("[0-9.-]+")) {
                Toast.makeText(getContext(), "Дата должна содержать только цифры, точки и дефисы", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!time.matches("[0-9:]+")) {
                Toast.makeText(getContext(), "Время должно содержать только цифры и двоеточие", Toast.LENGTH_SHORT).show();
                return;
            }

            int hallNumber;
            try {
                hallNumber = Integer.parseInt(hallStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Номер зала должен быть числом", Toast.LENGTH_SHORT).show();
                return;
            }

            Session session = new Session(0, movieName, date, time, hallNumber);
            boolean inserted = dbHelper.addSession(session);

            if (inserted) {
                Toast.makeText(getContext(), "Сеанс добавлен", Toast.LENGTH_SHORT).show();
                etMovieName.setText("");
                etDate.setText("");
                etTime.setText("");
                etHallNumber.setText("");
            } else {
                Toast.makeText(getContext(), "Ошибка добавления сеанса", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowSessions.setOnClickListener(v -> {
            if (!isSessionsVisible) {
                ArrayList<Session> sessions = dbHelper.getAllSessions();
                if (sessions.isEmpty()) {
                    tvSessionsList.setText("Сеансы не найдены");
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (Session s : sessions) {
                        builder.append("ID: ").append(s.getId())
                                .append(", Фильм: ").append(s.getMovieName())
                                .append(", Дата: ").append(s.getSessionDate())
                                .append(", Время: ").append(s.getSessionTime())
                                .append(", Зал: ").append(s.getHallNumber())
                                .append("\n");
                    }
                    tvSessionsList.setText(builder.toString());
                }
                tvSessionsList.setVisibility(View.VISIBLE);
                btnShowSessions.setText("Скрыть");
                isSessionsVisible = true;
            } else {
                tvSessionsList.setVisibility(View.GONE);
                btnShowSessions.setText("Показать все сеансы");
                isSessionsVisible = false;
            }
        });

        btnDeleteSession.setOnClickListener(v -> {
            String idStr = etDeleteSessionId.getText().toString().trim();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(getContext(), "Введите ID сеанса для удаления", Toast.LENGTH_SHORT).show();
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "ID должен быть числом", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean deleted = dbHelper.deleteSessionById(id);
            if (deleted) {
                Toast.makeText(getContext(), "Сеанс удалён", Toast.LENGTH_SHORT).show();
                etDeleteSessionId.setText("");
            } else {
                Toast.makeText(getContext(), "Сеанс с таким ID не найден", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
