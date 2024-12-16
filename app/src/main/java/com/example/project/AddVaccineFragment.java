package com.example.project;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVaccineFragment extends Fragment {

    private static final String ARG_PET = "pet";
    private Pet pet;
    private EditText vaccineNameInput, drugNameInput, vaccineDateInput, vaccineTimeInput, veterinarianNameInput, clinicPlaceInput, vaccineRepeatDaysInput;
    private DBHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();

    public static AddVaccineFragment newInstance(Pet pet) {
        AddVaccineFragment fragment = new AddVaccineFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PET, pet); // Pass the pet object
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable(ARG_PET); // Retrieve the pet object
        }
        dbHelper = new DBHelper(getActivity()); // Initialize the database helper
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_vaccine, container, false);

        // Initialize inputs
        vaccineNameInput = view.findViewById(R.id.VaccineNameInput);
        drugNameInput = view.findViewById(R.id.DrugNameInput);
        vaccineDateInput = view.findViewById(R.id.VaccineDateInput);
        vaccineTimeInput = view.findViewById(R.id.VaccineTimeInput);
        veterinarianNameInput = view.findViewById(R.id.VeterinarianNameInput);
        clinicPlaceInput = view.findViewById(R.id.ClinicPlaceInput);
        vaccineRepeatDaysInput = view.findViewById(R.id.VaccineRepeatDaysInput);

        // Set up date and time pickers
        vaccineDateInput.setOnClickListener(v -> showDatePickerDialog());
        vaccineTimeInput.setOnClickListener(v -> showTimePickerDialog());

        // Set up buttons
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> onSaveButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());

        return view;
    }

    // Show Date Picker dialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInput();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Show Time Picker dialog
    private void showTimePickerDialog() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                (view, hourOfDay, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    updateTimeInput();
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    // Update the EditText field with the selected date
    private void updateDateInput() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        vaccineDateInput.setText(dateFormat.format(calendar.getTime()));
    }

    // Update the EditText field with the selected time
    private void updateTimeInput() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        vaccineTimeInput.setText(timeFormat.format(calendar.getTime()));
    }

    // Save the vaccine data to the database
    // Save the vaccine data to the database
    private void saveVaccineData() {
        String vaccineName = vaccineNameInput.getText().toString().trim();
        String drugName = drugNameInput.getText().toString().trim();
        String vaccineDate = vaccineDateInput.getText().toString().trim();
        String vaccineTime = vaccineTimeInput.getText().toString().trim();
        String veterinarianName = veterinarianNameInput.getText().toString().trim();
        String clinicPlace = clinicPlaceInput.getText().toString().trim();
        String repeatDaysText = vaccineRepeatDaysInput.getText().toString().trim();
        int repeatDays = repeatDaysText.isEmpty() ? 0 : Integer.parseInt(repeatDaysText);

        if (vaccineName.isEmpty() || drugName.isEmpty() || vaccineDate.isEmpty() || vaccineTime.isEmpty() ||
                veterinarianName.isEmpty() || clinicPlace.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pet == null) {
            Toast.makeText(getActivity(), "Pet data is unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert vaccine data into the database
        boolean success = dbHelper.insertVaccine(vaccineName, drugName, vaccineDate, vaccineTime, pet.getId(), veterinarianName, clinicPlace, repeatDays);

        if (success) {
            Snackbar.make(requireView(), "Vaccine added successfully!", Snackbar.LENGTH_SHORT).show();
            scheduleVaccineNotification(vaccineName, vaccineDate, vaccineTime, repeatDays); // Pass repeatDays here as well
            goToVaccinationFragment(); // Navigate to VaccinationFragment
        } else {
            Snackbar.make(requireView(), "Failed to add vaccine", Snackbar.LENGTH_SHORT).show();
        }
    }


    // Schedule the notification and handle repeating logic
    private void scheduleVaccineNotification(String vaccineName, String vaccineDate, String vaccineTime, int repeatDays) {
        // Convert the selected date and time into milliseconds
        long notificationTime = calendar.getTimeInMillis(); // Calendar is already set with the selected date and time

        // Use the passed repeatDays argument for notification repetition
        if (repeatDays < 0) {
            repeatDays = 0; // Prevent negative repeatDays values
        }

        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        String title = "Vaccine Reminder for " + pet.getName();
        String message = pet.getName() + " needs the " + vaccineName + " vaccine on " + vaccineDate + " at " + vaccineTime;

        intent.putExtra("notificationTitle", title);
        intent.putExtra("notificationText", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Schedule the first notification
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);

            // If a repeat interval is set, schedule repeating notifications
            if (repeatDays > 0) {
                long repeatInterval = repeatDays * 24 * 60 * 60 * 1000; // Convert repeat days to milliseconds
                long nextNotificationTime = notificationTime + repeatInterval;

                // Schedule repeating notifications with setExact for accuracy
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextNotificationTime, pendingIntent);
            }

        }
    }


    // Navigate to VaccinationFragment
    private void goToVaccinationFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        VaccinationFragment vaccinationFragment = VaccinationFragment.newInstance(pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccinationFragment); // Ensure this is the correct container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void onCancelButtonClicked() {
        goToVaccinationFragment(); // Go back to the VaccinationFragment when 'Cancel' is clicked
    }

    private void onSaveButtonClicked() {
        saveVaccineData(); // Save the vaccine data and navigate to VaccinationFragment
    }
}
