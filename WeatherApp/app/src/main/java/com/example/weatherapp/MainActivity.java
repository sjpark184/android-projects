package com.example.weatherapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText cityText;
    TextView infoText;

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();

                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "The city you entered is misspelled or does not exist in our data set", Toast.LENGTH_SHORT).show();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("") || jsonObject==null){

            } else {
                infoText = findViewById(R.id.textView2);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String weatherInfo = jsonObject.getString("weather");
                    String mainInfo = jsonObject.getString("main");
                    JSONObject mainObject = new JSONObject(mainInfo);
                    JSONArray weatherArray = new JSONArray(weatherInfo);
                    for (int i = 0; i < weatherArray.length(); i++) {
                        JSONObject jsonPart = weatherArray.getJSONObject(i);
                        infoText.setText("Forecast: " + jsonPart.getString("main")+"("+ jsonPart.getString("description")+")\n");
                    }
                    infoText.append("Temperature: " + Integer.toString((mainObject.getInt("temp")-273)*9/5+33) +"\n");
                    infoText.append("Humidity: " + mainObject.getString("humidity"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("do in onPostExecutefail", "failed");
                }
            }
        }
    }

    public void onClick(View view){
        DownloadTask task = new DownloadTask();
        String cityName = cityText.getText().toString();
        if(cityName.equals(""))
            Toast.makeText(getApplicationContext(), "Please enter a city", Toast.LENGTH_SHORT).show();
        else
            task.execute("http://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid=18462895bd8a58d5c22376eafe3ef717");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = (EditText) findViewById(R.id.editText);
    }
}
