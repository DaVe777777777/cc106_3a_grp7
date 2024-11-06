package com.example.project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String CHANNEL_ID = "your_channel_id"; // Replace with your channel ID
    TextView welcomeText, usernameText;
    ImageView notificationIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.welcomeText);
        usernameText = view.findViewById(R.id.usernameText);
        notificationIcon = view.findViewById(R.id.notificationIcon);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("loggedInUser", "Guest");

        Log.d("HomeFragment", "Retrieved username: " + username);
        usernameText.setText(username);

        notificationIcon.setOnClickListener(v -> {
            // Navigate to NotificationFragment
            Fragment notificationFragment = new NotificationFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, notificationFragment);
            transaction.addToBackStack(null);
            transaction.commit();


        });



        createNotificationChannel();

        return view;
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
