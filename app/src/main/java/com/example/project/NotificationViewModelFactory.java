
package com.example.project;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.project.DBHelper;

public class NotificationViewModelFactory implements ViewModelProvider.Factory {
    private final DBHelper dbHelper;
    private final String currentUsername;

    public NotificationViewModelFactory(DBHelper dbHelper, String currentUsername) {
        this.dbHelper = dbHelper;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
            return (T) new NotificationViewModel(dbHelper, currentUsername);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
