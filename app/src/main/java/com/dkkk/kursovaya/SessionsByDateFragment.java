package com.dkkk.kursovaya;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionsByDateFragment extends Fragment {

    private Calendar currentWeekStart;
    private TextView weekRangeText;
    private LinearLayout daysContainer;
    private LinearLayout sessionsContainer;
    private Button selectedDayButton = null;
    private final SimpleDateFormat dayLabelFormat = new SimpleDateFormat("EE dd", new Locale("ru"));
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SessionsByDateFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessions_by_date, container, false);

        weekRangeText = view.findViewById(R.id.week_range);
        daysContainer = view.findViewById(R.id.days_container);
        sessionsContainer = view.findViewById(R.id.sessions_container);
        Button btnPrev = view.findViewById(R.id.btn_prev_week);
        Button btnNext = view.findViewById(R.id.btn_next_week);

        currentWeekStart = getWeekStart(Calendar.getInstance());

        btnPrev.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekView();
        });

        btnNext.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekView();
        });

        updateWeekView();

        return view;
    }

    private Calendar getWeekStart(Calendar date) {
        Calendar start = (Calendar) date.clone();
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return start;
    }

    private void updateWeekView() {
        daysContainer.removeAllViews();

        Calendar day = (Calendar) currentWeekStart.clone();
        SimpleDateFormat headerFormat = new SimpleDateFormat("dd.MM");
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);

        String header = "Неделя " + headerFormat.format(day.getTime()) +
                " - " + headerFormat.format(weekEnd.getTime());
        weekRangeText.setText(header);

        for (int i = 0; i < 7; i++) {
            Button dayButton = new Button(getContext());
            dayButton.setText(dayLabelFormat.format(day.getTime()));
            dayButton.setTextAppearance(requireContext(), R.style.DayButtonStyle);

            Calendar selectedDay = (Calendar) day.clone();

            dayButton.setOnClickListener(v -> {
                if (selectedDayButton != null) {
                    selectedDayButton.setTextAppearance(requireContext(), R.style.DayButtonStyle);
                }
                selectedDayButton = dayButton;
                dayButton.setTextAppearance(requireContext(), R.style.SelectedDayButton);
                loadSessionsForDate(selectedDay);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(4, 0, 4, 0);
            dayButton.setLayoutParams(params);

            daysContainer.addView(dayButton);

            // Автовыбор понедельника при загрузке
            if (i == 0 && selectedDayButton == null) {
                dayButton.performClick();
            }

            day.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadSessionsForDate(Calendar date) {
        sessionsContainer.removeAllViews();

        // Начало дня (00:00:00)
        Calendar start = (Calendar) date.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        // Конец дня — начало следующего дня
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);

        db.collection("sessions")
                .whereGreaterThanOrEqualTo("date", start.getTime())
                .whereLessThan("date", end.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QueryDocumentSnapshot> sessions = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        sessions.add(doc);
                    }

                    // Сортируем по времени
                    sessions.sort(Comparator.comparing(s -> s.getString("time")));

                    for (QueryDocumentSnapshot doc : sessions) {
                        String sessionId = doc.getId();
                        String movieName = doc.getString("movieName");
                        String time = doc.getString("time");
                        String hall = doc.getString("hall");
                        String price = String.valueOf(doc.get("price"));

                        String info = "ID: " + sessionId + "\n" +
                                movieName + "\n" +
                                "Время: " + time + "\n" +
                                "Зал: " + hall + " | Цена: " + price + "₽";

                        TextView sessionView = new TextView(getContext());
                        sessionView.setText(info);
                        sessionView.setTextColor(ContextCompat.getColor(requireContext(), R.color.field_text));
                        sessionView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.field_background));
                        sessionView.setPadding(24, 24, 24, 24);
                        sessionView.setTextSize(16);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 12, 0, 12);
                        sessionView.setLayoutParams(params);

                        sessionsContainer.addView(sessionView);
                    }

                    if (sessions.isEmpty()) {
                        TextView emptyView = new TextView(getContext());
                        emptyView.setText("Нет сеансов на этот день.");
                        emptyView.setTextColor(ContextCompat.getColor(requireContext(), R.color.field_hint));
                        emptyView.setPadding(24, 24, 24, 24);
                        sessionsContainer.addView(emptyView);
                    }
                })
                .addOnFailureListener(e -> {
                    TextView errorView = new TextView(getContext());
                    errorView.setText("Ошибка загрузки сеансов.");
                    errorView.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_color));
                    sessionsContainer.addView(errorView);
                });
    }


}


