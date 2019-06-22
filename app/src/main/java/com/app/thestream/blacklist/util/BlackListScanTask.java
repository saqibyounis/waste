package com.app.thestream.blacklist.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;


import com.app.thestream.blacklist.model.BlockedAppModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BlackListScanTask extends AsyncTask<Void, Integer, ArrayList<BlockedAppModel>> {
    private ArrayList<BlockedAppModel> detectedPkgs = new ArrayList<>(0);
    private BlackListSearchCallback callback;
    private WeakReference<Context> context;

    public BlackListScanTask(Context context, BlackListSearchCallback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected ArrayList<BlockedAppModel> doInBackground(Void... voids) {
        getALlInstalledPackages();
        return detectedPkgs;
    }

    @Override
    protected void onPostExecute(ArrayList<BlockedAppModel> strings) {
        super.onPostExecute(strings);
        if (strings.isEmpty()) {
            if (callback != null)
                callback.onBlackListScanCompleted(false, null);
            ;
        } else {
            if (callback != null)
                callback.onBlackListScanCompleted(true, strings);
            ;
        }
    }

    private void getALlInstalledPackages() {
        detectedPkgs.clear();
        List blockedLists = BlackListUtils.getInstance().getBlackList();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ril = context.get().getPackageManager().queryIntentActivities(mainIntent, 0);
        int count=0;
        for (ResolveInfo ri : ril) {
            if (isSystemPackage(ri))
                continue;
            count++;
            if (ri.activityInfo != null ) {
                try {
                    if (ri.activityInfo.packageName != null) {
                        String pgk = ri.activityInfo.packageName;
                        Log.e("Pkg",pgk);
                        if (blockedLists.contains(pgk)) {
                            detectedPkgs.add(new BlockedAppModel(pgk, null, null));
                        }
                    }
                } catch (Exception list) {
                    list.printStackTrace();
                }
            }
        }
        Log.e("Count",count+"");
    }
    private boolean isSystemPackage(ResolveInfo ri) {
        return (ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
