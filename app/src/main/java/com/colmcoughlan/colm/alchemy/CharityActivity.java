package com.colmcoughlan.colm.alchemy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
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
        setContentView(R.layout.activity_charities);
        setupActionBar();
        this.donationViewModel = ViewModelProviders.of(this).get(DonationViewModel.class);

        // set up observer for donations
        final Observer<List<Donation>> observer = new Observer<List<Donation>>() {
            @Override
            public void onChanged(@Nullable final List<Donation> updatedDonations) {
                donations = updatedDonations;
            }
        };
        donationViewModel.getAllDonations().observe(this, observer);

        // if this is the first run, display an information box

        if (firstRun()) {
            showHelp();
        }

        // create the gridview and get the data

        gridView = findViewById(R.id.gridview);
        DataReader.executeAsync(getString(R.string.server_url), callback());

        // set up click listener for selection of charities
        gridView.setOnItemClickListener(createOnClickListener());
        gridView.setOnItemLongClickListener(createOnLongClickListener());
    }

    private DataReader.Callback callback(){
        final Context context = this;
        return new DataReader.Callback() {
            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View progressBarGroup = findViewById(R.id.indeterminateBar);
                        progressBarGroup.setVisibility(View.GONE);
                        ImageAdapter imageAdapter = new ImageAdapter(context, StaticState.getCharities());
                        imageAdapter.getFilter().filter("" + ":cat:" + StaticState.getCategory());
                        gridView.setAdapter(imageAdapter);
                    }
                });
            }
        };
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private OnItemClickListener createOnClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                final Charity charity = (Charity) gridView.getItemAtPosition(position);
                final List<String> keywords = charity.getKeys();
                final Map<String, String> freqs = charity.getFreqs();

                AlertDialog.Builder builder = new AlertDialog.Builder(CharityActivity.this);
                builder.setTitle("Choose a keyword.");
                builder.setItems(charity.getKeywords(keywords), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog(charity, keywords.get(which), freqs.get(keywords.get(which)));
                    }
                });
                builder.create().show();
            }
        };
    }

    private AdapterView.OnItemLongClickListener createOnLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Charity charity = (Charity) gridView.getItemAtPosition(position);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getApplicationContext(), charity.getDescription(), duration);
                toast.show();
                return true; // cancel the single click with true
            }
        };
    }

    // add the search and about sections to the menu. Hook up the search option to the correct searchview


    // this is actually the same as on create, but called after resume to make sure menu is ok

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.charity_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));

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
        imageAdapter.getFilter().filter(query + ":cat:" + StaticState.getCategory());

        return true;
    }

    // connect search to imageadapter filter method

    @Override
    public boolean onQueryTextChange(String newText) {
        ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
        imageAdapter.getFilter().filter(newText + ":cat:" + StaticState.getCategory());
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
        builder.setTitle("Welcome!");
        builder.setMessage(R.string.welcome_text);
        builder.setPositiveButton(R.string.welcome_dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // check with the user if they want to confirm a donation

    private void confirmDialog(final Charity charity, final String keyword, final String freq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg;
        if (freq.equals("once")) {
            msg = "Donate ";
        } else if (freq.equals("week")) {
            msg = "Set up a weekly donation of ";
        } else if (freq.equals("month")) {
            msg = "Set up a monthly donation of ";
        } else {
            msg = "ERROR! Please report this and try a different donation option.";
        }

        builder.setTitle(msg + charity.getCost(keyword) + " to " + charity.getName() + "?");
        builder.setMessage(getString(R.string.likecharity_tcs));
        builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendSms(charity, keyword);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
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
            startActivityForResult(intent, 1);
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
