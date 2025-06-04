package com.dkkk.kursovaya;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class SessionsByDateFragment extends Fragment {

    private RecyclerView recyclerViewSessions;
    private SessionsAdapter sessionsAdapter;
    private FirebaseFirestore db;
    private ArrayList<HashMap<String, String>> sessionsList = new ArrayList<>();
    private ArrayList<Button> dayButtons = new ArrayList<>();
    private String selectedDate = "";
    private Calendar currentWeekStart = Calendar.getInstance();
    private TextView textCurrentWeek;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions_by_date, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerViewSessions = view.findViewById(R.id.recyclerViewSessions);
        recyclerViewSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionsAdapter = new SessionsAdapter(sessionsList);
        recyclerViewSessions.setAdapter(sessionsAdapter);

        textCurrentWeek = view.findViewById(R.id.textCurrentWeek);

        // Найдем кнопки дней по id и положим в список
        dayButtons.add(view.findViewById(R.id.buttonDay0));
        dayButtons.add(view.findViewById(R.id.buttonDay1));
        dayButtons.add(view.findViewById(R.id.buttonDay2));
        dayButtons.add(view.findViewById(R.id.buttonDay3));
        dayButtons.add(view.findViewById(R.id.buttonDay4));
        dayButtons.add(view.findViewById(R.id.buttonDay5));
        dayButtons.add(view.findViewById(R.id.buttonDay6));

        // Кнопки переключения недели
        ImageButton btnPrevWeek = view.findViewById(R.id.buttonPrevWeek);
        ImageButton btnNextWeek = view.findViewById(R.id.buttonNextWeek);

        // Устанавливаем старт недели - понедельник
        setCurrentWeekStartToMonday();

        // Заполняем кнопки датами недели
        updateWeekDays();

        // Навесим обработчики на кнопки дней
        for (int i = 0; i < dayButtons.size(); i++) {
            final int index = i;
            dayButtons.get(i).setOnClickListener(v -> {
                selectedDate = dayButtons.get(index).getText().toString();
                highlightSelectedDay(index);
                loadSessionsForDate(selectedDate);
            });
        }

        // Обработчики переключения недели
        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
            updateWeekDays();
            selectedDate = dayButtons.get(0).getText().toString();
            highlightSelectedDay(0);
            loadSessionsForDate(selectedDate);
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, 7);
            updateWeekDays();
            selectedDate = dayButtons.get(0).getText().toString();
            highlightSelectedDay(0);
            loadSessionsForDate(selectedDate);
        });

        // Изначально выделяем первый день и загружаем данные
        selectedDate = dayButtons.get(0).getText().toString();
        highlightSelectedDay(0);
        loadSessionsForDate(selectedDate);

        return view;
    }

    private void setCurrentWeekStartToMonday() {
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);
    }

    private void updateWeekDays() {
        SimpleDateFormat sdf = new SimpleDateFormat("EE dd.MM", new Locale("ru"));

        Calendar calendar = (Calendar) currentWeekStart.clone();

        for (Button btn : dayButtons) {
            btn.setText(sdf.format(calendar.getTime()));
            btn.setBackgroundResource(android.R.drawable.btn_default); // сброс фона, чтобы убрать выделение
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Обновляем заголовок недели: например "Неделя с 01.01.2025 по 07.01.2025"
        String startWeek = dayButtons.get(0).getText().toString();
        String endWeek = dayButtons.get(dayButtons.size() - 1).getText().toString();
        textCurrentWeek.setText("Неделя с " + startWeek + " по " + endWeek);
    }

    private void highlightSelectedDay(int selectedIndex) {
        for (int i = 0; i < dayButtons.size(); i++) {
            if (i == selectedIndex) {
                dayButtons.get(i).setBackgroundColor(0xFFDDDDFF); // светло-синий фон для выделения
            } else {
                dayButtons.get(i).setBackgroundResource(android.R.drawable.btn_default);
            }
        }
    }

    private void loadSessionsForDate(String date) {
        db.collection("sessions")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HashMap<String, String> session = new HashMap<>();
                        session.put("id", doc.getId());
                        session.put("movieName", doc.getString("movieName"));
                        session.put("time", doc.getString("time"));
                        session.put("hall", doc.getString("hall"));
                        session.put("price", doc.getString("price"));
                        sessionsList.add(session);
                    }
                    Collections.sort(sessionsList, Comparator.comparing(s -> s.get("time")));
                    sessionsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Adapter для сеансов
    static class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {
        private final ArrayList<HashMap<String, String>> data;

        public SessionsAdapter(ArrayList<HashMap<String, String>> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, String> item = data.get(position);
            holder.sessionId.setText("ID: " + item.get("id"));
            holder.movieName.setText(item.get("movieName"));
            holder.time.setText("Время: " + item.get("time"));
            holder.hall.setText("Зал: " + item.get("hall"));
            holder.price.setText("Цена: " + item.get("price") + " ₽");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView movieName, time, hall, price, sessionId;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                movieName = itemView.findViewById(R.id.textMovieName);
                time = itemView.findViewById(R.id.textTime);
                hall = itemView.findViewById(R.id.textHall);
                price = itemView.findViewById(R.id.textPrice);
                sessionId = itemView.findViewById(R.id.textSessionId);
            }
        }
    }
}
