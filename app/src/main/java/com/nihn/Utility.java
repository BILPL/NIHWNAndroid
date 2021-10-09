package com.nihn;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class Utility {
    public static boolean IsNullOrEmpty(String str) {
        Boolean value = false;
        try {
            if (null == str || str.length() == 0 || str == "" || str.isEmpty()) {
                return true;
            }

        } catch (Exception ex) {

        }
        return value;
    }

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String GetUTCDateTimeWithFormat() {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String utcTime = sdf.format(new Date());

            return utcTime;
        } catch (Exception ex) {
            Log.e("GetUTCDateTimeWith: ", ex.getMessage());
            throw ex;
        }
    }

    public static Date ConvertStringToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }

    public static File GetFileStored(Activity activity, String json, String download){
        File file = null;
        try{
            String direct64 = json.replace("data:image/jpeg;base64,", Constants.EMPTY_STRING);

            byte[] decodedString = Base64.decode(direct64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+ "/NIHWNMedia"); //Creates app specific folder

            if(!path.exists()) {
                path.mkdirs();
            }
            String imgPath = path.getAbsolutePath();
            if(IsNullOrEmpty(download)){
                file = new File(imgPath, "Report.pdf");
            }else{
                file = new File(imgPath, UUID.randomUUID().toString() + ".pdf");
            }

            if (file.exists())
                file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedString);
            //decodedByte.compress (Bitmap.CompressFormat.pdf, 100, fos);
            fos.flush();
            fos.close();

        }catch(Exception ex){
            Log.e(TAG, "GetFileStored: "+ ex.getMessage() );
        }
        return  file;
    }

    /// <summary>
    /// Sets the given value in user preferences using given key.
    /// </summary>
    /// <returns><c>true</c>, if user preferences was set, <c>false</c> otherwise.</returns>
    /// <param name="key">Preference key.</param>
    /// <param name="value">Preference value.</param>
    public static boolean SetUserPreferences(String key, String value, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
            sprefs.edit().putString(key, value).commit();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /// <summary>
    /// This method for Get the stores the data from shared preferences based on key value
    /// </summary>
    /// <param name="key">store key value</param>
    /// <returns>it return's stored string</returns>
    public static String GetUserPreference(String key, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);

            return sprefs.getString(key, Constants.EMPTY_STRING);
        } catch (Exception ex) {
            return null;
        }
    }

    /// <summary>
    /// Gets device unique id.
    /// </summary>
    /// <param name="context">Activity context.</param>
    /// <returns>Device unique id.</returns>
    public static String GetDeviceUniqueId(Context context) {
        String value = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return value;
    }

    public static void RegisterOrUpdateDeviceToken(String userId, Context context) {
        final Boolean result = false;
        if (Utility.IsNullOrEmpty(userId))
            return;
        String deviceUniqueId = GetDeviceUniqueId(context);
        String deviceToken = GetUserPreference(Constants.DeviceToken, context);
        final String endPoint = "https://www.phleby.co/" + "/api/UnsecuredAPI/RegisterOrUpdateDeviceToken?personId=" + userId + "&deviceType=ANDROID" + "&deviceUniqueId=" + deviceUniqueId + "&deviceToken=" + deviceToken;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HttpURLConnection connection = null;
                    URL url = new URL(endPoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("GET"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                    connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                    connection.connect();

                    int response = connection.getResponseCode();
                    if (response >= 200 && response <= 399) {

                    } else {

                    }

                } catch (Exception ex) {
                    Log.e(TAG, "RegisterOrUpdateDeviceToken: ");
                }
            }

        }).start();
    }

    public static Boolean isContactServiceCallGoingOn = false;

    public static void SyncContactsToServer(final Activity activity) {
        try {
            String lastSyncDate = Utility.GetUserPreference(Constants.ContactsLastSyncDateTime, activity);
            if (!IsNullOrEmpty(lastSyncDate)) {
                long differenceinTime = (ConvertStringToDate(GetUTCDateTimeWithFormat()).getTime() - Utility.ConvertStringToDate(lastSyncDate).getTime()) / (1000 * 60 * 60) % 24;
                Log.d(TAG, "SyncContactsToServer: " + differenceinTime);
                if (differenceinTime < 3)
                    return;
            }

            MainActivity.mainActivity.ReadAndContacts();
            final List<User> finalcontacts = MainActivity.mainActivity.mydb.getAllContacts();
            if (finalcontacts == null || finalcontacts.size() <= 0) {
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String UserId = Utility.GetUserPreference(Constants.UseruniqueId, activity);//"0C5E0AAB-DBA6-4A35-AD0C-437DFE50C05B"; //
                        if (Utility.IsNullOrEmpty(UserId))
                            return;
                        String endPoint = "https://www.phleby.co/api/UnsecuredAPI/SyncContacts?userId=" + UserId;

                        HttpURLConnection connection = null;
                        URL url = new URL(endPoint);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestMethod("POST"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                        connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                        Gson gson = new Gson();
                        String content = gson.toJson(finalcontacts);

                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                        wr.write(content.getBytes());
                        connection.connect();
                        InputStream is;
                        String resp = connection.getResponseMessage();
                        int response = connection.getResponseCode();
                        if (response >= 200 && response <= 399) {
                            Utility.SetUserPreferences(Constants.ContactsLastSyncDateTime, GetUTCDateTimeWithFormat(), activity);
                            wr.flush();
                            wr.close();
                            //return is = connection.getInputStream();
                            return;
                        } else {
                            wr.flush();
                            wr.close();
                            //return is = connection.getErrorStream();
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    } finally {
                        isContactServiceCallGoingOn = false;
                    }
                }

            }).start();

        } catch (Exception ex) {
            Log.e("SyncContactsToServer: ", ex.getMessage());
        }
    }

    /// <summary>
    /// This method used to open web url
    /// </summary>
    /// <param name="context"></param>
    /// <param name="url"></param>
    public static void OpenURL(Activity context, String url) {
        try {
            if (IsNullOrEmpty(url))
                return;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);

        } catch (Exception ex) {
            Log.e(TAG, "OpenURL: ");
        }
    }

    //*****************START :: eMail Fetch :: Nawin******************
    static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }


    //*****************END:: eMail Fetch :: Nawin******************
    public static void CheckMediaPermissions(Activity activity, int read_phone_State) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                activity.requestPermissions(new String[]{
                        // Android.Manifest.Permission.Camera ,
                        Manifest.permission.MANAGE_DOCUMENTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,

                }, read_phone_State);
            }
        } catch (Exception ex) {
            Log.e("CheckMediaPermissions: ", ex.getMessage());
        }
    }

    public static void ShareonWhatsapp(Activity activity, String title, File file) {
        try {
            String imgPath = GetImagePathStored(activity, title);
            if (IsNullOrEmpty(imgPath))
                return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //File file = GetFileStored(activity, json, "");
                uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.parse(imgPath);
            }
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_TEXT, title);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(activity, "Whatsapp have not been installed.", Toast.LENGTH_LONG);
            }

        } catch (Exception ex) {
            Log.e("ShareonWhatsapp: ", ex.getMessage());
        }
    }

    public static String GetImagePathStored(Activity activity, String json) {
        String imgPath = Constants.EMPTY_STRING;
        try {
            String direct64 = json.replace("data:image/jpeg;base64,", Constants.EMPTY_STRING);

            byte[] decodedString = Base64.decode(direct64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/BLMedia"); //Creates app specific folder

            if (!path.exists()) {
                path.mkdirs();
            }
            imgPath = path.getAbsolutePath();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, imgPath);
            activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            File file = new File(imgPath, "Thumbnail.jpg");
            if (file.exists())
                file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception ex) {

        }
        return imgPath;
    }

    public static File saveTempBitmap(Bitmap bitmap, String type) {
        File file = null;
        if (isExternalStorageWritable()) {
            file = saveImage(bitmap);
        } else {
            //prompt the user or do something
        }
        return file;
    }

    private static File saveImage(Bitmap finalBitmap) {
        File file = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/BLMedia"); //Creates app specific folder

        if (!path.exists()) {
            path.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = timeStamp + ".jpg";
        String imagePath = path.getAbsolutePath();
        file = new File(imagePath, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void CloseDialog(DialogInterface dialog, WebView webview) {
        try {
            dialog.dismiss();
            dialog = null;
            webview.destroy();
        } catch (Exception ex) {
            Log.e(TAG, "CloseDialog: ");
        }
    }

    public static String getCookie(String siteName,String CookieName){
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if(cookies != null){
            String[] temp=cookies.split(";");
            for (String item : temp ){
                if(item.contains(CookieName)){
                    String[] temp1=item.split("=");
                    CookieValue = temp1[1];
                }
            }
        }
        return CookieValue;
    }




    public static class WebAppInterface {
        Context mContext;
        public WebAppInterface(Context context) {
            mContext = context;
        }
        @JavascriptInterface   // must be added for API 17 or higher
        public void showAndroidToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    public static class MyCustomDownloadListener implements DownloadListener {
        Activity activity;
        public MyCustomDownloadListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onDownloadStart( String url, String userAgent, String contentDisposition, String mimetype, long contentLength)
        {
            try {
                String direct64 = url.replace("data:image/octet-stream;base64,", Constants.EMPTY_STRING).replace("data:image/png;base64,", Constants.EMPTY_STRING).replace("data:image/jpeg;base64,", Constants.EMPTY_STRING);

                //GetFileStored(activity, direct64, "download");
            }catch (Exception ex){
                Log.e(TAG, "onDownloadStart: " + ex.getMessage() );
            }
        }
    }
}

