package com.dkkk.kursovaya;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SalesFragment extends Fragment {

    private TextView salesTextView;
    private Button loadButton;

    public SalesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales, container, false);

        salesTextView = view.findViewById(R.id.salesTextView);
        loadButton = view.findViewById(R.id.loadButton);

        loadButton.setOnClickListener(v -> loadSalesData());

        return view;
    }

    private void loadSalesData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference salesRef = db.collection("ticket_sales");

        final StringBuilder result = new StringBuilder();

        salesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String movie = document.getString("movieName");
                        Long tickets = document.getLong("tickets");
                        Double price = document.getDouble("ticketPrice");

                        if (movie == null) movie = "Нет названия";
                        if (tickets == null) tickets = 0L;
                        if (price == null) price = 0.0;

                        double totalSum = tickets * price;

                        result.append("Фильм: ").append(movie).append("\n")
                                .append("Цена за билет: ").append(price).append(" руб.\n")
                                .append("Продано билетов: ").append(tickets).append("\n")
                                .append("Общая сумма: ").append(totalSum).append(" руб.\n\n");
                    }
                    salesTextView.setText(result.toString());
                })
                .addOnFailureListener(e -> salesTextView.setText("Ошибка: " + e.getMessage()));
    }

}

