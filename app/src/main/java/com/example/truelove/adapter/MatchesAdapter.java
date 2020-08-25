package com.example.truelove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.custom_class.MatchesObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolders>  implements Filterable {
    private List<MatchesObject> matchesList;
    private List<MatchesObject> moviesListAll;
    private Context context;

    public MatchesAdapter(List<MatchesObject> matchesList, Context context) {
        this.matchesList = matchesList;
        moviesListAll = new ArrayList<>();
        moviesListAll.addAll(this.matchesList);
        this.context = context;
    }

    public void setListForAsch(List<MatchesObject> matchesList){
        this.matchesList=matchesList;
        moviesListAll.clear();
        moviesListAll.addAll(this.matchesList);
    }

    @Override
    public MatchesViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolders rcv = new MatchesViewHolders((layoutView));
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolders holder, int position) {
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

    @Override
    public Filter getFilter() {

        return myFilter;
    }

    Filter myFilter = new Filter() {

        //Automatic on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            List<MatchesObject> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(moviesListAll);
            } else {
                for (MatchesObject movie: moviesListAll) {
                    if (movie.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(movie);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        //Automatic on UI thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            matchesList.clear();
            matchesList.addAll((Collection<? extends MatchesObject>) filterResults.values);
            notifyDataSetChanged();
        }
    };


}
