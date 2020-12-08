package com.example.hw9.model;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw9.R;

public final class ItemViewHolder extends RecyclerView.ViewHolder {

    public final View rootView;
    final TextView ticker;
    final TextView name;
    final TextView price;
    final TextView change;
    final ImageView changeArrow;
    final ImageButton gotoArrow;

    ItemViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        ticker = view.findViewById(R.id.ticker);
        name = view.findViewById(R.id.tickerName);
        price = view.findViewById(R.id.currentprice);
        change = view.findViewById(R.id.changeprice);
        changeArrow = view.findViewById(R.id.changeArrow);
        gotoArrow = view.findViewById(R.id.gotoArrow);
    }
}
