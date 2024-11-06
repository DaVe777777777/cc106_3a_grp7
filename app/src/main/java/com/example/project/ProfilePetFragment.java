package com.example.project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfilePetFragment extends Fragment {

    private Pet pet; // The pet object to display
    private TextView tvName, tvGender, tvDob, tvHeight, tvWeight, tvBreed;
    private ImageView ivPetImage;
    private ImageView btnUpdate;
    private ImageView btnDelete; // Add this line

    public static ProfilePetFragment newInstance(Pet pet) {
        ProfilePetFragment fragment = new ProfilePetFragment();
        Bundle args = new Bundle();
        args.putSerializable("pet", pet); // Assuming Pet implements Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable("pet");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pet, container, false);

        // Initialize views
        tvName = view.findViewById(R.id.petNameTextView);
        tvGender = view.findViewById(R.id.petGenderTextView);
        tvDob = view.findViewById(R.id.petDobTextView);
        tvHeight = view.findViewById(R.id.petHeightTextView);
        tvWeight = view.findViewById(R.id.petWeightTextView);
        tvBreed = view.findViewById(R.id.petBreedTextView);
        ivPetImage = view.findViewById(R.id.petProfilePicture);
        btnUpdate = view.findViewById(R.id.updateIcon);
        btnDelete = view.findViewById(R.id.deleteIcon); // Initialize the delete icon

        Button btnVaccination = view.findViewById(R.id.btn_vaccination);
        Button backButton = view.findViewById(R.id.backButton); // Back button initialization

        // Populate fields with pet data
        populateFields();

        // Set click listener for the update button
        btnUpdate.setOnClickListener(v -> navigateToUpdatePetFragment());

        // Set click listener for the delete button
        btnDelete.setOnClickListener(v -> confirmDeletePet());

        // Set click listener for the vaccination button
        btnVaccination.setOnClickListener(v -> navigateToVaccinationFragment());

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            PetFragment petFragment = new PetFragment(); // Create a new instance of PetFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, petFragment) // Use your container ID here
                    .addToBackStack(null) // Optional: Add to back stack if you want to return to ProfilePetFragment
                    .commit();
        });

        return view;
    }

    private void populateFields() {
        if (pet != null) {
            tvName.setText(pet.getName());
            tvGender.setText(pet.getGender());
            tvDob.setText(pet.getDob());
            tvHeight.setText(String.valueOf(pet.getHeight()));
            tvWeight.setText(String.valueOf(pet.getWeight()));
            tvBreed.setText(pet.getBreed());

            // Load pet image
            String imageUri = pet.getImageUri();
            if (imageUri != null && !imageUri.isEmpty()) {
                Uri uri = Uri.parse(imageUri);
                loadImageFromUri(uri);
            } else {
                ivPetImage.setImageResource(R.drawable.ic_pet_placeholder); // Fallback if no image is set
            }
        }
    }

    private void loadImageFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ivPetImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ivPetImage.setImageResource(R.drawable.ic_pet_placeholder); // Fallback image if not found
        }
    }

    private void navigateToUpdatePetFragment() {
        Log.d("ProfilePetFragment", "Navigating to UpdatePetFragment");
        PetUpdateFragment updateFragment = PetUpdateFragment.newInstance(pet);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, updateFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToVaccinationFragment() {
        Log.d("ProfilePetFragment", "Navigating to VaccinationFragment");
        VaccinationFragment vaccinationFragment = VaccinationFragment.newInstance(pet);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, vaccinationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void confirmDeletePet() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Pet")
                .setMessage("Are you sure you want to delete this pet?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deletePet())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deletePet() {
        DBHelper dbHelper = new DBHelper(getContext());
        if (dbHelper.deletePetAndVaccinations(pet.getId())) {
            Toast.makeText(getContext(), "Pet deleted successfully", Toast.LENGTH_SHORT).show();
            // Navigate back to PetFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PetFragment())
                    .commit(); // No addToBackStack here to avoid returning to ProfilePetFragment
        } else {
            Toast.makeText(getContext(), "Error deleting pet", Toast.LENGTH_SHORT).show();
        }
    }


}
