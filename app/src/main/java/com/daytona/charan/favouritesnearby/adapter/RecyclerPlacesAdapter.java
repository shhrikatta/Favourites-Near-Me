package com.daytona.charan.favouritesnearby.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daytona.charan.favouritesnearby.PlacesPOJO;
import com.daytona.charan.favouritesnearby.R;
import com.daytona.charan.favouritesnearby.StoreModel;
import com.daytona.charan.favouritesnearby.data.Place;

import java.util.List;

/**
 * Created by anupamchugh on 01/03/17.
 */

public class RecyclerPlacesAdapter extends RecyclerView.Adapter<RecyclerPlacesAdapter.MyViewHolder> {
    private List<Place> placeList;
    ISelectedPlace mISelectedPlace;

    public RecyclerPlacesAdapter(List<Place> placeList, Context context) {
        this.placeList = placeList;
        mISelectedPlace = (ISelectedPlace) context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.places_list_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String name = placeList.get(position).getName();

        holder.setData(name, holder);

        holder.txtPlaceName.setOnClickListener(click -> {
            mISelectedPlace.onPlaceSelected(name);
        });
    }


    @Override
    public int getItemCount() {
        return Math.min(5, placeList.size());
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtPlaceName;

        MyViewHolder(View itemView) {
            super(itemView);
            this.txtPlaceName = itemView.findViewById(R.id.txtPlaceName);
        }


        void setData(String placeName, MyViewHolder holder) {
            holder.txtPlaceName.setText(placeName);
        }

    }

    public interface ISelectedPlace {
        void onPlaceSelected(String placeName);
    }
}