package com.example.truelove.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.truelove.R;
import com.example.truelove.adapter.MatchesAdapter;
import com.example.truelove.custom_class.MatchesObject;

import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<MatchesObject> resultMatches = new ArrayList<MatchesObject>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(MatchesActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MatchesAdapter(getDatasetMatches(), MatchesActivity.this);
        recyclerView.setAdapter(mAdapter);

        MatchesObject obj = new MatchesObject("asd");
        resultMatches.add(obj);
        resultMatches.add(obj);
        resultMatches.add(obj);
        resultMatches.add(obj);
        resultMatches.add(obj);
        mAdapter.notifyDataSetChanged();
    }

    private List<MatchesObject> getDatasetMatches() {

        return resultMatches;
    }

}