package com.example.truelove.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truelove.R;
import com.example.truelove.activities.ChatActivity;
import com.example.truelove.activities.ProfileFindersActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindersViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtFindesAddress, txtMatchesName, txtDistance,txtMatchesId;
    public CircleImageView imageMatches;


    public FindersViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        txtFindesAddress = itemView.findViewById(R.id.txtFindesAddress);
        txtMatchesName = itemView.findViewById(R.id.txtFindersName);
        imageMatches = itemView.findViewById(R.id.imageFinders);
        txtDistance=itemView.findViewById(R.id.txtFindersDistance);
        txtMatchesId=itemView.findViewById(R.id.txtFindesId);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), " Finder click ", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), ProfileFindersActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", txtMatchesId.getText().toString());
        intent.putExtras(b);
        ((Activity) view.getContext()).startActivityForResult(intent,1111);
    }
}
