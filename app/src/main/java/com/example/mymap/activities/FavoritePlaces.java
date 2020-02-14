package com.example.mymap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mymap.R;
import com.example.mymap.adapters.FavAdapter;
import com.example.mymap.database.AppDatabase;
import com.example.mymap.database.AppExecutors;
import com.example.mymap.model.UserData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritePlaces extends AppCompatActivity implements View.OnClickListener {

    FavAdapter favAdapter;
    LinearLayoutManager layoutManager;

    @BindView(R.id.rec_favlocation)
    RecyclerView rec_favlocation;

    @BindView(R.id.img_addfav)
    ImageView img_addfav;

    UserData userData;
    public AppDatabase mDb;
    List<UserData> user;

    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        ButterKnife.bind(this);
        userData = new UserData();
        mDb = AppDatabase.getInstance(getApplicationContext());
        retrieveTasks();
        img_addfav.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId()) {

            case R.id.img_addfav:
                Intent intent = new Intent(FavoritePlaces.this, MapsActivity.class);
                intent.putExtra("from", 0);
                startActivity(intent);
                break;

        }
    }


    private void retrieveTasks() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                user = mDb.favDao().loadAllPersons();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favAdapter = new FavAdapter(FavoritePlaces.this, user);
                        layoutManager = new LinearLayoutManager(FavoritePlaces.this);
                        rec_favlocation.setLayoutManager(layoutManager);
                        rec_favlocation.setHasFixedSize(true);
                        rec_favlocation.setItemAnimator(new DefaultItemAnimator());
                        rec_favlocation.setAdapter(favAdapter);
                    }
                });
            }
        });
    }

    public void deletefav(UserData deleteint) {

        Dialog dialog = new Dialog(FavoritePlaces.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FavoritePlaces.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_data_layout);
        dialog.setCancelable(false);

        Button deldata= dialog.findViewById(R.id.but_deldata);
        Button cancel= dialog.findViewById(R.id.but_canceldata);

        deldata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.favDao().delete(deleteint);
                      // retrieveTasks();
                        dialog.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(FavoritePlaces.this, FavoritePlaces.class);
                                intent.putExtra("category", "dsd");
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void editfav(UserData userData) {

        Intent intent = new Intent(FavoritePlaces.this, MapsActivity.class);
        intent.putExtra("country", userData.getCountry());
        intent.putExtra("state", userData.getState());
        intent.putExtra("city", userData.getCity());
        intent.putExtra("address", userData.getAddress());
        intent.putExtra("postal", userData.getPostalCode());
        intent.putExtra("latitude", userData.getLatitude());
        intent.putExtra("longitude", userData.getLongitude());
        intent.putExtra("markercode", userData.getApicountrycode());
        intent.putExtra("markercity", userData.getApicity());
        intent.putExtra("from", 1);
        startActivity(intent);
    }




    @Override
    public void onBackPressed() {
        if (exit) {
            finishAffinity(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }
}
