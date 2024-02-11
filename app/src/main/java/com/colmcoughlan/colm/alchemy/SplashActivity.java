package com.colmcoughlan.colm.alchemy;

import android.content.Intent;
import android.os.Bundle;

import com.colmcoughlan.colm.alchemy.service.CharityService;
import com.colmcoughlan.colm.alchemy.service.HttpCharityService;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by colmc on 22/08/2017.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preload the charities
        CharityService repository = new HttpCharityService(this);
        repository.getCharities(() -> {
        });
        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
        finish();
    }
}