package com.colmcoughlan.colm.alchemy;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.colmcoughlan.colm.alchemy.model.Donation;
import com.colmcoughlan.colm.alchemy.repository.DonationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by colmc on 30/12/2018.
 */

public class DonationViewModel extends AndroidViewModel {
    private final DonationRepository donationRepository;

    public DonationViewModel(@NonNull Application application) {
        super(application);
        this.donationRepository = new DonationRepository(application);
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

    public void insertMap(Map<String, Integer> map) {
        final long timestamp = System.currentTimeMillis();
        List<Donation> donations = new ArrayList<>(map.size());

        for (String charity : map.keySet()) {
            Donation donation = new Donation();
            donation.setCharityName(charity);
            donation.setTotalDonation(map.get(charity));
            donation.setNumberOfDonations(1);
            donation.setFirstDonationTimestamp(timestamp);
            donation.setLastDonationTimestamp(timestamp);

            donations.add(donation);
        }

        donationRepository.insertAll(donations);
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
    }
}
