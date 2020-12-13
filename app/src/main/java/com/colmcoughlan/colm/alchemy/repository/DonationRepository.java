package com.colmcoughlan.colm.alchemy.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

/**
 * Created by colmc on 30/12/2018.
 */

public class DonationRepository {
    private DonationDao donationDao;
    private static final String DB_NAME = "db_donation";

    private LiveData<List<Donation>> allDonations;

    public DonationRepository(Context context) {
        DonationDatabase db = Room.databaseBuilder(context, DonationDatabase.class, DB_NAME).build();
        donationDao = db.donationDao();
        allDonations = donationDao.getAllDonations();
    }

    public LiveData<List<Donation>> getAllDonations() {
        return allDonations;
    }

    public void insertDonation(Donation donation) {
        new insertAsyncDonation(donationDao).execute(donation);
    }

    public void insertAll(List<Donation> donations) {
        new insertAsyncDonations(donationDao).execute(donations);
    }

    public void updateDonation(Donation donation) {
        new updateAsyncDonation(donationDao).execute(donation);
    }

    private static class insertAsyncDonations extends AsyncTask<List<Donation>, Void, Void> {

        private DonationDao mAsyncDonationDao;

        insertAsyncDonations(DonationDao dao) {
            mAsyncDonationDao = dao;
        }

        @Override
        protected Void doInBackground(final List<Donation>... params) {
            mAsyncDonationDao.insertAll(params[0]);
            return null;
        }
    }

    private static class insertAsyncDonation extends AsyncTask<Donation, Void, Void> {

        private DonationDao mAsyncDonationDao;

        insertAsyncDonation(DonationDao dao) {
            mAsyncDonationDao = dao;
        }

        @Override
        protected Void doInBackground(final Donation... params) {
            mAsyncDonationDao.insertDonation(params[0]);
            return null;
        }
    }

    private static class updateAsyncDonation extends AsyncTask<Donation, Void, Void> {

        private DonationDao mAsyncDonationDao;

        updateAsyncDonation(DonationDao dao) {
            mAsyncDonationDao = dao;
        }

        @Override
        protected Void doInBackground(final Donation... params) {
            mAsyncDonationDao.updateDonation(params[0]);
            return null;
        }
    }
}
