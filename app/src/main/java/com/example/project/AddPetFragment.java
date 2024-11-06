package com.example.project;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Calendar;

public class AddPetFragment extends Fragment {

    private static final int SELECT_PICTURE = 1;
    private EditText petNameInput;
    private Spinner petGenderInput;
    private EditText petDobInput;
    private EditText petHeightInput;
    private EditText petWeightInput;
    private EditText petBreedInput;
    private ImageView petPicture; // ImageView for pet picture
    private Button saveButton;
    private Button cancelButton;
    private Uri selectedImageUri; // URI for the selected image
    private DBHelper dbHelper;
    private String loggedInUsername;
    private Calendar calendar = Calendar.getInstance(); // Calendar instance for the selected date

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_pet, container, false);

        // Initialize UI components
        petNameInput = view.findViewById(R.id.petNameInput);
        petGenderInput = view.findViewById(R.id.petGenderInput);
        petDobInput = view.findViewById(R.id.petDobInput);
        petHeightInput = view.findViewById(R.id.petHeightInput);
        petWeightInput = view.findViewById(R.id.petWeightInput);
        petBreedInput = view.findViewById(R.id.petBreedInput);
        petPicture = view.findViewById(R.id.petPicture); // Initialize ImageView
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        dbHelper = new DBHelper(getContext());

        // Retrieve the logged-in username from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        loggedInUsername = sharedPreferences.getString("loggedInUser", ""); // Default to empty if not found

        // Set up the DatePickerDialog for DOB
        petDobInput.setOnClickListener(v -> showDatePickerDialog());

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                            petPicture.setImageURI(selectedImageUri); // Display selected image
                        }
                    }
                }
        );

        // Set up click listener to select a pet picture
        petPicture.setOnClickListener(v -> openImageSelector());

        // Set up the save button click listener
        saveButton.setOnClickListener(v -> {
            // Create a new Pet object
            Pet newPet = new Pet();

            // Set the properties of the Pet object using user input
            newPet.setName(petNameInput.getText().toString());
            newPet.setGender(petGenderInput.getSelectedItem().toString());
            newPet.setDob(petDobInput.getText().toString());
            newPet.setHeight(Double.parseDouble(petHeightInput.getText().toString()));
            newPet.setWeight(Double.parseDouble(petWeightInput.getText().toString()));
            newPet.setBreed(petBreedInput.getText().toString());
            newPet.setImageUri(selectedImageUri != null ? selectedImageUri.toString() : "");
            newPet.setUser(loggedInUsername); // Set the user from the logged-in username

            // Validate inputs
            if (!newPet.getName().isEmpty() && !newPet.getDob().isEmpty() && newPet.getHeight() > 0 && newPet.getWeight() > 0 && !newPet.getBreed().isEmpty()) {
                // Insert the pet into the database
                boolean isInserted = dbHelper.insertPet(
                        newPet.getName(),
                        newPet.getGender(),
                        newPet.getDob(),
                        newPet.getHeight(),
                        newPet.getWeight(),
                        newPet.getBreed(),
                        newPet.getImageUri(),
                        newPet.getUser() // Use getter to get user
                );

                if (isInserted) {
                    Toast.makeText(getContext(), "Pet added successfully", Toast.LENGTH_SHORT).show();

                    // Schedule birthday notification
                    scheduleBirthdayNotification(newPet.getName(), newPet.getDob());

                    // Navigate explicitly to PetFragment
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new PetFragment())  // Replace with PetFragment
                            .commit();  // Commit the transaction

                } else {
                    Toast.makeText(getContext(), "Failed to add pet", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the cancel button click listener
        cancelButton.setOnClickListener(v -> {
            // Navigate directly to PetFragment
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PetFragment())  // Replace with PetFragment
                    .commit();  // Commit the transaction
        });

        return view;
    }

    // Method to show DatePickerDialog
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date as YYYY-MM-DD
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay; // Month is 0-based
                    petDobInput.setText(date);
                    // Set calendar instance for the selected date
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                },
                year, month, day);

        datePickerDialog.show();
    }

    // Method to open the image selector
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent); // Launch the image picker using ActivityResultLauncher
    }

    // Schedule a notification for the pet's birthday
    private void scheduleBirthdayNotification(String petName, String petDob) {
        Calendar birthdayCalendar = Calendar.getInstance(); // Create a new instance here
        Log.d("AddPetFragment", "Scheduling birthday notification for: " + petName + " on " + petDob);

        // Parse the birthday date to set the calendar
        String[] dateParts = petDob.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
        int day = Integer.parseInt(dateParts[2]);
        birthdayCalendar.set(year, month, day);

        // Check if the birthday is today
        Calendar currentCalendar = Calendar.getInstance();
        if (birthdayCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                birthdayCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                birthdayCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)) {
            // If today is the birthday, schedule for the current time
            long birthdayTime = currentCalendar.getTimeInMillis();
            scheduleNotification(petName, birthdayTime);
        } else if (birthdayCalendar.before(currentCalendar)) {
            // If the birthday is in the past, set to next year's birthday
            birthdayCalendar.add(Calendar.YEAR, 1);
            long birthdayTime = birthdayCalendar.getTimeInMillis();
            scheduleNotification(petName, birthdayTime);
        } else {
            // If the birthday is in the future, just use that time
            long birthdayTime = birthdayCalendar.getTimeInMillis();
            scheduleNotification(petName, birthdayTime);
        }
    }

    // Method to schedule the notification
    // Method to schedule the notification
    private void scheduleNotification(String petName, long birthdayTime) {
        // Create an intent to trigger the NotificationReceiver
        Log.d("AddPetFragment", "Notification set for: " + petName + " at " + birthdayTime);
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        intent.putExtra("notificationTitle", "Pet Birthday Reminder");
        intent.putExtra("notificationText", "It's " + petName + "'s birthday today!");

        // Create a PendingIntent with the intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE here
        );

        // Schedule the notification with AlarmManager
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, birthdayTime, pendingIntent);
        }
    }

}
