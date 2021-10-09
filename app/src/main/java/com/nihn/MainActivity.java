package com.nihn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.androidbrowserhelper.trusted.LauncherActivity;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    String urlscheme = "";
    String deviceId;
    ArrayList<User> listOfContacts = new ArrayList<>();
    public DatabaseHelper mydb;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            mainActivity = this;
            Utility.SetUserPreferences(Constants.DeviceId, Utility.GetDeviceUniqueId(this), this);
            String regId =Utility.GetUserPreference(Constants.registrationID, this);

            if (null == mydb)
                mydb = new DatabaseHelper(this);

            // ATTENTION: This was to handle app links from out side like Notifications and deep links.
            Intent appLinkIntent = getIntent();
            Uri appLinkData = appLinkIntent.getData();

            if(null == appLinkData){
                urlscheme = "https://mobile.phleby.co?ct=android";
            }else{
                urlscheme = appLinkData.toString() + "?clienttype=android";
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        StartPWA(mainActivity);
                        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                            Utility.SyncContactsToServer(mainActivity);
                        } else {
                            SplashActivity.splashActivity.requestPermission();
                        }

                    } else {
                        Intent intent = new Intent(mainActivity, SplashActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                }
            });

            deviceId = Utility.GetDeviceUniqueId(this);

        }catch (Exception e){
            ToastNotify("oncreate error");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void StartPWA(Activity context){
        try {
            Intent intent = new Intent(this, com.google.androidbrowserhelper.trusted.LauncherActivity.class);
            intent.setData(Uri.parse(urlscheme));
            intent.putExtra("url",urlscheme);
            startActivity(intent);
            context.finish();
        }catch(Exception ex){
            Log.e(TAG, "run: " + ex.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            ToastNotify("called2");
        }catch (Exception e){

        }
    }
    public void ToastNotify(final String notificationMessage) {
        Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
    }
    /**
     * Return a Bitmap representation of the Drawable. Based on Android KTX.
     */
    private Bitmap toBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Rect oldBounds = new Rect(drawable.getBounds());

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(new Canvas(bitmap));

        drawable.setBounds(oldBounds);
        return bitmap;
    }

    //This method will let you know the concept of reading contacts
    public ArrayList ReadAndContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                        if ((cur != null ? cur.getCount() : 0) > 0) {
                            while (cur != null && cur.moveToNext()) {
                                User contact = new User();
                                String id = cur.getString(
                                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));
                                contact.Name = name;
                                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                    Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{id}, null);
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                                        if (Utility.IsNullOrEmpty(phoneNo)) {
                                            continue;
                                        }

                                        if (phoneNo.contains("*") || phoneNo.contains("#")) {
                                            continue;
                                        }

                                        phoneNo = phoneNo.trim().replace(" ", Constants.EMPTY_STRING).replace("+", Constants.EMPTY_STRING).replace("(", Constants.EMPTY_STRING).replace(")", Constants.EMPTY_STRING).trim();
                                        if (phoneNo.length() < 10) {
                                            continue;
                                        }

                                        if (phoneNo.length() == 10) {
                                            phoneNo = phoneNo;
                                        }
                                        contact.MobileNumber = phoneNo;
                                        contact.F5 = "ANDROID";
                                        contact.F6 = deviceId;
                                    }
                                    pCur.close();
                                }
                                if (!Utility.IsNullOrEmpty(contact.MobileNumber)) {
                                    listOfContacts.add(contact);
                                    mydb.addContact(contact);
                                }
                            }
                        }else{
                            Log.d(TAG, "run: Naveen closed");
                        }
                        if (cur != null) {
                            cur.close();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    }
                }

            }).start();
        } catch (Exception ex) {
            Log.e("getAllContacts: ", ex.getMessage());
        }

        return listOfContacts;
    }
}