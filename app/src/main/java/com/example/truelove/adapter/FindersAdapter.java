package com.example.truelove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.custom_class.FinderDistance;
import com.example.truelove.custom_class.User;
import com.example.truelove.adapter.FindersViewHolders;

import java.util.List;

public class FindersAdapter extends RecyclerView.Adapter<FindersViewHolders>  {
    private List<FinderDistance> matchesList;
    private Context context;

    public FindersAdapter(List<FinderDistance> matchesList, Context context) {
        this.matchesList = matchesList;
        this.context = context;
    }


    @Override
    public FindersViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_finders, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        FindersViewHolders rcv = new FindersViewHolders((layoutView));
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull FindersViewHolders holder, int position) {
        holder.txtFindesAddress.setText(matchesList.get(position).getUser().getAddress());
        holder.txtMatchesName.setText(matchesList.get(position).getUser().getName());

        if(!matchesList.get(position).getUser().getImg().equals("default")) {
            Glide.with(context).load(matchesList.get(position).getUser().getImg()).into(holder.imageMatches);
        }
        holder.txtDistance.setText(String.valueOf(matchesList.get(position).getDistance())+" "+matchesList.get(position).getUnit());
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
