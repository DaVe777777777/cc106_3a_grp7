package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VaccinationFragment extends Fragment implements VaccineAdapter.OnVaccineClickListener {

    private static final String ARG_PET = "pet";
    private Pet pet; // Pet object to store the passed pet
    private DBHelper dbHelper; // Database helper
    private RecyclerView recyclerView; // RecyclerView to display vaccine cards
    private VaccineAdapter vaccineAdapter; // Adapter for RecyclerView
    private List<Vaccine> vaccineList; // List of vaccines

    public static VaccinationFragment newInstance(Pet pet) {
        VaccinationFragment fragment = new VaccinationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PET, pet); // Pass the pet object as an argument
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable(ARG_PET); // Retrieve the pet object from the arguments
        }
        dbHelper = new DBHelper(getActivity()); // Initialize the database helper
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vaccination, container, false);

        // Back button functionality
        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> navigateToProfilePetFragment());

        // Add Vaccination button functionality
        ImageButton addVaccinationBtn = view.findViewById(R.id.add_vaccination_btn);
        addVaccinationBtn.setOnClickListener(v -> goToAddVaccineFragment());

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewVaccines);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Load and display vaccines
        loadVaccines();

        return view;
    }

    // Load vaccines from the database and display them
    private void loadVaccines() {
        if (pet == null) {
            Toast.makeText(getActivity(), "Pet information is unavailable.", Toast.LENGTH_SHORT).show();
            return; // Early exit if pet is null
        }

        // Fetch the list of vaccines
        updateVaccineList();
    }

    // Refresh the vaccines list when returning from other fragments
    @Override
    public void onResume() {
        super.onResume();
        // Refresh the vaccine list in case new vaccines were added
        updateVaccineList();
    }

    // Method to update the vaccine list in the adapter
    private void updateVaccineList() {
        if (pet == null) {
            Toast.makeText(getActivity(), "Pet information is unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the updated list of vaccines
        vaccineList = dbHelper.getVaccinesByPetId(pet.getId());

        // Check if the vaccine list is null or empty
        if (vaccineList == null || vaccineList.isEmpty()) {
            Toast.makeText(getActivity(), "No vaccines found for this pet.", Toast.LENGTH_SHORT).show();
        }

        // Initialize the adapter and set it to the RecyclerView if it's not already initialized
        if (vaccineAdapter == null) {
            vaccineAdapter = new VaccineAdapter(vaccineList, this); // Pass 'this' to handle item clicks
            recyclerView.setAdapter(vaccineAdapter);
        } else {
            // Update the existing adapter with the new list
            vaccineAdapter.updateVaccineList(vaccineList); // Ensure this method correctly updates the list
        }
    }

    // Handle vaccine item click event, receiving the position of the clicked item
    @Override
    public void onVaccineClick(int position) {
        // Get the vaccine object from the list using the position
        Vaccine vaccine = vaccineList.get(position);

        // Navigate to the VaccineDisplayFragment when a vaccine is clicked
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        VaccineDisplayFragment vaccineDisplayFragment = VaccineDisplayFragment.newInstance(vaccine, pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccineDisplayFragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack so the user can navigate back
        fragmentTransaction.commit();
    }

    // Navigate to AddVaccineFragment
    private void goToAddVaccineFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        AddVaccineFragment addVaccineFragment = AddVaccineFragment.newInstance(pet); // Pass the pet object to the add fragment
        fragmentTransaction.replace(R.id.fragment_container, addVaccineFragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack
        fragmentTransaction.commit();
    }

    // Navigate directly to ProfilePetFragment
    private void navigateToProfilePetFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        ProfilePetFragment profilePetFragment = ProfilePetFragment.newInstance(pet); // Ensure to pass the pet object
        fragmentTransaction.replace(R.id.fragment_container, profilePetFragment); // Replace current fragment with ProfilePetFragment
        fragmentTransaction.commit(); // Commit the transaction
    }
}
