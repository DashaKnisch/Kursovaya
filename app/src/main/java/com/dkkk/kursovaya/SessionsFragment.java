package com.dkkk.kursovaya;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SessionsFragment extends Fragment {

    private EditText editMovieName, editSessionDate, editSessionTime, editHallNumber;
    private EditText editDeleteMovieName, editDeleteDate, editDeleteTime, editDeleteHall;
    private TextView textAllSessions;
    private Button buttonAddSession, buttonShowSessions, buttonDeleteSession;

    private FirebaseFirestore db;
    private boolean isShown = false;

    public SessionsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);


        editMovieName = view.findViewById(R.id.editMovieName);
        editSessionDate = view.findViewById(R.id.editSessionDate);
        editSessionTime = view.findViewById(R.id.editSessionTime);
        editHallNumber = view.findViewById(R.id.editHallNumber);

        editDeleteMovieName = view.findViewById(R.id.editDeleteMovieName);
        editDeleteDate = view.findViewById(R.id.editDeleteDate);
        editDeleteTime = view.findViewById(R.id.editDeleteTime);
        editDeleteHall = view.findViewById(R.id.editDeleteHall);

        textAllSessions = view.findViewById(R.id.textAllSessions);

        buttonAddSession = view.findViewById(R.id.buttonAddSession);
        buttonShowSessions = view.findViewById(R.id.buttonShowSessions);
        buttonDeleteSession = view.findViewById(R.id.buttonDeleteSession);

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();


        buttonAddSession.setOnClickListener(v -> {
            String name = editMovieName.getText().toString().trim();
            String date = editSessionDate.getText().toString().trim();
            String time = editSessionTime.getText().toString().trim();
            String hall = editHallNumber.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || time.isEmpty() || hall.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> session = new HashMap<>();
            session.put("movieName", name);
            session.put("sessionDate", date);
            session.put("sessionTime", time);
            session.put("hallNumber", hall);

            db.collection("sessions").add(session)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(getContext(), "Сеанс добавлен", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка добавления", Toast.LENGTH_SHORT).show());
        });

        buttonDeleteSession.setOnClickListener(v -> {
            String name = editDeleteMovieName.getText().toString().trim();
            String date = editDeleteDate.getText().toString().trim();
            String time = editDeleteTime.getText().toString().trim();
            String hallStr = editDeleteHall.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || time.isEmpty() || hallStr.isEmpty()) {
                Toast.makeText(getContext(), "Введите название, дату, время и зал", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("sessions")
                    .whereEqualTo("movieName", name)
                    .whereEqualTo("sessionDate", date)
                    .whereEqualTo("sessionTime", time)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean deleted = false;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Object hallObj = doc.get("hallNumber");
                            String hallInDoc = "";
                            if (hallObj instanceof Long) {
                                hallInDoc = String.valueOf(hallObj);
                            } else if (hallObj instanceof String) {
                                hallInDoc = (String) hallObj;
                            }

                            if (hallStr.equals(hallInDoc)) {
                                doc.getReference().delete();
                                deleted = true;
                            }
                        }
                        if (deleted) {
                            Toast.makeText(getContext(), "Сеанс удалён", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Сеанс не найден", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });


        // Показать/Скрыть сеансы
        buttonShowSessions.setOnClickListener(v -> {
            if (!isShown) {
                db.collection("sessions").get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            StringBuilder builder = new StringBuilder();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                String movieName = doc.getString("movieName");
                                String sessionDate = doc.getString("sessionDate");
                                String sessionTime = doc.getString("sessionTime");
                                Long hallNumber = null;

                                try {
                                    hallNumber = doc.getLong("hallNumber");
                                } catch (Exception ignored) {
                                    String hallStr = doc.getString("hallNumber");
                                    if (hallStr != null) {
                                        try {
                                            hallNumber = Long.parseLong(hallStr);
                                        } catch (NumberFormatException e) {
                                            hallNumber = 0L;
                                        }
                                    }
                                }

                                if (movieName == null) movieName = "Без названия";
                                if (sessionDate == null) sessionDate = "Не указана";
                                if (sessionTime == null) sessionTime = "Не указано";
                                if (hallNumber == null) hallNumber = 0L;

                                builder.append("Фильм: ").append(movieName).append("\n")
                                        .append("Дата: ").append(sessionDate).append("\n")
                                        .append("Время: ").append(sessionTime).append("\n")
                                        .append("Зал: №").append(hallNumber).append("\n\n");
                            }
                            textAllSessions.setText(builder.toString().trim());
                            textAllSessions.setVisibility(View.VISIBLE);
                            buttonShowSessions.setText("Скрыть сеансы");
                            isShown = true;
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                textAllSessions.setText("");
                textAllSessions.setVisibility(View.GONE);
                buttonShowSessions.setText("Показать сеансы");
                isShown = false;
            }
        });

        return view;
    }
}
