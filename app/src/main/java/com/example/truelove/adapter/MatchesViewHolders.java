package com.example.truelove.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtMatchesId;
    public MatchesViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        txtMatchesId = itemView.findViewById(R.id.txtMatchesId);

    }

    @Override
    public void onClick(View view) {

    }
}
