package com.colmcoughlan.colm.alchemy.repository;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
