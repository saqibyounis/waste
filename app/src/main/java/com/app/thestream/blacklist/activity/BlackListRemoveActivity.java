package com.app.thestream.blacklist.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import com.app.thestream.R;

import com.app.thestream.blacklist.adatptor.BlackListAppsAdapter;
import com.app.thestream.blacklist.model.BlockedAppModel;
import com.app.thestream.blacklist.util.BlackListSelectionCallback;

import java.util.ArrayList;

public class BlackListRemoveActivity extends AppCompatActivity implements BlackListSelectionCallback {

    public static final String DATA_EXTRA = "dataList";
    private ArrayList<BlockedAppModel> blockedApps;
    private RecyclerView recycler_view_app_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list_remove);
        recycler_view_app_list = findViewById(R.id.recycler_view_app_list);
        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recycler_view_app_list.setLayoutManager(new LinearLayoutManager(this));
        try {
            //noinspection unchecked
            blockedApps =  getIntent().getParcelableArrayListExtra(DATA_EXTRA);
            if (blockedApps == null || blockedApps.isEmpty()) {
                finish();
            }
        } catch (Exception e) {
            finish();
        }
        setupAdaptor();
    }

    private void setupAdaptor() {
        BlackListAppsAdapter blackListAppsAdapter = new BlackListAppsAdapter(blockedApps, this);
        recycler_view_app_list.setAdapter(blackListAppsAdapter);
    }

    private BlockedAppModel tempStorage;
    @Override
    public void onDeleteRequest(int pos) {
        tempStorage=blockedApps.get(pos);
        requestUnInstall(tempStorage.getPkgName());
    }
    private int UNINSTALL_REQUEST_CODE = 1;


    private void requestUnInstall(String pkg) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + pkg));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d("TAG", "onActivityResult: user accepted the (un)install");
                blockedApps.remove(tempStorage);
                tempStorage=null;
                if (blockedApps.isEmpty()){
                    setResult(RESULT_OK);
                    finish();
                }else{
                    setupAdaptor();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("TAG", "onActivityResult: user canceled the (un)install");
                setResult(RESULT_CANCELED);
                finish();
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d("TAG", "onActivityResult: failed to (un)install");
                setupAdaptor();
            }
        }
    }
}
