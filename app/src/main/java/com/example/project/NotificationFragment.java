package com.example.project;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.List;

public class NotificationFragment extends Fragment {

    private DBHelper dbHelper;
    private static final String CHANNEL_ID = "PetNotifications";
    private static final String CHANNEL_NAME = "Pet Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for pet birthdays and vaccinations";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private static final int ALARM_PERMISSION_REQUEST_CODE = 2;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        TextView notificationTextView = view.findViewById(R.id.notificationTextView);

        dbHelper = new DBHelper(getContext());
        checkPermissions();

        // Fetch the current username from SharedPreferences
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            notificationTextView.setText("User not logged in.");
            return view;
        }


        NotificationViewModel notificationViewModel = new ViewModelProvider(this,
                new NotificationViewModelFactory(dbHelper, currentUsername)).get(NotificationViewModel.class);

// Observe changes to pets with birthdays
        notificationViewModel.getPetsWithBirthdayLiveData().observe(getViewLifecycleOwner(), petsWithBirthday -> {
            updateNotifications(notificationTextView, petsWithBirthday, notificationViewModel.getVaccinesDueTodayLiveData().getValue());
        });

// Observe changes to vaccines due today
        notificationViewModel.getVaccinesDueTodayLiveData().observe(getViewLifecycleOwner(), vaccinesDueToday -> {
            updateNotifications(notificationTextView, notificationViewModel.getPetsWithBirthdayLiveData().getValue(), vaccinesDueToday);
        });


        return view;
    }

    private void updateNotifications(TextView notificationTextView, List<Pet> petsWithBirthday, List<Vaccine> vaccinesDueToday) {
        StringBuilder notifications = new StringBuilder();

        if (petsWithBirthday != null && !petsWithBirthday.isEmpty()) {
            notifications.append("Today's Birthdays:\n");
            for (Pet pet : petsWithBirthday) {
                notifications.append("- ").append(pet.getName()).append("\n");
                scheduleNotification(getContext(), "Birthday Reminder", "Today is " + pet.getName() + "'s birthday!", pet.getDob());
            }
        } else {
            notifications.append("No pets have birthdays today.\n");
        }

        if (vaccinesDueToday != null && !vaccinesDueToday.isEmpty()) {
            notifications.append("Vaccinations Due Today:\n");
            for (Vaccine vaccine : vaccinesDueToday) {
                notifications.append("- ").append(vaccine.getVaccineName()).append(" for pet ID: ").append(vaccine.getPetId()).append("\n");
                scheduleNotification(getContext(), "Vaccination Reminder", vaccine.getVaccineName() + " for pet ID: " + vaccine.getPetId(), vaccine.getVaccineDate());
            }
        } else {
            notifications.append("No vaccinations are due today.\n");
        }

        notificationTextView.setText(notifications.toString());
    }

    // New method to retrieve the current logged-in username
    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("loggedInUser", null); // Use null to check if user is logged in
        if (username == null) {
            Log.d("NotificationFragment", "User not logged in.");
            return null; // Return null if no user is logged in
        }
        return username; // Return the username directly
    }





    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, ALARM_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle permission result logic here if needed
    }

    public void scheduleNotification(Context context, String title, String message, String date) {
        String[] dateParts = date.split("-"); // Expecting format "yyyy-MM-dd"
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is zero-based in Calendar
        int day = Integer.parseInt(dateParts[2]);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("notificationTitle", title);
        intent.putExtra("notificationText", message);

        int notificationId = (year * 10000) + (month * 100) + day;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 8, 0, 0); // Set time for the notification

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
