package com.example.neno.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by neno on 1.3.2016..
 */


public class CustomAdapter extends BaseAdapter {
//ADAPTER ZA IZLISTAVANJE DOSTUPNIH VOZILA
private Context mContext;
private LayoutInflater mInflater;
public final static String DEFAULT = "N/A";



        public CustomAdapter(Context context)
        {

            mContext = context;
            mInflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        }

        @Override
        public int getCount() {
            return DataStorage.poruke.length;
        }

        @Override
        public Object getItem(int position) {
            //return carItem.get(position);
            return  null;


        }

        @Override
        public long getItemId(int position) {
            return 0;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item,parent,false);

                ImageView profilna = (ImageView) convertView.findViewById(R.id.profilna);
                TextView Korisnik = (TextView) convertView.findViewById(R.id.korisnik);
                TextView Poruka = (TextView) convertView.findViewById(R.id.poruka);
                TextView Vrijeme = (TextView) convertView.findViewById(R.id.tv_vrijeme);

                SharedPreferences sharedPreferences = mContext.getSharedPreferences("login_podaci", 0);
                String name = sharedPreferences.getString("name",DEFAULT);
                Log.d("ime",name);

                byte[] decodedString = Base64.decode(DataStorage.poruke[position].thumbimg, Base64.DEFAULT); //primi base 64 string,dekodiraj u bajtove

                Log.d("decodedstring6",decodedString.toString());
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);//bajtovi => slika
                profilna.setImageBitmap(decodedByte);

                //Korisnik.setText(name + ": ");
                Korisnik.setText((DataStorage.poruke[position].korisnik).toString() + ": ");
                Poruka.setText(" "+(DataStorage.poruke[position].por).toString()+" ");
                Vrijeme.setText((DataStorage.poruke[position].vrijeme).toString());
               // profilna.getDrawable(profilna);






            //Log.d("aaaaaaaaa", "manufacturer " + DataStorage.cars[position].manufacturer);//pozicija je int postavljen na 0,ink skrolon



            return convertView;
        }
}

