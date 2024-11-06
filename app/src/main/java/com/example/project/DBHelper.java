package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "register.db";
    private static final int DB_VERSION = 6; // Updated version number for new fields

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
    public static final String COL_VACCINE_TIME = "vaccine_time"; // New column for vaccine time
    public static final String COL_VACCINE_PET_ID = "vaccine_pet_id"; // Foreign key to pet
    public static final String COL_VETERINARIAN_NAME = "veterinarian_name"; // New column for veterinarian name
    public static final String COL_CLINIC_PLACE = "clinic_place"; // New column for clinic location

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
                COL_VACCINE_TIME + " TEXT, " + // New column for vaccine time
                COL_VACCINE_PET_ID + " INTEGER, " +
                COL_VETERINARIAN_NAME + " TEXT, " + // New column for veterinarian name
                COL_CLINIC_PLACE + " TEXT, " + // New column for clinic location
                "FOREIGN KEY(" + COL_VACCINE_PET_ID + ") REFERENCES " + TABLE_PETS + "(" + COL_PET_ID + ") ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(createVaccineTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            // Upgrade logic for adding new columns and maintaining data integrity
            db.execSQL("ALTER TABLE " + TABLE_VACCINES + " ADD COLUMN " + COL_VETERINARIAN_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_VACCINES + " ADD COLUMN " + COL_CLINIC_PLACE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_VACCINES + " ADD COLUMN " + COL_VACCINE_TIME + " TEXT");
        }
        // Add more upgrade logic if there are new future updates.
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

        // Updating row
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
    public boolean insertVaccine(String vaccineName, String drugName, String vaccineDate, String vaccineTime, int petId, String veterinarianName, String clinicPlace) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_VACCINE_NAME, vaccineName);
        contentValues.put(COL_DRUG_NAME, drugName);
        contentValues.put(COL_VACCINE_DATE, vaccineDate);
        contentValues.put(COL_VACCINE_TIME, vaccineTime); // Insert vaccine time
        contentValues.put(COL_VACCINE_PET_ID, petId); // Link the vaccine to a pet
        contentValues.put(COL_VETERINARIAN_NAME, veterinarianName); // Insert veterinarian name
        contentValues.put(COL_CLINIC_PLACE, clinicPlace); // Insert clinic location
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
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.rawQuery(
                "SELECT * FROM pets p INNER JOIN users u ON p.user = u.user_id WHERE strftime('%Y-%m-%d', p.pet_dob) = ? AND u.username = ?",
                new String[]{today, username}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Add pets to the list
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

        if (cursor != null) {
            cursor.close();
        }

        Log.d("DBHelper", "Number of pets with birthday today: " + petsWithBirthdayToday.size());
        return petsWithBirthdayToday;
    }





    // Method to get vaccines due today
    public List<Vaccine> getVaccinesDueToday(String username) {
        List<Vaccine> vaccinesDueToday = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM vaccines v INNER JOIN pets p ON v.vaccine_pet_id = p.pet_id WHERE v.vaccine_date = ? AND p.user = ?", new String[]{today, username});

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

                vaccinesDueToday.add(vaccine);
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.d("DBHelper", "Number of vaccines due today: " + vaccinesDueToday.size());
        return vaccinesDueToday;
    }





    // Get all vaccines for a specific pet
    public List<Vaccine> getVaccinesByPetId(int petId) {
        List<Vaccine> vaccineList = new ArrayList<>();
        SQLiteDatabase myDB = null;
        Cursor cursor = null; // Declare cursor outside of try

        try {
            myDB = this.getReadableDatabase(); // Open the database
            // Order by vaccine ID in descending order to get the newest vaccines on top
            String query = "SELECT * FROM " + TABLE_VACCINES +
                    " WHERE " + COL_VACCINE_PET_ID + "=? " +
                    " ORDER BY " + COL_VACCINE_ID + " DESC"; // Order by vaccine ID in descending order
            cursor = myDB.rawQuery(query, new String[]{String.valueOf(petId)});

            // Iterate through the cursor
            while (cursor.moveToNext()) {
                int vaccineIdIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_ID);
                int vaccineNameIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_NAME);
                int drugNameIndex = cursor.getColumnIndexOrThrow(COL_DRUG_NAME);
                int vaccineDateIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_DATE);
                int vaccineTimeIndex = cursor.getColumnIndexOrThrow(COL_VACCINE_TIME); // New index for vaccine time
                int vetNameIndex = cursor.getColumnIndexOrThrow(COL_VETERINARIAN_NAME);
                int clinicLocationIndex = cursor.getColumnIndexOrThrow(COL_CLINIC_PLACE);

                int vaccineId = cursor.getInt(vaccineIdIndex);
                String vaccineName = cursor.getString(vaccineNameIndex);
                String drugName = cursor.getString(drugNameIndex);
                String vaccineDate = cursor.getString(vaccineDateIndex);
                String vaccineTime = cursor.getString(vaccineTimeIndex); // Retrieve vaccine time
                String vetName = cursor.getString(vetNameIndex);
                String clinicLocation = cursor.getString(clinicLocationIndex);

                // Create a new Vaccine object and add it to the list
                vaccineList.add(new Vaccine(vaccineId, vaccineName, drugName, vaccineDate, vaccineTime, petId, vetName, clinicLocation)); // Pass vaccine time to the Vaccine constructor
            }

            Log.d("DBHelper", "Number of vaccines for pet " + petId + ": " + vaccineList.size()); // Log the size of the list

        } catch (Exception e) {
            // Log the exception for debugging, including stack trace
            Log.e("DBHelper", "Error retrieving vaccines: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure the cursor is closed
            }
            if (myDB != null && myDB.isOpen()) {
                myDB.close(); // Ensure the database is closed
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
