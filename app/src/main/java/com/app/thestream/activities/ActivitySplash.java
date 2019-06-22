package com.app.thestream.activities;

import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.thestream.Config;
import com.app.thestream.R;
import com.app.thestream.blacklist.activity.BlackListRemoveActivity;
import com.app.thestream.blacklist.model.BlockedAppModel;
import com.app.thestream.blacklist.util.BlackListScanTask;
import com.app.thestream.blacklist.util.BlackListSearchCallback;
import com.app.thestream.broadcast.broadcastRegister;

import com.app.thestream.models.UpdateModel;
import com.app.thestream.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.rootbeer.RootBeer;

import java.util.ArrayList;

public class ActivitySplash extends AppCompatActivity implements BlackListSearchCallback {


    Boolean isCancelled = false;
    private ProgressBar progressBar;
    private InterstitialAd interstitialAd;
    String id = "0", cname = "";

    private static final int REQUEST_CODE_REMOVE_APPS = 100;
    broadcastRegister br;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        br.cancletask();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(br.isvpnactive()){
            Toast.makeText(this, "VPN or packet capture is active,please turn off it to run app", Toast.LENGTH_SHORT).show();

            finish();
        }
  }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UpdateModel");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UpdateModel value = dataSnapshot.getValue(UpdateModel.class);
                //Log.d(TAG, "Value is: " + value);
                Config.updateapplink=value.getApklink();
                PackageInfo pinfo = null;
                try {
                    pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String versionName = pinfo.versionName;
                System.out.println(versionName+"=="+value.getApkv());
                if(!versionName.equalsIgnoreCase(value.getApkv())){

                    startActivity(new Intent(ActivitySplash.this,UpdateActivity.class));
                }else {
                    br=new broadcastRegister(ActivitySplash.this);
                    br.regBroadcast();
                    RootBeer rootBeer = new RootBeer(ActivitySplash.this);
                    if (rootBeer.isRooted()) {
                        Toast.makeText(ActivitySplash.this,"App can't run on rooted device",Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        new BlackListScanTask(ActivitySplash.this, ActivitySplash.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }


    private void handleSplashLogic(){

        initAds();
        loadInterstitialAd();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra("nid")) {
            id = getIntent().getStringExtra("nid");
            cname = getIntent().getStringExtra("cname");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled) {
                    if (id.equals("0")) {
                        Intent intent = new Intent(ActivitySplash.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(ActivitySplash.this, ActivityOneSignalDetail.class);
                        intent.putExtra("id", id);
                        intent.putExtra("cname", cname);
                        startActivity(intent);
                        finish();
                    }
                }
                showInterstitialAd();
            }
        }, Config.SPLASH_TIME);
    }

    public void initAds() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            MobileAds.initialize(ActivitySplash.this, getResources().getString(R.string.admob_app_id));
        }
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(ActivitySplash.this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(ActivitySplash.this));
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }
    }

    @Override
    public void onBlackListScanCompleted(boolean isBlackListDetected, ArrayList<BlockedAppModel> list) {
        if (isBlackListDetected) {
            Intent intent = new Intent(ActivitySplash.this, BlackListRemoveActivity.class);
            intent.putParcelableArrayListExtra(BlackListRemoveActivity.DATA_EXTRA, list);
            startActivityForResult(intent, REQUEST_CODE_REMOVE_APPS);
        } else {
          handleSplashLogic();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_REMOVE_APPS) {
            switch (resultCode) {
                case RESULT_OK:


                    handleSplashLogic();


                    break;

                case RESULT_CANCELED:
                    Toast.makeText(ActivitySplash.this, "Unable to continue without uninstall forbidden applications", Toast.LENGTH_SHORT).show();
                    finish();//or do what ever you want (user cancelled uninstall prompt)
                    break;

                case RESULT_FIRST_USER:
                    Toast.makeText(ActivitySplash.this, "Unable to continue without uninstall forbidden applications. some error occurred " +
                            "please try after device restart or try after manual uninstall.", Toast.LENGTH_SHORT).show();
                    finish();//or do what ever you want (some error happened while uninstall app, restart device and try again )
                    break;
            }
        }
    }


}
