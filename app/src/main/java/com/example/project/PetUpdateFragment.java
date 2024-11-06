package com.example.project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class PetUpdateFragment extends Fragment {

    private static final String ARG_PET = "pet";
    private Pet pet;

    private EditText petNameInput, petDobInput, petHeightInput, petWeightInput, petBreedInput;
    private Spinner petGenderInput;
    private ImageView petPicture;
    private Button saveButton, cancelButton;

    public static PetUpdateFragment newInstance(Pet pet) {
        PetUpdateFragment fragment = new PetUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PET, pet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable(ARG_PET);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_update, container, false);

        // Initialize UI elements
        petNameInput = view.findViewById(R.id.petNameInput);
        petDobInput = view.findViewById(R.id.petDobInput);
        petGenderInput = view.findViewById(R.id.petGenderInput);
        petHeightInput = view.findViewById(R.id.petHeightInput);
        petWeightInput = view.findViewById(R.id.petWeightInput);
        petBreedInput = view.findViewById(R.id.petBreedInput);
        petPicture = view.findViewById(R.id.petPicture);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Load existing pet data into the views
        loadPetData();

        // Set up button listeners
        saveButton.setOnClickListener(v -> updatePet());
        cancelButton.setOnClickListener(v -> goBackToProfile());

        // Set up image picker
        petPicture.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void loadPetData() {
        if (pet != null) {
            petNameInput.setText(pet.getName());
            petDobInput.setText(pet.getDob());
            petHeightInput.setText(String.valueOf(pet.getHeight()));
            petWeightInput.setText(String.valueOf(pet.getWeight()));
            petBreedInput.setText(pet.getBreed());

            // Load gender into spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.gender_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            petGenderInput.setAdapter(adapter);
            int genderPosition = adapter.getPosition(pet.getGender());
            petGenderInput.setSelection(genderPosition);

            // Load pet picture
            setPetProfilePicture(petPicture, pet.getImageUri());
        }
    }

    private void setPetProfilePicture(ImageView imageView, String imageUri) {
        if (imageUri != null && !imageUri.isEmpty()) {
            Uri uri = Uri.parse(imageUri);
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.ic_pet_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_pet_placeholder);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1); // Request code 1
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Set the image to the ImageView
                petPicture.setImageURI(selectedImageUri);
                // Optionally, update the pet object with the new image URI
                pet.setImageUri(selectedImageUri.toString());
            }
        }
    }

    private void updatePet() {
        // Get updated values
        String name = petNameInput.getText().toString().trim();
        String dob = petDobInput.getText().toString().trim();
        String gender = petGenderInput.getSelectedItem().toString();
        String heightStr = petHeightInput.getText().toString().trim();
        String weightStr = petWeightInput.getText().toString().trim();
        String breed = petBreedInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dob.isEmpty()) {
            Toast.makeText(getContext(), "Date of birth cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (heightStr.isEmpty()) {
            Toast.makeText(getContext(), "Height cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (weightStr.isEmpty()) {
            Toast.makeText(getContext(), "Weight cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breed.isEmpty()) {
            Toast.makeText(getContext(), "Breed cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert height and weight to double
        double height = Double.parseDouble(heightStr);
        double weight = Double.parseDouble(weightStr);

        // Update pet object
        pet.setName(name);
        pet.setDob(dob);
        pet.setGender(gender);
        pet.setHeight(height);
        pet.setWeight(weight);
        pet.setBreed(breed);

        // Save updated pet to the database
        DBHelper dbHelper = new DBHelper(getContext());
        boolean success = dbHelper.updatePet(pet);  // Make sure you have an updatePet method in DBHelper

        if (success) {
            Toast.makeText(getContext(), "Pet updated successfully!", Toast.LENGTH_SHORT).show();
            // Schedule notification for pet birthday
            scheduleBirthdayNotification(pet);
        } else {
            Toast.makeText(getContext(), "Failed to update pet.", Toast.LENGTH_SHORT).show();
        }

        // Go back to the profile fragment or any other relevant fragment
        goBackToProfile();
    }

    private void scheduleBirthdayNotification(Pet pet) {
        // Set up the notification
        Calendar calendar = Calendar.getInstance();
        String[] dateParts = pet.getDob().split("-");
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)); // Set to this year
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Month is 0-indexed
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.HOUR_OF_DAY, 9); // Set the time for notification
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Create the intent for the notification
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("message", "It's " + pet.getName() + "'s birthday today!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), pet.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Schedule the notification
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    private void goBackToProfile() {
        // Logic to go back to the previous fragment
        getParentFragmentManager().popBackStack();
    }
}
