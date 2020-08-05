package com.example.truelove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.custom_class.User;
import com.example.truelove.adapter.FindersViewHolders;

import java.util.List;

public class FindersAdapter extends RecyclerView.Adapter<FindersViewHolders>  {
    private List<User> matchesList;
    private Context context;

    public FindersAdapter(List<User> matchesList, Context context) {
        this.matchesList = matchesList;
        this.context = context;
    }


    @Override
    public FindersViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolders rcv = new MatchesViewHolders((layoutView));
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull FindersViewHolders holder, int position) {
        holder.txtMatchesId.setText(matchesList.get(position).getUserId());
        holder.txtMatchesName.setText(matchesList.get(position).getName());

        if(!matchesList.get(position).getProfileImageUrl().equals("default")) {
            Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.imageMatches);
        }

    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
