package com.example.project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String CHANNEL_ID = "your_channel_id";
    TextView welcomeText, usernameText;
    ImageView notificationIcon, logo;  // Add logo reference
    RecyclerView homePetRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.welcomeText);
        usernameText = view.findViewById(R.id.usernameText);
        notificationIcon = view.findViewById(R.id.notificationIcon);
        logo = view.findViewById(R.id.logo);  // Initialize the logo ImageView
        TextView totalPetsText = view.findViewById(R.id.totalPetsText);
        TextView totalVaccinesText = view.findViewById(R.id.totalVaccinesText);
        homePetRecyclerView = view.findViewById(R.id.homePetRecyclerView);

        // Set up logo click listener to navigate to DeveloperFragment
        logo.setOnClickListener(v -> {
            DeveloperFragment developerFragment = new DeveloperFragment(); // Create an instance of DeveloperFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, developerFragment); // Replace current fragment with DeveloperFragment
            transaction.addToBackStack(null); // Optional: if you want to allow returning to the previous fragment
            transaction.commit();
        });

        // Retrieve the logged-in user's details
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("loggedInUser", "Guest");
        Log.d("HomeFragment", "Retrieved username: " + username);
        usernameText.setText(username);

        // Fetch totals from DBHelper
        DBHelper dbHelper = new DBHelper(getContext());
        int totalPets = dbHelper.getTotalPetsForUser(username);
        int totalVaccines = dbHelper.getTotalVaccinesForUser(username);

        totalPetsText.setText(String.valueOf(totalPets));
        totalVaccinesText.setText(String.valueOf(totalVaccines));

        // Fetch pet data and set up RecyclerView
        List<Pet> petList = dbHelper.getPetsForUser(username);
        if (petList == null || petList.isEmpty()) {
            Log.d("HomeFragment", "No pets found for user: " + username);
            // Optionally display a placeholder message
        } else {
            setupRecyclerView(petList);
        }

        // Notification icon click listener
        notificationIcon.setOnClickListener(v -> {
            Fragment notificationFragment = new NotificationFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, notificationFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        createNotificationChannel();
        return view;
    }

    private void setupRecyclerView(List<Pet> petList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        homePetRecyclerView.setLayoutManager(layoutManager);

        // Create the adapter without passing a click listener
        HomePetAdapter adapter = new HomePetAdapter(getContext(), petList, null); // Passing null as we don't need the click listener anymore

        homePetRecyclerView.setAdapter(adapter);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
