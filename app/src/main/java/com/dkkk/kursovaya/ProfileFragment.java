package com.dkkk.kursovaya;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView textUser = view.findViewById(R.id.textUser);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences prefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "неизвестный");

        textUser.setText("Вы вошли как: " + username);

        btnLogout.setOnClickListener(v -> {
            requireActivity().finish();
            startActivity(requireActivity().getIntent());
        });

        return view;
    }
}
