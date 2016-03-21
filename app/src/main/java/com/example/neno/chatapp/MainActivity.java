package com.example.neno.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    public final static String DEFAULT = "N/A";
    ImageView profilna;
    Button slikaj;
    Button galerija;
    Button logi;
    TextView Korisnik;
    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private Gson mGson = new Gson();
    private ListView listView;
    private CustomAdapter adapter;

    JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;


    private static final String TAG_SUCCESS = "success";
    private static final String USER = "user";
    private static final String MSG = "msg";  //poveznica sa arg u php
    private static final String VRIJEME = "vrijeme";
    private static final String PROFIL = "thumbimg";

    static  final int CAM_REQUEST = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int RESULT_LOAD_IMAGE = 1;
    private String urlphp = "http://nenotst.esy.es/send.php";
    private String urlphp1 = "http://nenotst.esy.es/view.php";

    public String name;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        //profilna = (ImageView) findViewById(R.id.profilna);
       // slikaj = (Button) findViewById(R.id.slikaj);
       // galerija = (Button) findViewById(R.id.galerija);
       // logi = (Button) findViewById(R.id.log);



        Button sendBtn = (Button) findViewById(R.id.sendButton);




        //ff8300
////////////////////////////////////////////////////////////////////////////////////////
///////////////////ŠALJI PORUKU////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new salji().execute();
            }
        });

///////////////////////////////////////DOBAVI PORUKU//////////////////////////////////////////////////////////
        httpClient.get(urlphp1, new JsonHttpResponseHandler() { //get zahtjev,dobavi JSON sa URL


            @Override
            public void onSuccess(JSONArray response) {

                Log.d("pre_RESPONSE", response.toString());
                //Log.d("length",  ""+DataStorage.poruke.length);

              //DataStorage.cars = mGson.fromJson(response.toString(),Car[].class);  //JSON konvertaj u string, napravi Car objekte
                DataStorage.poruke = mGson.fromJson(response.toString(),Poruka[].class);

                adapter = new CustomAdapter(getApplicationContext());//adapter u trenutnom aktivitiju
                listView.setAdapter(adapter);



            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getApplicationContext(), "Error: ne može se pristupiti serveru.", Toast.LENGTH_LONG).show();
            }


        });









    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //PREUZIMANJE,SPREMANJE U IMAGE VIEW
        if(requestCode == CAM_REQUEST) {
            super.onActivityResult(requestCode, resultCode, data);
            String path = "sdcard/camera_app/cam_image.jpg";
            profilna.setImageDrawable(Drawable.createFromPath(path)); //POSTAVI SA PATHA U imageview
        }
        else if(requestCode == REQUEST_PICK_PHOTO){
            Uri uri = data.getData();
            if (uri != null) {
                uriFJA(uri);
            }
        }

    }

    private void uriFJA(Uri uri) {
        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                profilna.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }






    class salji extends AsyncTask<String, String, String> {


        EditText message = (EditText) findViewById(R.id.messageContentEditText);


        Time now = new Time();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM. HH:mm");

        String currentDateandTime = sdf.format(new Date());
        SharedPreferences sharedPreferences = getSharedPreferences("login_podaci", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name",DEFAULT);









        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Šaljem ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected String doInBackground(String... args) {
            String msg = message.getText().toString();
            if(msg != null && !msg.isEmpty()){
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair(USER, name));
                        params.add(new BasicNameValuePair(MSG, message.getText().toString()));
                        params.add(new BasicNameValuePair(VRIJEME, currentDateandTime));

                        //Log.d("user", userName.getText().toString());
                        Log.d("pass", message.getText().toString());
                        // check json success tag
                        JSONObject json = jsonParser.makeHttpRequest(urlphp,
                                "POST", params);

                        // check json success tag
                        try {
                            Log.d("try", "pokusaj upisa u bazu");
                            int success = json.getInt(TAG_SUCCESS);
                            Log.d("response", "sukses" + success);
                            if (success == 1) {
                                Intent i = getIntent();
                                setResult(100, i);
                                Log.d("response", "uspjeh POST-A");
                                finish();
                            } else {
                                Log.d("GRESKA", "greska");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                return null;
                }
                else
                {
                    return null;
                 //Toast.makeText(getApplicationContext(),"Poruka prazna!",Toast.LENGTH_SHORT);
                }
            //return null;
        }



        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product updated
          //Toast.makeText(getApplicationContext(),"Poslano",Toast.LENGTH_LONG).show();
           // Log.d("VRIJEME", currentDateandTime);
            //Toast.makeText(getApplicationContext(),currentDateandTime,Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            pDialog.dismiss();


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                Log.d("meni","meni");
                Intent slikaIntent = new Intent(getApplicationContext(),Slika.class);
                startActivity(slikaIntent);
                return true;
            //case R.id.help:
            //    showHelp();
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
