package com.daytona.charan.favouritesnearby;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.daytona.charan.favouritesnearby.adapter.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private com.daytona.charan.favouritesnearby.ApiInterface apiService;
    private ArrayList<PlacesPOJO.CustomA> placesResults;
    private List<com.daytona.charan.favouritesnearby.StoreModel> storeModels;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        apiService = com.daytona.charan.favouritesnearby.APIClient.getClient().create(
                com.daytona.charan.favouritesnearby.ApiInterface.class);

        storeModels = new ArrayList<>();

        mProgressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent().getSerializableExtra("CURRENT_LOC") != null) {
            String current_loc = (String) getIntent().getSerializableExtra(
                    "CURRENT_LOC");

            if (getIntent().getSerializableExtra("PLACES_RES") != null) {
                placesResults = (ArrayList<PlacesPOJO.CustomA>) getIntent().getSerializableExtra(
                        "PLACES_RES");

                for (int i = 0; i < placesResults.size(); i++) {
                    PlacesPOJO.CustomA info = placesResults.get(i);
                    fetchDistance(current_loc, info);
                }
            }

        }

    }

    private void fetchDistance(String latLngString, final PlacesPOJO.CustomA info) {
        mProgressBar.setVisibility(View.VISIBLE);

        Call<ResultDistanceMatrix> call = apiService.getDistance(APIClient.GOOGLE_PLACE_API_KEY,
                latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng);
        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(Call<ResultDistanceMatrix> call,
                    Response<ResultDistanceMatrix> response) {

                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {

                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix =
                            resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement =
                            infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration =
                                distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance =
                                distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        String totalDuration = String.valueOf(itemDuration.text);

                        storeModels.add(new StoreModel(info.name, info.vicinity, totalDistance,
                                totalDuration));

                        if (storeModels.size() == placesResults.size()) {
                            mProgressBar.setVisibility(View.GONE);

                            RecyclerViewAdapter adapterStores = new RecyclerViewAdapter(
                                    placesResults, storeModels);
                            recyclerView.setAdapter(adapterStores);
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });

    }
}