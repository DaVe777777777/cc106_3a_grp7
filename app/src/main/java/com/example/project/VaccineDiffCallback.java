package com.example.project;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class VaccineDiffCallback extends DiffUtil.Callback {
    private final List<Vaccine> oldList;
    private final List<Vaccine> newList;

    public VaccineDiffCallback(List<Vaccine> oldList, List<Vaccine> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getVaccineId() == newList.get(newItemPosition).getVaccineId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
