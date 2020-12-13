package com.colmcoughlan.colm.alchemy.repository;

import com.colmcoughlan.colm.alchemy.model.Donation;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Created by colmc on 30/12/2018.
 */

@Database(entities = {Donation.class}, version = 1, exportSchema = false)
public abstract class DonationDatabase extends RoomDatabase {
    public abstract DonationDao donationDao();
}
