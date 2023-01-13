package com.example.marasigan.worksampler.views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.marasigan.worksampler.R;
import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Sample;

import java.util.ArrayList;
import java.util.List;

public class SchedRecyclerViewAdapter extends RecyclerView.Adapter<SchedRecyclerViewAdapter.MyViewHolder> {
//    private ArrayList<SchedData> schedDataList;
    private ArrayList<Sample> samplesList;

    public SchedRecyclerViewAdapter(ArrayList<Sample> samplesList){
       this.samplesList = samplesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ObjectActivityButton btnObject = (ObjectActivityButton) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_operator_activity_button, parent, false);
        btnObject.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new MyViewHolder(btnObject);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.getBtnObject().setSample(samplesList.get(position));

    }

    @Override
    public int getItemCount() {
        return samplesList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ObjectActivityButton btnObject;

        public MyViewHolder(View itemView) {
            super(itemView);
            btnObject = (ObjectActivityButton) itemView;
        }

        public ObjectActivityButton getBtnObject() {
            return btnObject;
        }
    }
}

