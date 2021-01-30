package com.colmcoughlan.colm.alchemy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by colmc on 22/08/2017.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preload the charities
        DataReader.executeAsync(getString(R.string.server_url), callback());
        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
        finish();
    }

    private DataReader.Callback callback() {
        return new DataReader.Callback() {
            @Override
            public void onComplete() {
                // do nothing
            }
        };
    }
}