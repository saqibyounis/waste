package com.app.thestream.activities;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.thestream.BuildConfig;
import com.app.thestream.Config;
import com.app.thestream.R;
import com.app.thestream.broadcast.broadcastRegister;
import com.app.thestream.callbacks.CallbackUser;
import com.app.thestream.fcm.NotificationUtils;
import com.app.thestream.fragments.FragmentAbout;
import com.app.thestream.fragments.FragmentFavorite;
import com.app.thestream.models.Setting;
import com.app.thestream.models.User;
import com.app.thestream.rests.ApiInterface;
import com.app.thestream.rests.RestAdapter;
import com.app.thestream.tab.FragmentTabCategory;
import com.app.thestream.tab.FragmentTabRecent;
import com.app.thestream.utils.Constant;
import com.app.thestream.utils.GDPR;
import com.app.thestream.utils.HttpTask;
import com.app.thestream.utils.NetworkCheck;
import com.app.thestream.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private static int selectedIndex;
    private Call<CallbackUser> callbackCall = null;
    private final static int COLLAPSING_TOOLBAR = 0;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    BroadcastReceiver broadcastReceiver;
    NavigationView navigationView;
    SharedPreferences preferences;
    private long exitTime = 0;
    private AdView adView;
    String androidId;
    View view; private android.app.AlertDialog alertDialog;
    private AdView mAdView;
    User user;
    broadcastRegister br;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        br.cancletask();


    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);
       br=new broadcastRegister(this);
        br.regBroadcast();

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } else {
            Log.d("Log", "Working in Normal Mode, RTL Mode is Disabled");
        }

        loadBannerAd();

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId("ca-app-pub-6720575921193824/4179455069");

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.alert_dialog, null, false);
        alertDialog.setView(view);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final TextView textView = view.findViewById(R.id.textTime);
        final TextView click = view.findViewById(R.id.btnCancel);
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("00:" + (millisUntilFinished / 1000));

                long sec = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                if (sec == 20) {
                    click.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFinish() {


                Toast.makeText(MainActivity.this, "Time is up ", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        }.start();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Toast.makeText(MainActivity.this, "Ad Opened thanks.", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (savedInstanceState != null) {
            navigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_TAG)).setChecked(true);
            return;
        }

        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new FragmentTabRecent(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                .commit();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    final String id = intent.getStringExtra("id");
                    final String title = intent.getStringExtra("title");
                    final String message = intent.getStringExtra("message");
                    final String image_url = intent.getStringExtra("image_url");

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setView(mView);

                    final TextView notification_title = mView.findViewById(R.id.title);
                    final TextView notification_message = mView.findViewById(R.id.message);
                    final ImageView notification_image = mView.findViewById(R.id.big_image);

                    if (id != null) {
                        if (id.equals("0")) {
                            notification_title.setText(title);
                            notification_message.setText(Html.fromHtml(message));
                            Picasso.with(MainActivity.this)
                                    .load(image_url.replace(" ", "%20"))
                                    .placeholder(R.drawable.ic_thumbnail)
                                    .into(notification_image);
                            alert.setPositiveButton(getResources().getString(R.string.option_ok), null);
                        } else {
                            notification_title.setText(title);
                            notification_message.setText(Html.fromHtml(message));
                            Picasso.with(MainActivity.this)
                                    .load(image_url.replace(" ", "%20"))
                                    .placeholder(R.drawable.ic_thumbnail)
                                    .into(notification_image);

                            alert.setPositiveButton(getResources().getString(R.string.option_read_more), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(), ActivityFCMDetail.class);
                                    intent.putExtra("id", id);
                                    startActivity(intent);
                                }
                            });
                            alert.setNegativeButton(getResources().getString(R.string.option_dismis), null);
                        }
                        alert.setCancelable(false);
                        alert.show();
                    }

                }
            }
        };

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id != null) {
            if (id.equals("0")) {
                Log.d("FCM_INFO", id);
            } else {
                Intent action = new Intent(MainActivity.this, ActivityFCMDetail.class);
                action.putExtra("id", id);
                startActivity(action);
                Log.d("FCM_INFO", id);
            }
        }


        GDPR.updateConsentStatus(this);
        validate();

        requestDetailsPostApi();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.drawer_recent:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentTabRecent(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_category:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentTabCategory(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_favorite:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentFavorite(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.drawer_rate:

                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }

                return true;

            case R.id.drawer_more:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));

                return true;

            case R.id.drawer_share:

                String app_name = android.text.Html.fromHtml(getResources().getString(R.string.app_name)).toString();
                String share_text = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, app_name + "\n\n" + share_text + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;

            case R.id.drawer_about:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FragmentAbout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

        }
        return false;
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void validate() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Setting> call = apiInterface.getPackageName();
        call.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                String package_name = response.body().getPackage_name();
                try {
                    if (BuildConfig.APPLICATION_ID.equals(package_name)) {
                        Log.d("INFO", "Validated");
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle(getResources().getString(R.string.msg_oops));
                        dialog.setMessage(getResources().getString(R.string.msg_validate));
                        dialog.setPositiveButton(getResources().getString(R.string.option_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {
            }

        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitApp();
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            MobileAds.initialize(MainActivity.this, getResources().getString(R.string.admob_app_id));
            adView = (AdView) findViewById(R.id.adView);
            adView.loadAd(Tools.getAdRequest(MainActivity.this));
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });

        } else {
            Log.d("AdMob", "AdMob Banner is Disabled");
        }
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
        AppRate.with(this).showRateDialog(this);
    }

    private void requestDetailsPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getUserToken('"' + androidId + '"');
        callbackCall.enqueue(new Callback<CallbackUser>() {
            @Override
            public void onResponse(Call<CallbackUser> call, Response<CallbackUser> response) {
                CallbackUser resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    user = resp.response;
                    String token = user.user_android_token;
                    String unique_id = user.user_unique_id;
                    String pref_token = preferences.getString("fcm_token", null);

                    if (token.equals(pref_token) && unique_id.equals(androidId)) {
                        Log.d("TOKEN", "FCM Token already exists");
                    } else {
                        updateRegistrationIdToBackend();
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackUser> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(this)) {
            sendRegistrationIdToBackend();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet_text), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRegistrationIdToBackend() {

        Log.d("FCM_TOKEN", "Send data to server...");

        String token = preferences.getString("fcm_token", null);
        String appVersion = BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
        String osVersion = currentVersion() + " " + Build.VERSION.RELEASE;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            nameValuePairs.add(new BasicNameValuePair("user_app_version", appVersion));
            nameValuePairs.add(new BasicNameValuePair("user_os_version", osVersion));
            nameValuePairs.add(new BasicNameValuePair("user_device_model", model));
            nameValuePairs.add(new BasicNameValuePair("user_device_manufacturer", manufacturer));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/token-register.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    private void updateRegistrationIdToBackend() {

        Log.d("FCM_TOKEN", "Update data to server...");
        String token = preferences.getString("fcm_token", null);
        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_unique_id", androidId));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/token-update.php", nameValuePairs, false).execute();
            Log.d("FCM_TOKEN_VALUE", token + " " + androidId);
        }

    }

    public static String currentVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Unsupported";
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        return codeName;
    }

    @Override
    protected void onResume() {
        super.onResume();


            if(br.isvpnactive()){
                Toast.makeText(this, "VPN or packet capture is active,please turn off it to run app", Toast.LENGTH_SHORT).show();

                finish();
            }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Constant.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

}
