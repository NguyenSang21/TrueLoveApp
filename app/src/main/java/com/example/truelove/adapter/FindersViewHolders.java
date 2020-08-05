package com.example.truelove.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;
import com.example.truelove.activities.ChatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindersViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtMatchesId, txtMatchesName;
    public CircleImageView imageMatches;


    public FindersViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        txtMatchesId = itemView.findViewById(R.id.txtMatchesId);
        txtMatchesName = itemView.findViewById(R.id.txtMatchesName);
        imageMatches = itemView.findViewById(R.id.imageMatches);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), " Finder click ", Toast.LENGTH_SHORT).show();
       /* Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", txtMatchesId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);*/
    }
}
