package com.app.thestream.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.app.thestream.Config;
import com.app.thestream.R;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
    }
    public void update(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.updateapplink));
        UpdateActivity.this.startActivity(browserIntent);


    }
}
