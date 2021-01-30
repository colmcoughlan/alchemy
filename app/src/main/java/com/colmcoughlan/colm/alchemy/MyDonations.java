package com.colmcoughlan.colm.alchemy;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MyDonations extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);
        final DonationViewModel donationViewModel = ViewModelProviders.of(this).get(DonationViewModel.class);

        setContentView(R.layout.activity_my_donations);
        setupActionBar();

        final Observer<List<Donation>> observer = new Observer<List<Donation>>() {
            @Override
            public void onChanged(@Nullable final List<Donation> donations) {
                TextView textView = findViewById(R.id.my_donations_total);
                textView.setText('€' + String.valueOf(getTotal(donations)));
                GridView gridView = findViewById(R.id.my_donations_gridview);
                gridView.setAdapter(new DonationsAdapter(context, getMap(donations)));
            }
        };

        donationViewModel.getAllDonations().observe(this, observer);
    }

    private int getTotal(List<Donation> donations) {
        int totalDonations = 0;
        for (Donation donation : donations) {
            totalDonations += (int) donation.getTotalDonation();
        }
        return totalDonations;
    }

    private Map<String, Integer> getMap(List<Donation> donations) {
        Map<String, Integer> result = new HashMap<>();
        for (Donation donation : donations) {
            result.put(donation.getCharityName(), (int) donation.getTotalDonation());
        }

        return result;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
