package com.app.thestream.broadcast;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;


public class broadcastRegister {

    Context ctx;
    AsyncTaskRunner myTask;
     SimpleExoPlayer player;


    public broadcastRegister(Context ctx){

        this.ctx=ctx;



    }


    public void regBroadcast() {


            this.myTask = new AsyncTaskRunner();
            this.myTask.execute(this);



    }
    public void cancletask(){

        this.myTask.cancel(true);

    }
    public  void setplayer(SimpleExoPlayer pl){
        this.player=pl;


    }

    @SuppressLint("NewApi")
    public Boolean isvpnactive() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = cm.getAllNetworks();


        for (int i = 0; i < networks.length; i++) {

            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);


            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {

                return  true;
            }

        }
        return  false;
    }


    private class AsyncTaskRunner extends AsyncTask<broadcastRegister, Void, Void> {







        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(final broadcastRegister... voids) {

            while(true){
                ConnectivityManager cm = (ConnectivityManager)voids[0].ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

                Network[] networks = cm.getAllNetworks();


                for(int i = 0; i < networks.length; i++) {

                    NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);


                   if(caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){


                       ((Activity) voids[0].ctx).runOnUiThread(new Runnable() {
                           public void run() {
                               if(voids[0].player!=null){
                                   voids[0].player.stop();
                               }
                               Toast.makeText(voids[0].ctx, "VPN or packet capture is active,please turn off it to run app", Toast.LENGTH_SHORT).show();
                           }
                       });

                       ((Activity) voids[0].ctx).finish();

                       return null;
                   }

                }
            }

        }

    }


}
