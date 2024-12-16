package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// In VaccineDisplayFragment.java

public class VaccineDisplayFragment extends Fragment {

    private static final String ARG_VACCINE = "vaccine";
    private static final String ARG_PET = "pet";
    private static final String ARG_REPEAT_DAYS = "repeatDays";
    private Vaccine vaccine;
    private Pet pet;
    private int repeatDays;

    private TextView repeatDaysTextView;

    public static VaccineDisplayFragment newInstance(Vaccine vaccine, Pet pet) {
        VaccineDisplayFragment fragment = new VaccineDisplayFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VACCINE, vaccine);
        args.putSerializable(ARG_PET, pet);
        args.putInt(ARG_REPEAT_DAYS, vaccine.getRepeatDays());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vaccine = (Vaccine) getArguments().getSerializable(ARG_VACCINE);
            pet = (Pet) getArguments().getSerializable(ARG_PET);
            repeatDays = getArguments().getInt(ARG_REPEAT_DAYS, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vaccine_display, container, false);

        // Display vaccine details
        TextView vaccineName = view.findViewById(R.id.vaccine_name_display);
        TextView vaccineDate = view.findViewById(R.id.vaccine_date_display);
        TextView vaccineTime = view.findViewById(R.id.vaccine_time_display);
        TextView drugName = view.findViewById(R.id.drug_name_display);
        TextView veterinarianName = view.findViewById(R.id.veterinarian_name_display);
        TextView clinicPlace = view.findViewById(R.id.clinic_place_display);
        repeatDaysTextView = view.findViewById(R.id.repeat_days_display); // New TextView for repeat days
        Button backButton = view.findViewById(R.id.backButton);
        ImageView updateIcon = view.findViewById(R.id.updateIcon);
        ImageView deleteIcon = view.findViewById(R.id.deleteIcon);

        // Set text to display vaccine details
        vaccineName.setText(vaccine.getVaccineName());
        vaccineDate.setText(vaccine.getVaccineDate());
        vaccineTime.setText(vaccine.getVaccineTime());
        drugName.setText(vaccine.getDrugName());
        veterinarianName.setText(vaccine.getVetName());
        clinicPlace.setText(vaccine.getClinicLocation());

        // Display repeat days if available, otherwise hide the view
        updateRepeatDaysDisplay(repeatDays);

        // Set back button to navigate to VaccinationFragment
        backButton.setOnClickListener(v -> navigateToVaccinationFragment());

        // Set the update icon to navigate to VaccineUpdateFragment
        updateIcon.setOnClickListener(v -> navigateToVaccineUpdateFragment());

        // Set the delete icon's click listener
        deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Vaccine")
                    .setMessage("Are you sure you want to delete this vaccine record?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        DBHelper dbHelper = new DBHelper(getContext());
                        boolean isDeleted = dbHelper.deleteVaccine(vaccine.getVaccineId());
                        if (isDeleted) {
                            Toast.makeText(getContext(), "Vaccine deleted successfully", Toast.LENGTH_SHORT).show();
                            navigateToVaccinationFragment();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete vaccine", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        return view;
    }

    public void updateRepeatDaysDisplay(int repeatDays) {
        // Make sure we're on the main thread before updating the UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (repeatDaysTextView != null) {
                    if (repeatDays > 0) {
                        repeatDaysTextView.setText("Repeats every " + repeatDays + " days");
                        repeatDaysTextView.setVisibility(View.VISIBLE);  // Ensure it's visible
                    } else {
                        repeatDaysTextView.setVisibility(View.GONE);  // Hide if invalid
                    }
                }
            });
        }
    }


    private void navigateToVaccineUpdateFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VaccineUpdateFragment vaccineUpdateFragment = VaccineUpdateFragment.newInstance(vaccine, pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccineUpdateFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToVaccinationFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VaccinationFragment vaccinationFragment = VaccinationFragment.newInstance(pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccinationFragment);
        fragmentTransaction.commit();
    }
}

