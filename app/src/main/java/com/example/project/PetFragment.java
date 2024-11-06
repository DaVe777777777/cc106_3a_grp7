package com.example.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PetFragment extends Fragment {

    private LinearLayout petListLayout;
    private DBHelper dbHelper;
    private ImageButton addPetButton;
    private String loggedInUsername;
    private List<View> petCardViews;

    // Flag to prevent multiple transitions
    private boolean isTransitioning = false;

    private final ActivityResultLauncher<Intent> addPetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    displayPets(loggedInUsername);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet, container, false);

        petListLayout = view.findViewById(R.id.petListLayout);
        addPetButton = view.findViewById(R.id.add_pet_button);
        dbHelper = new DBHelper(requireContext());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loggedInUsername = sharedPreferences.getString("loggedInUser", "");

        if (!loggedInUsername.isEmpty()) {
            displayPets(loggedInUsername);
        } else {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }

        // Add a click listener to the "Add Pet" button
        addPetButton.setOnClickListener(v -> {
            if (!isTransitioning) {
                isTransitioning = true; // Prevent further transitions
                goToAddPetFragment();  // Navigate to the AddPetFragment
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        enablePetCardClicks(false);  // Disable pet card clicks when fragment is paused
        if (addPetButton != null) {
            addPetButton.setClickable(false);  // Make button unclickable
            addPetButton.setEnabled(false);  // Disable button functionality
            addPetButton.setVisibility(View.GONE);  // Hide the button
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayPets(loggedInUsername);  // Refresh pet list on resume
        enablePetCardClicks(true);  // Re-enable pet card clicks when fragment resumes

        // Re-enable and show the button if transitioning is not in progress
        if (addPetButton != null) {
            addPetButton.setClickable(true);  // Make button clickable again
            addPetButton.setEnabled(true);  // Enable button functionality
            addPetButton.setVisibility(View.VISIBLE);  // Show the button
        }

        isTransitioning = false; // Reset the transitioning flag
    }

    // Method to enable or disable clicks on pet cards
    private void enablePetCardClicks(boolean enable) {
        if (petCardViews != null) {
            for (View card : petCardViews) {
                card.setClickable(enable);  // Enable or disable clicks
            }
        }
    }

    public void displayPets(String username) {
        List<Pet> petList = dbHelper.getAllPetsByUser(username);
        petListLayout.removeAllViews();
        petCardViews = new ArrayList<>();  // Initialize the pet card view list

        if (petList.isEmpty()) {
            TextView noPetsTextView = new TextView(requireContext());
            noPetsTextView.setText("No pets available.");
            petListLayout.addView(noPetsTextView);
            return;
        }

        // Reverse the pet list to display the newest pets first
        for (int i = petList.size() - 1; i >= 0; i--) {
            Pet pet = petList.get(i);
            View petCardView = LayoutInflater.from(requireContext()).inflate(R.layout.pet_card, petListLayout, false);
            TextView petNameTextView = petCardView.findViewById(R.id.petNameTextView);
            TextView petGenderTextView = petCardView.findViewById(R.id.petGenderTextView);
            ImageView petImageView = petCardView.findViewById(R.id.petImageView);

            petNameTextView.setText(pet.getName());
            petGenderTextView.setText("Gender: " + pet.getGender());

            String imageUri = pet.getImageUri();
            if (imageUri != null && !imageUri.isEmpty()) {
                Uri uri = Uri.parse(imageUri);
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    petImageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    petImageView.setImageResource(R.drawable.ic_pet_placeholder);
                }
            } else {
                petImageView.setImageResource(R.drawable.ic_pet_placeholder);
            }

            // Set a click listener for the pet card
            petCardView.setOnClickListener(v -> {
                // Disable further clicks when a pet is selected and navigating to ProfilePetFragment
                enablePetCardClicks(false);
                goToProfilePetFragment(pet);
            });

            // Add the pet card view at the top of the layout (index 0)
            petListLayout.addView(petCardView, 0);
            petCardViews.add(0, petCardView); // Add the card to the start of the list to control clickability
        }
    }


    private void goToProfilePetFragment(Pet pet) {
        ProfilePetFragment profilePetFragment = ProfilePetFragment.newInstance(pet);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profilePetFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }

    private void goToAddPetFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new AddPetFragment());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
