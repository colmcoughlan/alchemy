package com.colmcoughlan.colm.alchemy;

import android.app.Application;
import android.os.Bundle;

import com.colmcoughlan.colm.alchemy.model.Donation;
import com.colmcoughlan.colm.alchemy.repository.DonationRepository;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/**
 * Created by colmc on 30/12/2018.
 */

public class DonationViewModel extends AndroidViewModel {
    private final DonationRepository donationRepository;
    private final FirebaseAnalytics mFirebaseAnalytics;

    public DonationViewModel(@NonNull Application application) {
        super(application);
        this.donationRepository = new DonationRepository(application);
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(application);
    }

    public LiveData<List<Donation>> getAllDonations() {
        return donationRepository.getAllDonations();
    }

    private void insertNewDonation(String charity, double value) {
        final long timestamp = System.currentTimeMillis();

        Donation donation = new Donation();
        donation.setCharityName(charity);
        donation.setTotalDonation(value);
        donation.setNumberOfDonations(1);
        donation.setFirstDonationTimestamp(timestamp);
        donation.setLastDonationTimestamp(timestamp);

        donationRepository.insertDonation(donation);
    }

    public void recordDonation(List<Donation> donations, final String charity, final double value) {

        boolean insert = true;
        for (Donation donation : donations) {
            if (donation.getCharityName().equals(charity)) {
                donation.setTotalDonation(donation.getTotalDonation() + value);
                donation.setNumberOfDonations(donation.getNumberOfDonations() + 1);
                donation.setLastDonationTimestamp(System.currentTimeMillis());
                donationRepository.updateDonation(donation);
                insert = false;
                break;
            }
        }

        if (insert) {
            insertNewDonation(charity, value);
        }
        recordForAnalytics(charity, value);
    }

    public void recordForAnalytics(final String charity, final double value) {
        Bundle bundle = new Bundle();
        bundle.putString("charity", charity);
        bundle.putDouble("value", value);
        mFirebaseAnalytics.logEvent("DONATION", new Bundle());
    }
}
