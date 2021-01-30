package com.colmcoughlan.colm.alchemy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by colm on 28/04/17.
 */

public class DataReader implements Runnable {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String LOGO_URL_STR = "logo_url";
    private static final String DESCRIPTION_STR = "description";

    public interface Callback {
        void onComplete();
    }

    public static void executeAsync(String url, Callback callback) {
        EXECUTOR.execute(new DataReader(url, callback));
    }

    private final String url;
    private final Callback callback;

    private DataReader(String url, Callback callback) {
        this.url = url;
        this.callback = callback;
    }

    @Override
    public void run() {
        if(StaticState.shouldRefreshCharities()){
            StaticState.setCharities(getCharities(url));
        }
        callback.onComplete();
    }

    private static List<Charity> getCharities(String urlString){
        BufferedReader reader;
        HttpsURLConnection urlConnection = null;
        List<Charity> charityList = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("client-id", "alchemy");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip"); // ask for json to be compressed

            InputStream in;
            if ("gzip".equals(urlConnection.getContentEncoding())) { // support compression if present
                in = new BufferedInputStream(new GZIPInputStream(urlConnection.getInputStream()));
            } else {
                in = new BufferedInputStream(urlConnection.getInputStream());
            }
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
                //Log.d("Response: ", "> " + line);
            }
            String result = buffer.toString();

            JSONObject payload = new JSONObject(result);

            // get the number
            final String number = payload.getJSONObject("config")
                    .getString("number"); // phone number

            // now get the charities
            JSONObject charities = payload.getJSONObject("charities");
            Iterator<?> keys = charities.keys();
            // for each charity
            while (keys.hasNext()) {
                String key = (String) keys.next(); // get the charity name
                if (charities.get(key) instanceof JSONObject) {
                    charityList.add(createCharity(key, charities, number));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return charityList;
    }

    private static Charity createCharity(String key, JSONObject charities, String number) throws JSONException {
        JSONObject charity = ((JSONObject) charities.get(key));
        String category = charity.getString("category"); // category
        String link = charity.has(LOGO_URL_STR) ? charity.getString(LOGO_URL_STR) : ""; // logo link
        String description = charity.has(DESCRIPTION_STR) ? charity.getString(DESCRIPTION_STR) : ""; // description link

        Map<String, String> donation_keys_strings = new HashMap<String, String>();
        Map<String, String> frequency_keys_strings = new HashMap<String, String>();

        JSONObject donation_list = new JSONObject(charity.getString("donation_options")); // get the donation options
        Iterator<?> donation_keys = donation_list.keys();
        while (donation_keys.hasNext()) {
            String donation_key = (String) donation_keys.next(); // donation keys and values
            donation_keys_strings.put(donation_key, donation_list.getString(donation_key));
        }

        JSONObject freq_list = new JSONObject(charity.getString("freq")); // get the frequencies
        Iterator<?> freq_keys = freq_list.keys();
        while (freq_keys.hasNext()) {
            String freq_key = (String) freq_keys.next(); // donation keys and values
            frequency_keys_strings.put(freq_key, freq_list.getString(freq_key));
        }

        return new Charity(key, category, description, link, number, donation_keys_strings, frequency_keys_strings);
    }
}