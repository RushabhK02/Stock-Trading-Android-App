package com.example.hw9.model;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw9.R;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView section;
    HeaderViewHolder(@NonNull View view) {
        super(view);
        section = view.findViewById(R.id.sectionTitle);
    }
}

