package com.colmcoughlan.colm.alchemy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.colmcoughlan.colm.alchemy.model.Callback;
import com.colmcoughlan.colm.alchemy.model.Charity;
import com.colmcoughlan.colm.alchemy.model.Donation;
import com.colmcoughlan.colm.alchemy.service.CharityService;
import com.colmcoughlan.colm.alchemy.service.HttpCharityService;
import com.colmcoughlan.colm.alchemy.utils.DialogUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class CharityActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private GridView gridView = null;
    private Menu menu;
    private DonationViewModel donationViewModel;
    private List<Donation> donations = Collections.emptyList();

    // create the main screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharityService charityService = new HttpCharityService(this);
        setContentView(R.layout.activity_charities);
        setupActionBar();
        this.donationViewModel = ViewModelProviders.of(this).get(DonationViewModel.class);

        // set up observer for donations
        final Observer<List<Donation>> observer = updatedDonations -> donations = updatedDonations;
        donationViewModel.getAllDonations().observe(this, observer);

        // if this is the first run, display an information box

        if (firstRun()) {
            showHelp();
        }

        // create the gridview and get the data

        gridView = findViewById(R.id.gridview);
        charityService.getCharities(callback());

        // set up click listener for selection of charities
        gridView.setOnItemClickListener(createOnClickListener());
    }

    private Callback callback() {
        final Context context = this;
        return () -> runOnUiThread(() -> {
            View progressBarGroup = findViewById(R.id.indeterminateBar);
            progressBarGroup.setVisibility(View.GONE);
            ImageAdapter imageAdapter = new ImageAdapter(context, StaticState.getCharities());
            imageAdapter.getFilter().filter("" + ":cat:" + StaticState.getCategory());
            gridView.setAdapter(imageAdapter);
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private OnItemClickListener createOnClickListener() {
        return (parent, view, position, id) -> {
            final Charity charity = (Charity) gridView.getItemAtPosition(position);
            final Map<String, String> freqs = charity.getFrequencies();

            AlertDialog.Builder builder = new AlertDialog.Builder(CharityActivity.this);
            builder.setTitle(String.format("%s\nChoose a donation option", charity.getName()));
            List<String> keywords = charity.getKeywords();
            Context context = this;
            builder.setItems(charity.getDonationText(), (dialog, which) -> {
                String keyword = keywords.get(which);
                DialogUtils.INSTANCE.confirmDialog(context, charity,
                        keyword, freqs.get(keyword),
                        () -> sendSms(charity, keyword));
            });

//
//            AlertDialog.Builder builder = new AlertDialog.Builder(CharityActivity.this);
//            LayoutInflater inflater = getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.charity_summary_and_donation_options, null);
//            builder.setView(dialogView);
//            builder.setTitle(charity.getName());
//
//            // Reference to the TextView for the message
//            TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
//            messageTextView.setText(charity.getDescription());
//
//            // Reference to the ListView for the items
//            ListView listView = dialogView.findViewById(R.id.list_view);
//
//            // Set up the adapter for the ListView
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, charity.getDonationText());
//            listView.setAdapter(adapter);
//
//            // Handle item clicks
//            listView.setOnItemClickListener((parent1, view1, position1, id1) -> {
//                String keyword = charity.getKeywords().get(position1);
//                DialogUtils.INSTANCE.confirmDialog(this, charity, keyword, freqs.get(keyword), () -> sendSms(charity, keyword));
//            });

            builder.create().show();
        };
    }

    // add the search and about sections to the menu. Hook up the search option to the correct searchview


    // this is actually the same as on create, but called after resume to make sure menu is ok

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.charity_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchMenuItem.collapseActionView();

        this.menu = menu;

        return true;
    }

    @Override
    public void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }


    // add ability to select about activity

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menu.findItem(R.id.search).collapseActionView();
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // don't really submit searches, but still need function

    @Override
    public boolean onQueryTextSubmit(String query) {
        ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
        if (imageAdapter != null) {
            imageAdapter.getFilter().filter(query + ":cat:" + StaticState.getCategory());
        }

        return true;
    }

    // connect search to imageadapter filter method

    @Override
    public boolean onQueryTextChange(String newText) {
        ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
        if (imageAdapter != null) {
            imageAdapter.getFilter().filter(newText + ":cat:" + StaticState.getCategory());
        }
        return true;
    }

    // is this the first run?

    private boolean firstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
        }
        return !previouslyStarted;
    }

    // show help if needed

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.welcome_title);
        builder.setMessage(R.string.welcome_text);
        builder.setPositiveButton(R.string.welcome_dismiss, (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // actually send the text

    private void sendSms(Charity charity, String keyword) {
        Uri uri = Uri.parse("smsto:" + charity.getNumber());
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.setData(uri);
        intent.putExtra("address", charity.getNumber());
        intent.putExtra("sms_body", keyword);
        intent.putExtra("exit_on_sent", true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            donationViewModel.recordDonation(this.donations, charity.getName(), smsKeywordToDonation(charity.getCost(keyword)));
        } else {
            Toast.makeText(this, "No SMS provider found", Toast.LENGTH_SHORT).show();
        }
    }

    private static Integer smsKeywordToDonation(final String keyword) {
        String amountString = keyword.split("€")[1];
        return Integer.parseInt(amountString);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                case Activity.RESULT_CANCELED: // unfortunately returned by default android sms app at the moment
                    Toast.makeText(this, R.string.toast_confirmation, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
