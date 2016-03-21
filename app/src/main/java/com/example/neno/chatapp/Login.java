package com.example.neno.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Login extends ActionBarActivity {

    private static final String TAG_USERNAME = "username";
    private static final String TAG_PASS = "pass";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    // private static final String url_update = "http://10.0.3.2:8080/login.php";//emulator
    //private static final String url_update = "http://192.168.1.8:8080/login.php";
    private static final String url_update = "http://nenotst.esy.es/login.php";



    JSONParser jsonParser = new JSONParser();

    private ProgressDialog pDialog;

    EditText username;
    EditText password;
    TextView tv;
    TextView tv2;
    String user;
    String pass;
    Button login;
    Button registracija;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
        Button login = (Button) findViewById(R.id.login);
        Button registracija = (Button) findViewById(R.id.registracija);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SaveProductDetails().execute();
            }
        });

        registracija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(),Registracija.class);
                startActivity(intent1);
            }
        });


    }

    class SaveProductDetails extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Pokušaj logina ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected String doInBackground(String... args) {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            //vrijednosti ne smiju biti null



            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_USERNAME,user));
            params.add(new BasicNameValuePair(TAG_PASS,pass));


            // check json success tag
            JSONObject json = jsonParser.makeHttpRequest(url_update,
                    "POST", params);

            // check json success tag
            try {
                Log.d("try", "Pokušaj pristupa bazi");
                int success = json.getInt(TAG_SUCCESS);
                Log.d("resp", json.getString(TAG_SUCCESS));
                if (success == 1) {

                    Log.d("response", "Uspješan login"); // response PHP skripte

                    SharedPreferences sharedPreferences = getSharedPreferences("login_podaci", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();  //SPREMI PODATKE U OBJEKT KLASE SharedPreferences
                    editor.putString("name",user);
                    editor.putString("password",pass);
                    //editor.putString("thumbimg",slika);

                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    finish();
                    startActivity(intent);

                    return json.getString(TAG_MESSAGE);

                } else {

                    Log.d("response", "Krivi podaci za login!");
                    return json.getString(TAG_MESSAGE);////message varijabla iz php skripte

                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Debug", e.toString());
                // JSONObject jObj = new JSONObject(e);
                //throw Toast.makeText(getApplicationContext(), "Server nije dostupan", Toast.LENGTH_SHORT);
            }

            return null;

        }

        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show(); //message varijablu pretvori u toast
            }
        }




    }
}

