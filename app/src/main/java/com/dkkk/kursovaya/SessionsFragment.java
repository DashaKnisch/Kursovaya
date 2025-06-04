package com.dkkk.kursovaya;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SessionsFragment extends Fragment {

    private EditText editMovieName;
    private EditText editSessionDate, editSessionTime, editHallNumber, editTicketPrice, editSessionId;
    private Button buttonAddSession, buttonDeleteSession;
    private ListView listViewMovies;

    private FirebaseFirestore db;

    // Для списка фильмов
    private ArrayList<String> movieDisplayList = new ArrayList<>();
    private ArrayList<String> movieIds = new ArrayList<>();
    private ArrayAdapter<String> movieAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false);

        FirebaseApp.initializeApp(requireContext());

        editMovieName = view.findViewById(R.id.editMovieName);
        editSessionDate = view.findViewById(R.id.editSessionDate);
        editSessionTime = view.findViewById(R.id.editSessionTime);
        editHallNumber = view.findViewById(R.id.editHallNumber);
        editTicketPrice = view.findViewById(R.id.editTicketPrice);
        editSessionId = view.findViewById(R.id.editSessionId);

        buttonAddSession = view.findViewById(R.id.buttonAddSession);
        buttonDeleteSession = view.findViewById(R.id.buttonDeleteSession);

        listViewMovies = view.findViewById(R.id.listViewMovies);

        db = FirebaseFirestore.getInstance();

        movieAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, movieDisplayList);
        listViewMovies.setAdapter(movieAdapter);

        loadMoviesFromFirestore();

        listViewMovies.setOnItemClickListener((parent, view1, position, id) -> {
            String movieInfo = movieDisplayList.get(position);
            editMovieName.setText(movieInfo);
        });

        buttonAddSession.setOnClickListener(v -> addSession());
        buttonDeleteSession.setOnClickListener(v -> deleteSession());

        return view;
    }

    private void loadMoviesFromFirestore() {
        db.collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    movieDisplayList.clear();
                    movieIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String year = doc.getString("year");
                        String director = doc.getString("director");

                        if (title != null && year != null && director != null) {
                            String info = "Фильм: " + title + ", Год: " + year + ", Режиссёр: " + director;
                            movieDisplayList.add(info);
                            movieIds.add(doc.getId());
                        }
                    }
                    movieAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Ошибка загрузки фильмов: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addSession() {
        String movieName = editMovieName.getText().toString().trim();
        String date = editSessionDate.getText().toString().trim();
        String time = editSessionTime.getText().toString().trim();
        String hall = editHallNumber.getText().toString().trim();
        String price = editTicketPrice.getText().toString().trim();
        String sessionId = editSessionId.getText().toString().trim();

        if (movieName.isEmpty() || date.isEmpty() || time.isEmpty() || hall.isEmpty() || price.isEmpty() || sessionId.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("movieName", movieName);
        sessionData.put("date", date);
        sessionData.put("time", time);
        sessionData.put("hall", hall);
        sessionData.put("price", price);

        db.collection("sessions")
                .document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Сеанс добавлен", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteSession() {
        String sessionId = editSessionId.getText().toString().trim();
        if (sessionId.isEmpty()) {
            Toast.makeText(getContext(), "Введите ID сеанса для удаления", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("sessions")
                .document(sessionId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Сеанс удалён", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
