package com.daytona.charan.favouritesnearby.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlaceDao   {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(Place places);

    @Query("SELECT * FROM place")
    List<Place> loadAllUsers();

    @Query("SELECT count(*) from place")
    int countPlace();
}