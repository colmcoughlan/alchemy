package com.colmcoughlan.colm.alchemy.repository;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.colmcoughlan.colm.alchemy.model.Donation;

/**
 * Created by colmc on 30/12/2018.
 */

@Database(entities = {Donation.class}, version = 1, exportSchema = false)
public abstract class DonationDatabase extends RoomDatabase {
    public abstract DonationDao donationDao();
}
