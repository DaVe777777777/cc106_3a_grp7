package com.example.project;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomePetAdapter extends RecyclerView.Adapter<HomePetAdapter.HomePetViewHolder> {

    private Context context;
    private List<Pet> petList;
    private OnPetClickListener listener;

    public HomePetAdapter(Context context, List<Pet> petList, OnPetClickListener listener) {
        this.context = context;
        this.petList = petList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomePetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_pet_card, parent, false);
        return new HomePetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomePetViewHolder holder, int position) {
        Pet pet = petList.get(position);

        // Set pet name
        holder.petName.setText(pet.getName() != null ? pet.getName() : "Unnamed Pet");

        // Set pet image or placeholder
        if (pet.getImageUri() != null && !pet.getImageUri().isEmpty()) {
            holder.petImage.setImageURI(Uri.parse(pet.getImageUri()));
        } else {
            holder.petImage.setImageResource(R.drawable.ic_pet_placeholder); // Fallback image
        }

    }


    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class HomePetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImage;
        TextView petName;

        public HomePetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.homePetImageView);
            petName = itemView.findViewById(R.id.homePetNameTextView);
        }
    }

    // Interface for handling pet clicks
    public interface OnPetClickListener {
        void onPetClick(Pet pet);
    }
}
