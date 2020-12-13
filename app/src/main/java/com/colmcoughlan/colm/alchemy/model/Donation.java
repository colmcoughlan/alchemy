package com.colmcoughlan.colm.alchemy.model;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by colmc on 30/12/2018.
 */

@Entity
public class Donation {
    @PrimaryKey
    @NonNull
    private String charityName;
    private double totalDonation;
    private long numberOfDonations;
    private long firstDonationTimestamp;
    private long lastDonationTimestamp;

    public String getCharityName() {
        return charityName;
    }

    public void setCharityName(String charityName) {
        this.charityName = charityName;
    }

    public double getTotalDonation() {
        return totalDonation;
    }

    public void setTotalDonation(double totalDonation) {
        this.totalDonation = totalDonation;
    }

    public long getNumberOfDonations() {
        return numberOfDonations;
    }

    public void setNumberOfDonations(long numberOfDonations) {
        this.numberOfDonations = numberOfDonations;
    }

    public long getFirstDonationTimestamp() {
        return firstDonationTimestamp;
    }

    public void setFirstDonationTimestamp(long firstDonationTimestamp) {
        this.firstDonationTimestamp = firstDonationTimestamp;
    }

    public long getLastDonationTimestamp() {
        return lastDonationTimestamp;
    }

    public void setLastDonationTimestamp(long lastDonationTimestamp) {
        this.lastDonationTimestamp = lastDonationTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Donation donation = (Donation) o;

        if (Double.compare(donation.totalDonation, totalDonation) != 0) return false;
        if (numberOfDonations != donation.numberOfDonations) return false;
        if (firstDonationTimestamp != donation.firstDonationTimestamp) return false;
        if (lastDonationTimestamp != donation.lastDonationTimestamp) return false;
        return charityName != null ? charityName.equals(donation.charityName) : donation.charityName == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = charityName != null ? charityName.hashCode() : 0;
        temp = Double.doubleToLongBits(totalDonation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (numberOfDonations ^ (numberOfDonations >>> 32));
        result = 31 * result + (int) (firstDonationTimestamp ^ (firstDonationTimestamp >>> 32));
        result = 31 * result + (int) (lastDonationTimestamp ^ (lastDonationTimestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Donation{" +
                "charityName='" + charityName + '\'' +
                ", totalDonation=" + totalDonation +
                ", numberOfDonations=" + numberOfDonations +
                ", firstDonationTimestamp=" + firstDonationTimestamp +
                ", lastDonationTimestamp=" + lastDonationTimestamp +
                '}';
    }
}
