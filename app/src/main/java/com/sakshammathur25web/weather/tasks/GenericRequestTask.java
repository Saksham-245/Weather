package com.sakshammathur25web.weather.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import com.sakshammathur25web.weather.Constants;
import com.sakshammathur25web.weather.R;
import com.sakshammathur25web.weather.activities.MainActivity;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import static com.sakshammathur25web.weather.activities.MainActivity.saveLastUpdateTime;

/**
 * Created by saksham on 11/3/18.
 */

@SuppressWarnings("unused")
@SuppressLint("StaticFieldLeak")
public abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {
    private final ProgressDialog progressDialog;
    private final Context context;
    private final MainActivity activity;
    protected int loading = 0;

    protected GenericRequestTask(Context context, MainActivity activity, ProgressDialog progressDialog){
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        incLoadingCounter();
        if(!progressDialog.isShowing()) {
            progressDialog.setMessage(context.getString(R.string.downloading_data));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void incLoadingCounter() {
        loading++;
    }

    @Override
    protected TaskOutput doInBackground(String... params) {
        TaskOutput output = new TaskOutput();

        StringBuilder response = new StringBuilder();
        String[] coords = new String[]{};

        if (params != null && params.length > 0) {
            final String zeroParam = params[0];
            if ("cachedResponse".equals(zeroParam)) {
                response = new StringBuilder(params[1]);
                // Actually we did nothing in this case :)
                output.taskResult = TaskResult.SUCCESS;
            } else if ("coords".equals(zeroParam)) {
                String lat = params[1];
                String lon = params[2];
                coords = new String[]{lat, lon};
            }
        }

        if (response.length() == 0) {
            try {
                URL url = provideURL(coords);
                Log.i("URL",url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader r = new BufferedReader(inputStreamReader);

                    //noinspection unused
                    int responseCode = urlConnection.getResponseCode();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    close(r);
                    urlConnection.disconnect();
                    // Background work finished successfully
                    Log.i("Task", "done successfully");
                    output.taskResult = TaskResult.SUCCESS;
                    // Save date/time for latest successful result
                    saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(context));
                }
                else if (urlConnection.getResponseCode() == 429) {
                    // Too many requests
                    Log.i("Task", "too many requests");
                    output.taskResult = TaskResult.TOO_MANY_REQUESTS;
                }
                else {
                    // Bad response from server
                    Log.i("Task", "bad response"+urlConnection.getResponseCode());
                    output.taskResult = TaskResult.BAD_RESPONSE;
                }
            } catch (IOException e) {
                Log.e("IOException Data", response.toString());
                e.printStackTrace();
                // Exception while reading data from url connection
                output.taskResult = TaskResult.IO_EXCEPTION;
            }
        }

        if (TaskResult.SUCCESS.equals(output.taskResult)) {
            // Parse JSON data
            ParseResult parseResult = parseResponse(response.toString());
            if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
            output.parseResult = parseResult;
        }

        return output;
    }

    @Override
    protected void onPostExecute(TaskOutput output) {
        if(loading == 1) {
            progressDialog.dismiss();
        }
        decLoadingCounter();

        updateMainUI();

        handleTaskOutput(output);
    }

    private void decLoadingCounter() {
        loading--;
    }

    protected final void handleTaskOutput(TaskOutput output) {
        switch (output.taskResult) {
            case SUCCESS: {
                ParseResult parseResult = output.parseResult;
                if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content),context.getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                } else if (ParseResult.JSON_EXCEPTION.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content),context.getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case TOO_MANY_REQUESTS: {
                Snackbar.make(activity.findViewById(android.R.id.content),context.getString(R.string.msg_too_many_requests), Snackbar.LENGTH_LONG).show();
                break;
            }
            case BAD_RESPONSE: {
                Snackbar.make(activity.findViewById(android.R.id.content),context.getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                break;
            }
            case IO_EXCEPTION: {
                Snackbar.make(activity.findViewById(android.R.id.content),context.getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
                break;
            }
        }
    }

    private String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("cs")) {
            language = "cz";
        }
        return language;
    }

    private URL provideURL(String[] coords) throws UnsupportedEncodingException, MalformedURLException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey = sp.getString("apiKey", activity.getResources().getString(R.string.apiKey));

        StringBuilder urlBuilder = new StringBuilder("http://api.openweathermap.org/data/2.5/");
        urlBuilder.append(getAPIName()).append("?");
        if (coords.length == 2) {
            urlBuilder.append("lat=").append(coords[0]).append("&lon=").append(coords[1]);
        } else {
            final String city = sp.getString("city", Constants.DEFAULT_CITY);
            urlBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
        }
        urlBuilder.append("&lang=").append(getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }

    private void restorePreviousCity(){
        if (!TextUtils.isEmpty(activity.recentCity)){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("city",activity.recentCity);
            editor.apply();
            activity.recentCity = "";
        }
    }

    private static void close(Closeable x){
        try {
            if (x!=null){
                x.close();
            }
        }catch (IOException e){
            Log.e("IOException Data","Error occurred while closing the stream");
        }
    }

    protected void updateMainUI() { }

    protected abstract ParseResult parseResponse(String response);
    protected abstract String getAPIName();
}