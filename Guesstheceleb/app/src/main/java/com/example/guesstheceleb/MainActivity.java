package com.example.guesstheceleb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button0, button1, button2, button3;
    String downloadResult;
    ArrayList<String> names, imageURLs, answersList;
    int randNum, correctAnswerLocation;

    public class DownloadTask extends AsyncTask<String, Void, String>{
        //html downloader
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e){
                e.printStackTrace();
                return "FAILED";
            }
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{
        @Override
        //image downloader
        protected Bitmap doInBackground(String... urls) {
            try {
                String temp = "adf";
                URL urlImage = new URL(urls[0].toString());
                HttpURLConnection connection = (HttpURLConnection) urlImage.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public void onClick(View view){
        //when they click button, see if it matches the celeb in the image
        if(Integer.parseInt(view.getTag().toString())==correctAnswerLocation){
            Toast.makeText(getApplicationContext(), "CORRECT! it was " + names.get(randNum), Toast.LENGTH_SHORT).show();
        }   else{
            Toast.makeText(getApplicationContext(), "WRONG, it was " + names.get(randNum), Toast.LENGTH_SHORT).show();
        }
        //then set up new images
        setUpGames();
    }

    public void setUpGames(){
        //download the image from the url
        ImageDownloader task = new ImageDownloader();
        Bitmap myImage = null;
        //range = max-min+1 which is (max:size-1)-(min:0)+1
        randNum = (int)(Math.random()*imageURLs.size());
        try{
            myImage = task.execute(imageURLs.get(randNum)).get();
            imageView.setImageBitmap(myImage);
        }   catch (Exception e){
            e.printStackTrace();
        }
        //set random location for the answer(name) and put texts into buttons
        correctAnswerLocation = (int) (Math.random() * 4);
        for(int i = 0; i<4; i++) {
            try {
                if (i != correctAnswerLocation) {
                    int temp = (int) (Math.random() * names.size());
                    while (temp == randNum) {
                        temp = (int) (Math.random() * names.size());
                    }
                    answersList.add(i, names.get(temp));
                } else {
                    answersList.add(i, names.get(randNum));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        button0.setText(answersList.get(0));
        button1.setText(answersList.get(1));
        button2.setText(answersList.get(2));
        button3.setText(answersList.get(3));
    }

    public void patternAndMatcher(){
        //American
        //urlPattern finds all url for the image
        Pattern urlPattern = Pattern.compile("imgsrc=\"(.*?)\"alt");
        //name pattern finds all names for the celeb
        Pattern namePatter = Pattern.compile("alt=\"(.*?)\"/></d");

//        //temp for figuring out korean
//        Log.i("downloaded html", downloadResult);
//
//        //Korean
//        //urlPattern finds all url for the image
//        Pattern urlPattern = Pattern.compile("imgsrc=\"(.*?)\"data-original");
//        //name pattern finds all names for the celeb
//        Pattern namePatter = Pattern.compile("<strong>\\d.(.*?)</strong>");

        //matches them
        Matcher imgURLMatcher = urlPattern.matcher(downloadResult);
        Matcher nameMatcher = namePatter.matcher(downloadResult);
        //add them into array lists
        while(imgURLMatcher.find()){
            imageURLs.add(imgURLMatcher.group(1));
            System.out.println(imgURLMatcher.group(1));
        }
        while(nameMatcher.find()){
//            names.add(nameMatcher.group(1).replaceAll("(.)([A-Z])", "$1 $2"));
            names.add(nameMatcher.group(1).replaceFirst("(.)([A-Z])", "$1 $2"));
            System.out.println(nameMatcher.group(1));
        }
        //then set up games
        setUpGames();
    }

    public void deleteSpaces(){
        //delete all spaces and run pattern and matcher
        downloadResult = downloadResult.replaceAll("\\s", "");
        patternAndMatcher();
    }

    public void initialize(){
        //initialization
        imageView = (ImageView) findViewById(R.id.imageView2);
        names = new ArrayList<String>();
        imageURLs = new ArrayList<String>();
        answersList = new ArrayList<String>();
        button0 = (Button) findViewById(R.id.textView0);
        button1 = (Button) findViewById(R.id.textView1);
        button2 = (Button) findViewById(R.id.textView2);
        button3 = (Button) findViewById(R.id.textView3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        initialize();

        //download html
        DownloadTask task = new DownloadTask();
        downloadResult = null;
        try{
            downloadResult = task.execute("http://www.posh24.se/kandisar").get();
        }   catch (Exception e){
            e.printStackTrace();
        }

        deleteSpaces();
    }
}