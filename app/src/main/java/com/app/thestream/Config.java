package com.app.thestream;

public class Config {

    //your admin panel url
    public static final String ADMIN_PANEL_URL = "https://shikoshqiptv.xyz/";

    //your api key which obtained from admin panel
    public static final String API_KEY = "cda11uT8cBLzm6a1YvsiUWOEgrFowk95K2DM3tHAPRCX4ypGjN";
    public static String updateapplink="";
    //Ads Configuration
    //set true to enable or set false to disable
    public static final boolean ENABLE_ADMOB_BANNER_ADS = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS = true;
    public static final int ADMOB_INTERSTITIAL_ADS_INTERVAL = 2;

    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_PLAY_STREAMING = false;

    //set true to enable tab layout or set false to disable tab layout
    public static final boolean ENABLE_TAB_LAYOUT = true;

    //set true to turn on grid view in the channel list
    public static final boolean ENABLE_GRID_MODE = true;
    public static final int GRID_SPAN_COUNT = 3;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //load more for next channel list
    public static final int LOAD_MORE = 15;

    //splash screen duration in millisecond
    public static final int SPLASH_TIME = 3000;

}