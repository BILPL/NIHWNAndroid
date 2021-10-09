package com.nihn;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.androidbrowserhelper.trusted.LauncherActivity;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class DashboardActivity extends AppCompatActivity {


    public static DashboardActivity dashboardActivity;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent intent1 = this.getIntent();
        if (null != intent1 && null != intent1.getData() && null != intent1.getData().getHost()) {
            String urlscheme = intent1.getData().getHost();
            String scheme = intent1.getData().toString();
            switch (urlscheme){
                case "loginsuccess":
                    String uId = scheme.split("id=")[1].split("&CN=")[0];
                    Utility.SetUserPreferences(Constants.UseruniqueId, uId, this);
                    Utility.RegisterOrUpdateDeviceToken(uId, this);
                   // NotificationHub.setListener(new CustomNotificationListener());
                    //Here we are registering the notification with server
                    NotificationHub.start(this.getApplication(),NotificationSettings.HubName, NotificationSettings.HubListenConnectionString);

                    NotificationHub.setInstallationSavedListener(i -> {
                        Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show();
                        String regID = NotificationHub.getInstallationId();
                        Utility.SetUserPreferences(Constants.registrationID, regID, this);
                    });
                    NotificationHub.setInstallationSaveFailureListener(e -> Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show());
                    NotificationHub.addTag(uId);
                    break;
            }
        }
        this.finish();
    }

}