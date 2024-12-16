package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "register.db";
    private static final int DB_VERSION = 7;

    // User table columns
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // Pet table columns
    public static final String TABLE_PETS = "pets";
    public static final String COL_PET_ID = "pet_id"; // Primary key for pet
    public static final String COL_PET_NAME = "pet_name";
    public static final String COL_PET_GENDER = "pet_gender";
    public static final String COL_PET_DOB = "pet_dob";
    public static final String COL_PET_HEIGHT = "pet_height";
    public static final String COL_PET_WEIGHT = "pet_weight";
    public static final String COL_PET_BREED = "pet_breed";
    public static final String COL_PET_IMAGE_URI = "pet_image_uri";
    public static final String COL_PET_USER = "user"; // Foreign key to link with user

    // Vaccine table columns
    public static final String TABLE_VACCINES = "vaccines";
    public static final String COL_VACCINE_ID = "vaccine_id"; // Primary key for vaccine
    public static final String COL_VACCINE_NAME = "vaccine_name";
    public static final String COL_DRUG_NAME = "drug_name";
    public static final String COL_VACCINE_DATE = "vaccine_date";
    public static final String COL_VACCINE_TIME = "vaccine_time";
    public static final String COL_VACCINE_PET_ID = "vaccine_pet_id"; // Foreign key to pet
    public static final String COL_VETERINARIAN_NAME = "veterinarian_name";
    public static final String COL_CLINIC_PLACE = "clinic_place";
    public static final String COL_REPEAT_DAYS = "repeat_days"; // New column for clinic location

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION); // Use the updated version number
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create user table
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT , " +
                COL_PASSWORD + " TEXT );";
        sqLiteDatabase.execSQL(createUserTable);

        // Create pet table with new columns, including the image URI
        String createPetTable = "CREATE TABLE " + TABLE_PETS + " (" +
                COL_PET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PET_NAME + " TEXT, " +
                COL_PET_GENDER + " TEXT, " +
                COL_PET_DOB + " TEXT, " +
                COL_PET_HEIGHT + " REAL, " +
                COL_PET_WEIGHT + " REAL, " +
                COL_PET_BREED + " TEXT, " +
                COL_PET_IMAGE_URI + " TEXT, " +
                COL_PET_USER + " TEXT, " +
                "FOREIGN KEY(" + COL_PET_USER + ") REFERENCES " + TABLE_USERS + "(" + COL_USERNAME + ") ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(createPetTable);

        // Create vaccine table with new columns for vet name, clinic location, and vaccine time
        String createVaccineTable = "CREATE TABLE " + TABLE_VACCINES + " (" +
                COL_VACCINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_VACCINE_NAME + " TEXT, " +
                COL_DRUG_NAME + " TEXT, " +
                COL_VACCINE_DATE + " TEXT, " +
                COL_VACCINE_TIME + " TEXT, " + // Vaccine time
                COL_VACCINE_PET_ID + " INTEGER, " +
                COL_VETERINARIAN_NAME + " TEXT, " +
                COL_CLINIC_PLACE + " TEXT, " +
                COL_REPEAT_DAYS + " INTEGER, " + // New repeatDays column
                "FOREIGN KEY(" + COL_VACCINE_PET_ID + ") REFERENCES " + TABLE_PETS + "(" + COL_PET_ID + ") ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(createVaccineTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            // Upgrade logic for adding the 'repeat_days' column
            db.execSQL("ALTER TABLE " + TABLE_VACCINES + " ADD COLUMN " + COL_REPEAT_DAYS + " INTEGER DEFAULT 0");
        }
    }

    public int getTotalPetsForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM pets WHERE user = ?", new String[]{username});
        int totalPets = 0;
        if (cursor.moveToFirst()) {
            totalPets = cursor.getInt(0);
        }
        cursor.close();
        return totalPets;
    }

    public int getTotalVaccinesForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM vaccines v INNER JOIN pets p ON v.vaccine_pet_id = p.pet_id WHERE p.user = ?",
                new String[]{username});
        int totalVaccines = 0;
        if (cursor.moveToFirst()) {
            totalVaccines = cursor.getInt(0);
        }
        cursor.close();
        return totalVaccines;
    }

    public List<Pet> getPetsForUser(String username) {
        List<Pet> petList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM pets WHERE user = ?", new String[]{username});

        Log.d("DBHelper", "Number of rows fetched: " + cursor.getCount());

        if (cursor.moveToFirst()) {
            do {
                Pet pet = new Pet();
                pet.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PET_ID)));
                pet.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_NAME)));
                pet.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_IMAGE_URI)));

                Log.d("DBHelper", "Fetched pet: " + pet.getName() + ", Image URI: " + pet.getImageUri());
                petList.add(pet);
            } while (cursor.moveToNext());
        } else {
            Log.w("DBHelper", "No pets found for user: " + username);
        }

        cursor.close();
        db.close();
        return petList;
    }






    // Method to update a pet record
    public boolean updatePet(Pet pet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PET_NAME, pet.getName());
        values.put(COL_PET_GENDER, pet.getGender());
        values.put(COL_PET_DOB, pet.getDob());
        values.put(COL_PET_HEIGHT, pet.getHeight());
        values.put(COL_PET_WEIGHT, pet.getWeight());
        values.put(COL_PET_BREED, pet.getBreed());
        values.put(COL_PET_IMAGE_URI, pet.getImageUri()); // Optional, if you allow changing the image

        Log.d("DBHelper", "Updating pet: " + pet.getId());

        int rowsAffected = db.update(TABLE_PETS, values, COL_PET_ID + " = ?", new String[]{String.valueOf(pet.getId())});
        Log.d("DBHelper", "Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }




    // Method to update a vaccine record
    public boolean updateVaccine(Vaccine vaccine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VACCINE_NAME, vaccine.getVaccineName());
        values.put(COL_VACCINE_DATE, vaccine.getVaccineDate());
        values.put(COL_VACCINE_TIME, vaccine.getVaccineTime());
        values.put(COL_DRUG_NAME, vaccine.getDrugName());
        values.put(COL_VETERINARIAN_NAME, vaccine.getVetName());
        values.put(COL_CLINIC_PLACE, vaccine.getClinicLocation());
        values.put(COL_REPEAT_DAYS, vaccine.getRepeatDays()); // Update repeatDays field

        return db.update(TABLE_VACCINES, values, COL_VACCINE_ID + " = ?", new String[]{String.valueOf(vaccine.getVaccineId())}) > 0;
    }

    public boolean deleteVaccine(int vaccineId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_VACCINES, COL_VACCINE_ID + " = ?", new String[]{String.valueOf(vaccineId)}) > 0;
    }

    // Method to delete a pet and its associated vaccinations
    public boolean deletePetAndVaccinations(int petId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, delete all vaccinations for the pet
        int vaccinesDeleted = db.delete(TABLE_VACCINES, COL_VACCINE_PET_ID + " = ?", new String[]{String.valueOf(petId)});

        // Then, delete the pet
        int petDeleted = db.delete(TABLE_PETS, COL_PET_ID + " = ?", new String[]{String.valueOf(petId)});

        // Return true if both operations were successful
        return vaccinesDeleted >= 0 && petDeleted > 0;
    }


    // Insert user data
    public boolean insertUser(String username, String password) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        long result = myDB.insert(TABLE_USERS, null, contentValues);
        return result != -1; // Return true if insertion is successful
    }

    // Insert pet data linked to a user, with the image URI included
    public boolean insertPet(String petName, String petGender, String dob, double height, double weight, String breed, String imageUri, String username) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PET_NAME, petName);
        contentValues.put(COL_PET_GENDER, petGender);
        contentValues.put(COL_PET_DOB, dob);
        contentValues.put(COL_PET_HEIGHT, height);
        contentValues.put(COL_PET_WEIGHT, weight);
        contentValues.put(COL_PET_BREED, breed);
        contentValues.put(COL_PET_IMAGE_URI, imageUri);
        contentValues.put(COL_PET_USER, username); // Link the pet to a user
        long result = myDB.insert(TABLE_PETS, null, contentValues);
        return result != -1; // Return true if insertion is successful
    }

    // Insert vaccine record for a pet
    public boolean insertVaccine(String vaccineName, String drugName, String vaccineDate, String vaccineTime, int petId, String veterinarianName, String clinicPlace, int repeatDays) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_VACCINE_NAME, vaccineName);
        contentValues.put(COL_DRUG_NAME, drugName);
        contentValues.put(COL_VACCINE_DATE, vaccineDate);
        contentValues.put(COL_VACCINE_TIME, vaccineTime);
        contentValues.put(COL_VACCINE_PET_ID, petId);
        contentValues.put(COL_VETERINARIAN_NAME, veterinarianName);
        contentValues.put(COL_CLINIC_PLACE, clinicPlace);
        contentValues.put(COL_REPEAT_DAYS, repeatDays); // Insert repeatDays value
        long result = myDB.insert(TABLE_VACCINES, null, contentValues);
        // Log the insertion result
        Log.d("DBHelper", "Inserted vaccine: " + vaccineName + ", Result: " + result);
        return result != -1; // Return true if insertion is successful
    }



    public Integer getUserId(String loggedInUser) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=?", new String[]{loggedInUser}, null, null, null);

        Log.d("DBHelper", "Query executed for user: " + loggedInUser); // Add this line

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                    Log.d("DBHelper", "User ID retrieved: " + userId);
                    return userId;
                }
            } finally {
                cursor.close();
            }
        }
        Log.d("DBHelper", "No user found with username: " + loggedInUser);
        return null;
    }



    public List<Pet> getAllPetsWithBirthdayToday(String username) {
        List<Pet> petsWithBirthdayToday = new ArrayList<>();

        // Get today's month and day (MM-dd)
        String today = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();

        // Use try-with-resources to automatically close the cursor
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM pets p INNER JOIN users u ON p.user = u.username " +
                        "WHERE strftime('%m-%d', p.pet_dob) = ? AND LOWER(u.username) = LOWER(?)",
                new String[]{today, username.toLowerCase()} // Ensure case-insensitive matching of the username
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Create a new Pet object and set its properties
                    Pet pet = new Pet();
                    pet.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PET_ID)));
                    pet.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_NAME)));
                    pet.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_GENDER)));
                    pet.setDob(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_DOB)));
                    pet.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PET_HEIGHT)));
                    pet.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PET_WEIGHT)));
                    pet.setBreed(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_BREED)));
                    pet.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_IMAGE_URI)));
                    pet.setUser(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_USER)));

                    petsWithBirthdayToday.add(pet);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving pets with birthday today", e);
        }

        Log.d("DBHelper", "Number of pets with birthday today: " + petsWithBirthdayToday.size());
        return petsWithBirthdayToday;
    }



    // Method to get vaccines due today
    public List<Vaccine> getVaccinesDueToday(String username) {
        List<Vaccine> vaccinesDueToday = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM vaccines v " +
                        "INNER JOIN pets p ON v.vaccine_pet_id = p.pet_id " +
                        "WHERE (v.vaccine_date = ? OR (v.repeat_days > 0 AND strftime('%Y-%m-%d', v.vaccine_date) <= ?)) " +
                        "AND p.user = ?",
                new String[]{today, today, username});  // Check vaccines with today or within repeat range

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Add vaccines to the list
                Vaccine vaccine = new Vaccine();
                vaccine.setVaccineId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_VACCINE_ID)));
                vaccine.setVaccineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_VACCINE_NAME)));
                vaccine.setDrugName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DRUG_NAME)));
                vaccine.setVaccineDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_VACCINE_DATE)));
                vaccine.setVaccineTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_VACCINE_TIME)));
                vaccine.setVetName(cursor.getString(cursor.getColumnIndexOrThrow(COL_VETERINARIAN_NAME)));
                vaccine.setClinicLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_CLINIC_PLACE)));
                vaccine.setPetId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_VACCINE_PET_ID)));
                vaccine.setRepeatDays(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPEAT_DAYS)));  // Add repeatDays from DB

                // If repeatDays is greater than 0, check if the vaccine is due based on last date and repeat interval
                if (vaccine.getRepeatDays() > 0) {
                    String lastVaccineDate = vaccine.getVaccineDate();
                    if (shouldVaccineBeDue(lastVaccineDate, vaccine.getRepeatDays())) {
                        vaccinesDueToday.add(vaccine);  // Add repeat due vaccines
                    }
                } else if (vaccine.getVaccineDate().equals(today)) {
                    // If no repeatDays, just check if the vaccine is due today
                    vaccinesDueToday.add(vaccine);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d("DBHelper", "Number of vaccines due today: " + vaccinesDueToday.size());
        return vaccinesDueToday;
    }

    // Helper method to determine if the vaccine is due based on the repeat days
    private boolean shouldVaccineBeDue(String lastVaccineDate, int repeatDays) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastDate = dateFormat.parse(lastVaccineDate);
            long diffInMillis = System.currentTimeMillis() - lastDate.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24); // Convert millis to days
            return diffInDays >= repeatDays;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Method to get a pet by its ID
    public Pet getPetById(int petId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Pet pet = null; // Initialize the Pet object

        // Query to select a pet by its ID
        String query = "SELECT * FROM " + TABLE_PETS + " WHERE " + COL_PET_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(petId)});

        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Create a new Pet object and set its properties
                pet = new Pet();
                pet.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PET_ID)));
                pet.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_NAME)));
                pet.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_GENDER)));
                pet.setDob(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_DOB)));
                pet.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PET_HEIGHT)));
                pet.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PET_WEIGHT)));
                pet.setBreed(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_BREED)));
                pet.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_IMAGE_URI)));
                pet.setUser(cursor.getString(cursor.getColumnIndexOrThrow(COL_PET_USER)));
            } finally {
                cursor.close();
            }
        }

        return pet; // Return the Pet object, or null if not found
    }




    // Get all vaccines for a specific pet
    public List<Vaccine> getVaccinesByPetId(int petId) {
        List<Vaccine> vaccineList = new ArrayList<>();
        SQLiteDatabase myDB = null;
        Cursor cursor = null;

        try {
            myDB = this.getReadableDatabase();
            // Query to get vaccines ordered by vaccine ID in descending order
            String query = "SELECT * FROM " + TABLE_VACCINES +
                    " WHERE " + COL_VACCINE_PET_ID + "=? " +
                    " ORDER BY " + COL_VACCINE_ID + " DESC";
            cursor = myDB.rawQuery(query, new String[]{String.valueOf(petId)});

            // Iterate through the cursor to extract vaccine data
            while (cursor.moveToNext()) {
                int vaccineIdIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_ID);
                int vaccineNameIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_NAME);
                int drugNameIndex = cursor.getColumnIndexOrThrow(COL_DRUG_NAME);
                int vaccineDateIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_DATE);
                int vaccineTimeIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_TIME);
                int vetNameIndex = cursor.getColumnIndexOrThrow(COL_VETERINARIAN_NAME);
                int clinicLocationIndex = cursor.getColumnIndexOrThrow(COL_CLINIC_PLACE);
                int repeatDaysIndex = cursor.getColumnIndexOrThrow(COL_REPEAT_DAYS);

                int vaccineId = cursor.getInt(vaccineIdIndex);
                String vaccineName = cursor.getString(vaccineNameIndex);
                String drugName = cursor.getString(drugNameIndex);
                String vaccineDate = cursor.getString(vaccineDateIndex);
                String vaccineTime = cursor.getString(vaccineTimeIndex);
                String vetName = cursor.getString(vetNameIndex);
                String clinicLocation = cursor.getString(clinicLocationIndex);
                int repeatDays = cursor.getInt(repeatDaysIndex); // Convert to int

                // Create a new Vaccine object and add it to the list
                vaccineList.add(new Vaccine(vaccineId, vaccineName, drugName, vaccineDate, vaccineTime, petId, vetName, clinicLocation, repeatDays));
            }

            Log.d("DBHelper", "Number of vaccines for pet " + petId + ": " + vaccineList.size());

        } catch (Exception e) {
            Log.e("DBHelper", "Error retrieving vaccines: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Close the cursor
            }
            if (myDB != null && myDB.isOpen()) {
                myDB.close(); // Close the database
            }
        }

        return vaccineList;
    }


    // Get all pets for a specific user and return them as Pet objects
    public List<Pet> getAllPetsByUser(String username) {
        List<Pet> petList = new ArrayList<>();
        SQLiteDatabase myDB = this.getReadableDatabase();

        // Modified query to order pets by pet_id in descending order (newest first)
        String query = "SELECT * FROM " + TABLE_PETS + " WHERE " + COL_PET_USER + "=? ORDER BY " + COL_PET_ID + " DESC";
        Cursor cursor = myDB.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            do {
                int petIdIndex = cursor.getColumnIndexOrThrow(COL_PET_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COL_PET_NAME);
                int genderIndex = cursor.getColumnIndexOrThrow(COL_PET_GENDER);
                int dobIndex = cursor.getColumnIndexOrThrow(COL_PET_DOB);
                int heightIndex = cursor.getColumnIndexOrThrow(COL_PET_HEIGHT);
                int weightIndex = cursor.getColumnIndexOrThrow(COL_PET_WEIGHT);
                int breedIndex = cursor.getColumnIndexOrThrow(COL_PET_BREED);
                int imageUriIndex = cursor.getColumnIndexOrThrow(COL_PET_IMAGE_URI);

                int petId = cursor.getInt(petIdIndex);
                String petName = cursor.getString(nameIndex);
                String petGender = cursor.getString(genderIndex);
                String petDob = cursor.getString(dobIndex);
                double petHeight = cursor.getDouble(heightIndex);
                double petWeight = cursor.getDouble(weightIndex);
                String petBreed = cursor.getString(breedIndex);
                String petImageUri = cursor.getString(imageUriIndex);

                // Create a new Pet object and add it to the list
                petList.add(new Pet(petId, petName, petGender, petDob, petHeight, petWeight, petBreed, petImageUri, username));
            } while (cursor.moveToNext());
        }

        cursor.close(); // Close the cursor
        return petList;
    }





    // Check if a username exists
    public boolean checkUsername(String username) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = myDB.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Check if the username and password match
    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = myDB.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Update username
    public boolean updateUsername(String oldUsername, String newUsername) {
        if (checkUsername(newUsername)) {
            return false;
        }
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, newUsername);
        return myDB.update(TABLE_USERS, contentValues, COL_USERNAME + "=?", new String[]{oldUsername}) > 0;
    }

    // Update password
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);
        return myDB.update(TABLE_USERS, contentValues, COL_USERNAME + "=?", new String[]{username}) > 0;
    }
}
