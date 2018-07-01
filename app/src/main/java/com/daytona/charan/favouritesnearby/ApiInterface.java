package com.daytona.charan.favouritesnearby;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by anupamchugh on 29/03/17.
 */

public interface ApiInterface {

    @GET("place/nearbysearch/json?")
    Call<PlacesPOJO.Root> doPlaces(@Query(value = "location", encoded = true) String location,
            @Query(value = "type", encoded = true) String type,
            @Query(value = "radius", encoded = true) String radius,
//            @Query(value = "name", encoded = true) String name,
//            @Query(value = "opennow", encoded = true) boolean opennow,
//            @Query(value = "keyword", encoded = true) String rankby,
//            @Query(value = "rankby", encoded = true) String rankby,
            @Query(value = "key", encoded = true) String key);


    @GET("distancematrix/json") // origins/destinations:  LatLng as string
    Call<ResultDistanceMatrix> getDistance(@Query("key") String key, @Query("origins") String origins, @Query("destinations") String destinations);
}
