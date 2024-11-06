package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
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

public class VaccineDisplayFragment extends Fragment {

    private static final String ARG_VACCINE = "vaccine";
    private static final String ARG_PET = "pet"; // Add Pet object for passing to VaccinationFragment
    private Vaccine vaccine;
    private Pet pet; // Store the Pet object for navigation

    public static VaccineDisplayFragment newInstance(Vaccine vaccine, Pet pet) {
        VaccineDisplayFragment fragment = new VaccineDisplayFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VACCINE, vaccine); // Pass the vaccine object
        args.putSerializable(ARG_PET, pet); // Pass the pet object
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vaccine = (Vaccine) getArguments().getSerializable(ARG_VACCINE); // Retrieve the vaccine object
            pet = (Pet) getArguments().getSerializable(ARG_PET); // Retrieve the pet object
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
        Button backButton = view.findViewById(R.id.backButton);
        ImageView updateIcon = view.findViewById(R.id.updateIcon); // Find the update icon
        ImageView deleteIcon = view.findViewById(R.id.deleteIcon); // Find the delete icon

        // Set text to display vaccine details
        vaccineName.setText(vaccine.getVaccineName());
        vaccineDate.setText(vaccine.getVaccineDate());
        vaccineTime.setText(vaccine.getVaccineTime());
        drugName.setText(vaccine.getDrugName());
        veterinarianName.setText(vaccine.getVetName());
        clinicPlace.setText(vaccine.getClinicLocation());

        // Set back button to navigate to VaccinationFragment
        backButton.setOnClickListener(v -> navigateToVaccinationFragment());

        // Set the update icon to navigate to VaccineUpdateFragment
        updateIcon.setOnClickListener(v -> navigateToVaccineUpdateFragment());

        // Set the delete icon's click listener
        deleteIcon.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Vaccine")
                    .setMessage("Are you sure you want to delete this vaccine record?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Call delete method
                        DBHelper dbHelper = new DBHelper(getContext());
                        boolean isDeleted = dbHelper.deleteVaccine(vaccine.getVaccineId());
                        if (isDeleted) {
                            Toast.makeText(getContext(), "Vaccine deleted successfully", Toast.LENGTH_SHORT).show();
                            // Navigate back to the VaccinationFragment after deletion
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

    // Method to navigate to VaccineUpdateFragment
    private void navigateToVaccineUpdateFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Create an instance of VaccineUpdateFragment and pass the vaccine object
        VaccineUpdateFragment vaccineUpdateFragment = VaccineUpdateFragment.newInstance(vaccine, pet);

        fragmentTransaction.replace(R.id.fragment_container, vaccineUpdateFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Navigate back to VaccinationFragment (passing the Pet object)
    private void navigateToVaccinationFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();

        // Pop back stack to ensure VaccineDisplayFragment doesn't stay in the back stack
        fragmentManager.popBackStack();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace VaccineDisplayFragment with VaccinationFragment and pass the Pet object
        VaccinationFragment vaccinationFragment = VaccinationFragment.newInstance(pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccinationFragment);
        fragmentTransaction.commit();  // No need to add to back stack
    }
}
