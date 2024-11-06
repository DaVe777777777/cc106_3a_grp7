package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder> {

    private List<Vaccine> vaccineList;
    private OnVaccineClickListener onVaccineClickListener; // Interface for handling item clicks

    // Constructor
    public VaccineAdapter(List<Vaccine> vaccineList, OnVaccineClickListener listener) {
        this.vaccineList = vaccineList != null ? vaccineList : new ArrayList<>(); // Avoid null lists
        this.onVaccineClickListener = listener;
    }

    @NonNull
    @Override
    public VaccineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vaccine_item, parent, false);
        return new VaccineViewHolder(view, onVaccineClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineViewHolder holder, int position) {
        Vaccine vaccine = vaccineList.get(position);
        holder.bind(vaccine);
    }

    @Override
    public int getItemCount() {
        return vaccineList.size();
    }

    // Method to update the vaccine list using DiffUtil
    public void updateVaccineList(List<Vaccine> newVaccines) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new VaccineDiffCallback(vaccineList, newVaccines));
        vaccineList.clear();
        vaccineList.addAll(newVaccines);
        diffResult.dispatchUpdatesTo(this); // Notify the adapter about the changes
    }

    public static class VaccineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView vaccineNameText;
        private TextView drugNameText;
        private TextView vaccineDateText;
        private OnVaccineClickListener onVaccineClickListener;

        // ViewHolder constructor
        public VaccineViewHolder(@NonNull View itemView, OnVaccineClickListener listener) {
            super(itemView);
            vaccineNameText = itemView.findViewById(R.id.vaccineName);
            drugNameText = itemView.findViewById(R.id.drugName);
            vaccineDateText = itemView.findViewById(R.id.vaccineDate);
            this.onVaccineClickListener = listener;

            itemView.setOnClickListener(this); // Set click listener for the entire itemView
        }

        // Bind the vaccine data to the views
        public void bind(Vaccine vaccine) {
            vaccineNameText.setText(vaccine.getVaccineName());
            drugNameText.setText(vaccine.getDrugName());
            vaccineDateText.setText(vaccine.getVaccineDate());
        }

        @Override
        public void onClick(View v) {
            // Call the listener method when an item is clicked
            onVaccineClickListener.onVaccineClick(getAdapterPosition());
        }
    }

    // Interface to handle clicks
    public interface OnVaccineClickListener {
        void onVaccineClick(int position);
    }
}
