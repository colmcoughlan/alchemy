package com.colmcoughlan.colm.alchemy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.colmcoughlan.colm.alchemy.model.Donation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    static String category = "All";
    private GridView gridView = null;
    Activity mainActivity = this;
    private Menu menu;
    private DonationViewModel donationViewModel;
    private List<Donation> donations = Collections.EMPTY_LIST;

    // add the search and about sections to the menu. Hook up the search option to the correct searchview


    // this is actually the same as on create, but called after resume to make sure menu is ok

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

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
            case R.id.my_donations:
                Intent donations = new Intent(this, MyDonations.class);
                startActivity(donations);
                break;
            case R.id.about:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // don't really submit searches, but still need function

    @Override
    public boolean onQueryTextSubmit(String query) {
        ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
        imageAdapter.getFilter().filter(query + ":cat:" + category);

        return true;
    }

    // connect search to imageadapter filter method

    @Override
    public boolean onQueryTextChange(String newText) {


        ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
        imageAdapter.getFilter().filter(newText + ":cat:" + category);

        // use to enable search view popup text
//        if (TextUtils.isEmpty(newText)) {
//            friendListView.clearTextFilter();
//        }
//        else {
//            friendListView.setFilterText(newText.toString());
//        }

        return true;
    }

    // create the main screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            showHelp(this);
        }

        // create the gridview and get the data

        gridView = findViewById(R.id.gridview);
        DataReader dataReader = new DataReader(this, gridView);
        dataReader.execute(getString(R.string.server_url));

        // create the category spinner

        Spinner spinner = findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        spinner.setAdapter(adapter); // Apply the adapter to the spinner
        spinner.setSelection(0, false); // need this to stop OnItemSelectedListener being called at start
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                  category = (String) parent.getItemAtPosition(position);
                                                  if (gridView != null) {
                                                      ImageAdapter imageAdapter = (ImageAdapter) gridView.getAdapter();
                                                      if (imageAdapter != null) {
                                                          imageAdapter.getFilter().filter("" + ":cat:" + category);
                                                      }
                                                  }
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> parent) {
                                                  category = "All";
                                              }
                                          }
        );

        // set up click listener for selection of charities

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                final Charity charity = (Charity) gridView.getItemAtPosition(position);
                final List<String> keywords = charity.getKeys();
                final Map<String, String> freqs = charity.getFreqs();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose a keyword.");
                builder.setItems(charity.getKeywords(keywords), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog(charity, keywords.get(which), freqs.get(keywords.get(which)));
                    }
                });
                builder.create().show();
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Charity charity = (Charity) gridView.getItemAtPosition(position);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getApplicationContext(), charity.getDescription(), duration);
                toast.show();
                return true; // cancel the single click with true
            }
        });
    }

    // is this the first run?

    private boolean firstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();
        }
        return !previouslyStarted;
    }

    // show help if needed

    private void showHelp(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome!");
        builder.setMessage(R.string.welcome_text);
        builder.setPositiveButton(R.string.welcome_dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ActivityCompat.requestPermissions(activity, new String[]{READ_SMS}, 0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // if we get permissions for SMS - great, otherwise - raise an error and quit the app

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length == 0) {
            return;
        }
        switch (requestCode) {
            case 0: {
                if (Arrays.asList(permissions).contains(READ_PHONE_STATE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.phone_permission_confirmation, Toast.LENGTH_SHORT).show();
                    } else {
                        showPermissionDeniedDialog("Warning: This app needs calling permissions!", R.string.phone_permission_refused);
                    }
                } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDeniedDialog("Warning: This app needs SMS permissions!", R.string.sms_text);
                }
            }
        }
    }

    private void showPermissionDeniedDialog(String title, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.welcome_dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mainActivity.finish();
            }
        });
        builder.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(this, R.string.toast_confirmation, Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED: // unfortunately returned by default android sms app at the moment
                    Toast.makeText(this, R.string.toast_confirmation, Toast.LENGTH_SHORT).show();
                    break;
                default:
                Toast.makeText(this, "Error sending text", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static Integer smsKeywordToDonation(final String keyword) {
        String amountString = keyword.split("€")[1];
        return Integer.parseInt(amountString);
    }
}
