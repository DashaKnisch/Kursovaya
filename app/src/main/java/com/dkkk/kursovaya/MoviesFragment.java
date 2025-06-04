package com.dkkk.kursovaya;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.*;

import java.util.*;

public class MoviesFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText editTextTitle, editTextYear, editTextDirector;
    private Button buttonAdd, buttonUpdate, buttonDelete;
    private ListView listViewMovies;

    private ArrayList<String> movieList = new ArrayList<>();
    private ArrayList<String> movieIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String selectedMovieId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);

        db = FirebaseFirestore.getInstance();

        editTextTitle = root.findViewById(R.id.editTextTitle);
        editTextYear = root.findViewById(R.id.editTextYear);
        editTextDirector = root.findViewById(R.id.editTextDirector);
        buttonAdd = root.findViewById(R.id.buttonAdd);
        buttonUpdate = root.findViewById(R.id.buttonUpdate);
        buttonDelete = root.findViewById(R.id.buttonDelete);
        listViewMovies = root.findViewById(R.id.listViewMovies);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, movieList);
        listViewMovies.setAdapter(adapter);

        // 🔥 Автообновление списка при изменениях в базе
        db.collection("movies")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Ошибка чтения", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    movieList.clear();
                    movieIds.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String year = doc.getString("year");
                        String director = doc.getString("director");

                        movieList.add("Название: " + title + "\nГод: " + year + "\nРежиссёр: " + director);
                        movieIds.add(id);
                    }

                    adapter.notifyDataSetChanged();
                });

        buttonAdd.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String year = editTextYear.getText().toString().trim();
            String director = editTextDirector.getText().toString().trim();

            if (title.isEmpty() || year.isEmpty() || director.isEmpty()) {
                Toast.makeText(getContext(), "Заполни все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> movie = new HashMap<>();
            movie.put("title", title);
            movie.put("year", year);
            movie.put("director", director);

            db.collection("movies").add(movie)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(), "Фильм добавлен", Toast.LENGTH_SHORT).show();
                        clearFields();
                    });
        });

        buttonUpdate.setOnClickListener(v -> {
            if (selectedMovieId == null) {
                Toast.makeText(getContext(), "Выбери фильм", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = editTextTitle.getText().toString().trim();
            String year = editTextYear.getText().toString().trim();
            String director = editTextDirector.getText().toString().trim();

            Map<String, Object> updatedMovie = new HashMap<>();
            updatedMovie.put("title", title);
            updatedMovie.put("year", year);
            updatedMovie.put("director", director);

            db.collection("movies").document(selectedMovieId).update(updatedMovie)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Обновлено", Toast.LENGTH_SHORT).show();
                        clearFields();
                    });
        });

        buttonDelete.setOnClickListener(v -> {
            if (selectedMovieId == null) {
                Toast.makeText(getContext(), "Выбери фильм", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("movies").document(selectedMovieId).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                        clearFields();
                    });
        });

        listViewMovies.setOnItemClickListener((parent, view, position, id) -> {
            selectedMovieId = movieIds.get(position);
            db.collection("movies").document(selectedMovieId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            editTextTitle.setText(doc.getString("title"));
                            editTextYear.setText(doc.getString("year"));
                            editTextDirector.setText(doc.getString("director"));
                        }
                    });
        });

        return root;
    }

    private void clearFields() {
        editTextTitle.setText("");
        editTextYear.setText("");
        editTextDirector.setText("");
        selectedMovieId = null;
    }
}
