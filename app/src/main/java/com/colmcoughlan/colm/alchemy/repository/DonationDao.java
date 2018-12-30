package com.colmcoughlan.colm.alchemy.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.List;

/**
 * Created by colmc on 30/12/2018.
 */

@Dao
public interface DonationDao {
    @Insert
    void insertDonation(Donation donation);

    @Insert
    void insertAll(List<Donation> donation);

    @Update
    void updateDonation(Donation donation);

    @Query("SELECT * FROM Donation")
    LiveData<List<Donation>> getAllDonations();
}
