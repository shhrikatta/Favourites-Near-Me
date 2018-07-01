package com.daytona.charan.favouritesnearby.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Place.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaceDao mPlaceDao();
}