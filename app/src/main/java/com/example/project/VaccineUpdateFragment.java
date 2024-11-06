package com.example.project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;

public class VaccineUpdateFragment extends Fragment {

    private static final String ARG_VACCINE = "vaccine";
    private static final String ARG_PET = "pet";
    private Vaccine vaccine;
    private Pet pet;

    public static VaccineUpdateFragment newInstance(Vaccine vaccine, Pet pet) {
        VaccineUpdateFragment fragment = new VaccineUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VACCINE, vaccine);
        args.putSerializable(ARG_PET, pet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vaccine = (Vaccine) getArguments().getSerializable(ARG_VACCINE);
            pet = (Pet) getArguments().getSerializable(ARG_PET);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vaccine_update, container, false);

        // Initialize input fields
        EditText vaccineNameInput = view.findViewById(R.id.vaccine_name_input);
        EditText vaccineDateInput = view.findViewById(R.id.vaccine_date_input);
        EditText vaccineTimeInput = view.findViewById(R.id.vaccine_time_input);
        EditText drugNameInput = view.findViewById(R.id.drug_name_input);
        EditText vetNameInput = view.findViewById(R.id.vet_name_input);
        EditText clinicPlaceInput = view.findViewById(R.id.clinic_place_input);

        // Set existing vaccine details
        vaccineNameInput.setText(vaccine.getVaccineName());
        vaccineDateInput.setText(vaccine.getVaccineDate());
        vaccineTimeInput.setText(vaccine.getVaccineTime());
        drugNameInput.setText(vaccine.getDrugName());
        vetNameInput.setText(vaccine.getVetName());
        clinicPlaceInput.setText(vaccine.getClinicLocation());

        // Handle save button click
        Button saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            // Validate fields
            if (TextUtils.isEmpty(vaccineNameInput.getText().toString()) ||
                    TextUtils.isEmpty(vaccineDateInput.getText().toString()) ||
                    TextUtils.isEmpty(vaccineTimeInput.getText().toString()) ||
                    TextUtils.isEmpty(drugNameInput.getText().toString()) ||
                    TextUtils.isEmpty(vetNameInput.getText().toString()) ||
                    TextUtils.isEmpty(clinicPlaceInput.getText().toString())) {
                // Show error message if any field is empty
                Toast.makeText(getContext(), "All fields must be filled out", Toast.LENGTH_SHORT).show();
            } else {
                // Update vaccine details
                vaccine.setVaccineName(vaccineNameInput.getText().toString());
                vaccine.setVaccineDate(vaccineDateInput.getText().toString());
                vaccine.setVaccineTime(vaccineTimeInput.getText().toString());
                vaccine.setDrugName(drugNameInput.getText().toString());
                vaccine.setVetName(vetNameInput.getText().toString());
                vaccine.setClinicLocation(clinicPlaceInput.getText().toString());

                // Save to database
                DBHelper dbHelper = new DBHelper(getContext());
                boolean isUpdated = dbHelper.updateVaccine(vaccine);
                if (isUpdated) {
                    // Schedule the notification
                    scheduleVaccineNotification(vaccine);

                    // Show a success message
                    Toast.makeText(getContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
                } else {
                    // Show an error message
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }

                // Navigate back to VaccineDisplayFragment
                navigateBackToDisplayFragment();
            }
        });

        // Handle cancel button click
        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            // Navigate back to VaccineDisplayFragment
            navigateBackToDisplayFragment();
        });

        return view;
    }

    // Schedule notification for vaccine update
    private void scheduleVaccineNotification(Vaccine vaccine) {
        Calendar calendar = Calendar.getInstance();

        // Assuming vaccine date and time are stored as strings in the format "yyyy-MM-dd" and "HH:mm"
        String[] dateParts = vaccine.getVaccineDate().split("-");
        String[] timeParts = vaccine.getVaccineTime().split(":");

        calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Months are zero-based
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        // Use AlarmManager to schedule the notification
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("title", "Vaccination Reminder");
        intent.putExtra("message", "It's time for " + pet.getName() + "'s vaccine: " + vaccine.getVaccineName());

        // Specify FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    // Navigate back to VaccineDisplayFragment
    private void navigateBackToDisplayFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack(); // Return to the previous fragment
    }
}
