package com.colmcoughlan.colm.alchemy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.GridView;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDonations extends AppCompatActivity {

    GridView gridView = null;
    private DonationViewModel donationViewModel;

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
        this.donationViewModel = ViewModelProviders.of(this).get(DonationViewModel.class);

        setContentView(R.layout.activity_my_donations);
        setupActionBar();

        final Observer<List<Donation>> observer = new Observer<List<Donation>>() {
            @Override
            public void onChanged(@Nullable final List<Donation> donations) {
                if (donations.isEmpty()) {
                    Map<String, Integer> smsDonations = getSmsDonations();
                    for (String charity : smsDonations.keySet()) {
                        boolean insert = true;
                        for (Donation donation : donations) {
                            if (donation.getCharityName().equals(charity)) {
                                insert = false;
                            }
                        }
                        if (insert) {
                            donationViewModel.insertNewDonation(charity, smsDonations.get(charity));
                        }
                    }
                }

                gridView = findViewById(R.id.my_donations_gridview);
                gridView.setAdapter(new DonationsAdapter(context, getMap(donations)));
            }
        };

        donationViewModel.getAllDonations().observe(this, observer);
    }

    private Map<String, Integer> getMap(List<Donation> donations) {
        Map<String, Integer> result = new HashMap<>();
        int totalDonations = 0;
        for (Donation donation : donations) {
            int intValue = (int) donation.getTotalDonation();
            totalDonations = totalDonations + intValue;
            result.put(donation.getCharityName(), intValue);
        }

        result.put(getString(R.string.total_donations), totalDonations);

        return result;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private Map<String, Integer> getSmsDonations() {


        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, "address='50300'", null, null);

        Map<String, Integer> donations = new HashMap<>();

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {

                //Log.d("New message", "msg");
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    if (cursor.getColumnName(idx).contentEquals("body")) {
                        String msgData = cursor.getString(idx);

                        if (msgData.startsWith("Thank you")) {
                            try {
                                String charity_name = (msgData.split(" to ")[1]).split("\\.")[0];
                                String[] amountString = msgData.split(" Euro")[0].split(" ");
                                Integer donation_amount = Integer.parseInt(amountString[amountString.length - 1]);
                                //Log.d("charity_name", charity_name);
                                //Log.d("donation_amount", donation_amount.toString());
                                if (donations.containsKey(charity_name)) {
                                    donations.put(charity_name, donations.get(charity_name) + donation_amount);
                                } else {
                                    donations.put(charity_name, donation_amount);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                donations.put("Error!", 1);
                            }
                        }
                    }
                }
            } while (cursor.moveToNext());
        }

        return donations;
    }

}
