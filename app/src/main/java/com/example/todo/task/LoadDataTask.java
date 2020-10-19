package com.example.todo.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Scanner;

public class LoadDataTask extends AsyncTask<String, Void, String> {

    // Declare any reference to UI objects from UI controller

    public LoadDataTask() { // Can pass references to UI objects
    }

    @Override
    protected String doInBackground(String... strings) {

        String requestURL = strings[0];
        URL wikiRequest = null;
        try {
            wikiRequest = new URL(requestURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;

        try {
            assert wikiRequest != null;
            connection = (HttpURLConnection) wikiRequest.openConnection();
        } catch (IOException e) {
            System.out.println("Connection failed\n");
        }

        assert connection != null;
        connection.setDoOutput(true);


        Scanner scanner = null;
        try {
            scanner = new Scanner(wikiRequest.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert scanner != null;
        String response = scanner.useDelimiter("\\Z").next();
        //JSONObject json = java.text.MessageFormat.parseJson(response);

        Log.d("IMP MSG: ", response);
        scanner.close();

        return response;
    }

    @Override
    protected void onPostExecute(String temp) {     // Update any UI object's data
        Log.d("IMP MSG: ", temp);
    }
}