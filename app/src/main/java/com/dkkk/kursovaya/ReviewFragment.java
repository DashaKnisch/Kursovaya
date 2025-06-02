package com.dkkk.kursovaya;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

public class ReviewFragment extends Fragment {

    private LinearLayout reviewsContainer;
    private Button refreshButton;
    private FirebaseFirestore secondDb;

    public ReviewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        reviewsContainer = view.findViewById(R.id.reviewsContainer);
        refreshButton = view.findViewById(R.id.buttonRefreshReviews);

        try {
            // Читаем JSON из raw
            InputStream is = getResources().openRawResource(R.raw.google_services_2);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");

            // Парсим JSON
            JSONObject root = new JSONObject(jsonString);
            JSONArray clients = root.getJSONArray("client");

            // В твоём JSON два клиента, нам нужен второй (index 1)
            JSONObject client = clients.getJSONObject(1);
            JSONObject clientInfo = client.getJSONObject("client_info");

            String apiKey = client.getJSONArray("api_key").getJSONObject(0).getString("current_key");
            String applicationId = clientInfo.getString("mobilesdk_app_id");
            String projectId = root.getJSONObject("project_info").getString("project_id");
            String storageBucket = root.getJSONObject("project_info").getString("storage_bucket");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(applicationId)
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .build();

            FirebaseApp secondApp;
            try {
                secondApp = FirebaseApp.initializeApp(requireContext(), options, "secondApp");
            } catch (IllegalStateException e) {
                secondApp = FirebaseApp.getInstance("secondApp");
            }

            secondDb = FirebaseFirestore.getInstance(secondApp);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка инициализации второго FirebaseApp", Toast.LENGTH_LONG).show();
        }

        refreshButton.setOnClickListener(v -> loadReviewsFromSecondDb());

        return view;
    }

    private void loadReviewsFromSecondDb() {
        int count = reviewsContainer.getChildCount();
        if (count > 1) {
            reviewsContainer.removeViews(1, count - 1);
        }

        Context context = getContext();
        if (context == null || secondDb == null) return;

        secondDb.collection("reviews_other_user")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("author");
                        String text = doc.getString("text");

                        View reviewView = createReviewView(context, name, text);
                        if (reviewView != null) {
                            reviewsContainer.addView(reviewView);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Ошибка загрузки отзывов", Toast.LENGTH_SHORT).show());
    }


    private View createReviewView(Context context, String name, String text) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(24, 24, 24, 24);
        container.setBackgroundColor(Color.parseColor("#FFF9E5"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 20;
        container.setLayoutParams(params);

        ImageView avatar = new ImageView(context);
        avatar.setImageResource(R.drawable.baseline_account_circle_24);
        avatar.setColorFilter(Color.parseColor("#083508"));
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(100, 100);
        avatarParams.setMarginEnd(24);
        avatar.setLayoutParams(avatarParams);

        LinearLayout textBlock = new LinearLayout(context);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        textBlock.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView nameView = new TextView(context);
        nameView.setText(name != null ? name : "Аноним");
        nameView.setTextColor(Color.BLACK);
        nameView.setTypeface(null, Typeface.BOLD);
        nameView.setTextSize(16f);

        TextView reviewView = new TextView(context);
        reviewView.setText(text != null ? text : "");
        reviewView.setTextColor(Color.DKGRAY);
        reviewView.setTextSize(15f);

        textBlock.addView(nameView);
        textBlock.addView(reviewView);

        container.addView(avatar);
        container.addView(textBlock);

        return container;
    }
}
