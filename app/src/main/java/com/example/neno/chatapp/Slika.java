package com.example.neno.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;


public class Slika extends ActionBarActivity {
    ImageView pic;
    ImageView pic1;
    Bitmap img;
    private static final String TAG_SUCCESS = "success";
    private static final String USER = "user";
    private static final String MSG = "msg";
    private static final String SLIKA = "thumbimg";
    public final static String DEFAULT = "N/A";

    static  final int CAM_REQUEST = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
   // private static final int RESULT_LOAD_IMAGE = 1;
    private String urlphp = "http://nenotst.esy.es/uploadslike.php";
    JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;
    Button slikaj;
    Button galerija;
    Button uploadaj;


    private String selectedImagePath = "";
    final private int PICK_IMAGE = 1;
    final private int CAPTURE_IMAGE = 2;
    private String imgPath;
    ImageView imageView;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slika);
        //et = (EditText) findViewById(R.id.et);

        slikaj = (Button) findViewById(R.id.slikaj);
        galerija = (Button) findViewById(R.id.galerija);
        uploadaj = (Button) findViewById(R.id.uploadaj);
        imageView = (ImageView) findViewById(R.id.profilna);



        galerija.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);

            }
        });

        slikaj.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });
/*
        slikaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getFile();
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(camera, CAM_REQUEST);
            }
        });

        galerija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_PHOTO);
            }
        });  */



        uploadaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    SharedPreferences sharedPreferences = getSharedPreferences("login_podaci", Context.MODE_PRIVATE);
                    String name = sharedPreferences.getString("name",DEFAULT);
                if(imageView.getDrawable()!=null) {
                    Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap(); //imageview prebaci u bitmap
                    new UploadImage(image,name).execute();  //slika ne smiej biti prevelika
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Slika nije postavljena!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////
    /////////////DEFINIRANJE GDJE CE SE SLIKA SPREMITI//////////////////
    /////////////////////////////////////////////////////////////////////
    private File getFile()
    {
        File folder=new File("sdcard/camera_app");
        if(!folder.exists())
        {
            folder.mkdir();
        }
        File image_file = new File(folder, "cam_image.jpg");

        return image_file;

    }


    public Uri setImageUri() {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        this.imgPath = file.getAbsolutePath();
        return imgUri;
    }


    public String getImagePath() {
        return imgPath;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE) {
                selectedImagePath = getAbsolutePath(data.getData());


                imageView.setImageBitmap(decodeFile(selectedImagePath));
            } else if (requestCode == CAPTURE_IMAGE) {
                selectedImagePath = getImagePath();

                imageView.setImageBitmap(decodeFile(selectedImagePath));
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }


    public Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public String getAbsolutePath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// //PREUZIMANJE,SPREMANJE U IMAGE VIEW/////////////////////////////////
    ///////////////////////////////////////////////////////////////////
  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //PREUZIMANJE,SPREMANJE U IMAGE VIEW
        //if(requestCode == CAM_REQUEST)
           super.onActivityResult(requestCode, resultCode, data);

            String path = "sdcard/camera_app/cam_image.jpg";
            pic.setImageDrawable(Drawable.createFromPath(path)); //KREIRAJ DRAWABLE IZ PATHA I POSTAVI GA U IMAGEVIEW

/*

        if(requestCode == REQUEST_PICK_PHOTO){
            super.onActivityResult(requestCode, resultCode, data);

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                ImageView imageView = (ImageView) findViewById(R.id.profilna);
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }*/








    private class UploadImage extends AsyncTask<Void, Void, Void>
    {
        //SharedPreferences sharedPreferences = getSharedPreferences("login_podaci", Context.MODE_PRIVATE);
        //String name = sharedPreferences.getString("name",DEFAULT);
        String name;
        Bitmap image;


        public UploadImage(Bitmap image,String name)
        {
            this.image=image;
            this.name=name;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Slika.this);
            pDialog.setMessage("Uploading ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }



        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,30,byteArrayOutputStream);//bitmap komprimiraj sa kvalitetom 100% u BYTEARRAY
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);//BYTEARRAY kodiraj u String

            Log.d("imeeeeee",name);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("name", name));

            Log.d("BASE64",encodedImage);

            JSONObject json = jsonParser.makeHttpRequest(urlphp,
                    "POST", dataToSend);

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


        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);
            Toast.makeText(getApplicationContext(),"Slika je uploadana",Toast.LENGTH_LONG).show();
            pDialog.dismiss();
        }
    }


/*
    class uploadaj extends AsyncTask<String,String,String> {


        SharedPreferences sharedPreferences = getSharedPreferences("login_podaci", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name",DEFAULT);
        Bitmap image;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Slika.this);
            pDialog.setMessage("Šaljem ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected String doInBackground(String... args) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);//bitmap komprimiraj sa kvalitetom 100% u BYTEARRAY
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);//BYTEARRAY kodiraj u String

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(USER, name));
               // params.add(new BasicNameValuePair(MSG, message.getText().toString()));
                params.add(new BasicNameValuePair(SLIKA, encodedImage));

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






        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product updated
            //Toast.makeText(getApplicationContext(),"Poslano",Toast.LENGTH_LONG).show();
            // Log.d("VRIJEME", currentDateandTime);
            //Toast.makeText(getApplicationContext(),currentDateandTime,Toast.LENGTH_LONG).show();


            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            pDialog.dismiss();


        }

    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slika, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
