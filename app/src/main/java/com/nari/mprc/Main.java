package com.nari.mprc;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.nari.android.c2dm.GCM_Send;
import com.nari.camera.CameraActivity;
import com.nari.database.DBHandler;
import com.nari.gmail.GMailSender2;
import com.nari.util.AllConfig;
import com.nari.util.AnnotationView;
import com.nari.util.BackPressCloseHandler;
import com.nari.util.ConfigSetting;
import com.nari.util.FileUtil;
import com.nari.util.MarketVersionChecker;
import com.nari.util.MyLocation;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * 참고 페이지 : http://webnautes.tistory.com/1011
 *
 */
public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;
    static final LatLng SEOUL = new LatLng(37.56, 126.97);
    private static final String TAG = "MAIN";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int REQUEST_CODE_LOCATION = 2;
    ProgressDialog dialog;
    boolean ChkUse1, ChkUse2, ChkUse3, ChkUse4, ChkUse5, ChkUse6 ;
    String emailAddr ;
    Geocoder coder;
    boolean isUSIM;
    int iErrCode ;
    SupportMapFragment mapFragment;
    static TelephonyManager tMgr ;
    static SharedPreferences pref ;
    SharedPreferences mainPreference;
    String phoneNum ;
    DBHandler dbHandler ;
    Toolbar toolbar ;
    // BackPress Check
    private BackPressCloseHandler backPressCloseHandler;

    String deviceVersion;
    String storeVersion;
    private BackgroundThread mBackgroundThread;

    String mesg_uses1 = "" ;
    String mesg_uses2 = "" ;
    String mesg_uses3 = "" ;
    String mesg_uses4 = "" ;
    String mesg_uses5 = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mesg_uses1 = getStringID(R.string.mesg_uses1) ;
        mesg_uses2 = getStringID(R.string.mesg_uses2) ;
        mesg_uses3 = getStringID(R.string.mesg_uses3) ;
        mesg_uses4 = getStringID(R.string.mesg_uses4) ;
        mesg_uses5 = getStringID(R.string.mesg_uses5) ;

        // 뒤로가기 버튼 처리를 위해서
        backPressCloseHandler = new BackPressCloseHandler(this);

        // 메뉴 구성 부분.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(Main.this)
                        .setTitle(R.string.app_name_kr)
                        .setMessage(Html.fromHtml( getResources().getString(R.string.mesg_help1) + "<br><a href='http://6k2emg.blog.me'>http://6k2emg.blog.me</a>" +
                                "<br>" +  getResources().getString(R.string.mesg_help2) + " <a href='mailto:" + AllConfig.C2DM_SENDER + "'>" + AllConfig.C2DM_SENDER + "</a><br>"))
                        .setIcon(R.drawable.soo).setCancelable(false)
                        .setNegativeButton(getResources().getString(R.string.close), null).show();
            }
        });

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                Toast.makeText(Main.this, "권한 획득...", Toast.LENGTH_LONG);
                reStart() ;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                Toast.makeText(Main.this, getStringID(R.string.label_permission_denied) + "\n" + deniedPermissions.toString(), Toast.LENGTH_LONG);
                /*
                new AlertDialog.Builder(Main.this)
                        .setTitle(R.string.app_name_kr)
                        .setMessage(Html.fromHtml( getStringID(R.string.label_permission_denied) + "\n" + deniedPermissions.toString() ))
                        .setIcon(R.drawable.soo).setCancelable(false)
                        .setNegativeButton(getStringID(R.string.label_close), null).show();
                */
                //finish();

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }

        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleConfirmText("Permission Setting !!!")
                .setDeniedCloseButtonText("Close")
                .setGotoSettingButton(true)
                .setGotoSettingButtonText("Setting")
                .setDeniedMessage(getStringID(R.string.mesg_permission_agree1) + "\n\n" + getStringID(R.string.mesg_permission_agree2))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.CAMERA
                        , Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.READ_CALENDAR
                        , Manifest.permission.WRITE_CALENDAR
                        , Manifest.permission.READ_CONTACTS
                        , Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.SEND_SMS
                        , Manifest.permission.RECEIVE_SMS
                        , Manifest.permission.READ_SMS
                        , Manifest.permission.READ_CALL_LOG
                        , Manifest.permission.WRITE_CALL_LOG
                        , Manifest.permission.SYSTEM_ALERT_WINDOW // 2017.02.06 이건 설정을 해도 계속해서 설정화면으로 가도록 하고 있음 : 설정 - 어플리케이션 - 더보기 - 다른앱위에 표시 가능 설정 하면 됨
                        // 이건 권한을 요청할 수 없는 건가???  , Manifest.permission.RECEIVE_WAP_PUSH
                        , Manifest.permission.RECEIVE_MMS
                        , Manifest.permission.READ_PHONE_STATE)
                .check();

    }

    /**
     * onCreate() 에서 하는 것들은 전부다 이쪽으로 옮겨옴...
     */
    public void reStart() {

        timeThread();

        // 2016.11.29 playstore 버전 확인 하는 방법
        mBackgroundThread = new BackgroundThread();
        mBackgroundThread.start();

        // google Admop 2016.11.12
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                try {
                    String msg = showLocationName(location);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    drawMarker(location);
                } catch (Exception e) {

                }

            }
        };

        Log.d(TAG, "Service start =======================================================");
        PackageManager pm = this.getPackageManager();
        /* background 로 자동 실행되게 ??? : package name 은 다른 곳으로 옮기게 되면 같이 옮겨야함 */
        ComponentName componentName = new ComponentName("com.nari.mprc", "com.nari.autoreply.SmsMessageReceiver");
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        /* 2016.09.13 GPS수신*/
        ComponentName componentName1 = new ComponentName("com.nari.mprc", "com.nari.mprc.LocationReceiver");
        pm.setComponentEnabledSetting(componentName1,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Log.d(TAG, "Service start end =======================================================");

        if (isServiceRunningCheck()) {
            Log.d(TAG, "GpsBackGroundService is Live!!!");
        } else {
            Log.d(TAG, "GpsBackGroundService is Die!!!");
            // 강제로 백그라운드로 실행 하기.
            Intent gpsBackGroundService = new Intent(this, GpsBackGroundService.class);
            gpsBackGroundService.putExtra("TAG", "1");
            startService(gpsBackGroundService);
        }

        tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if(checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS },
                    MY_PERMISSION_REQUEST_STORAGE);
        } else {
            AllConfig.PHONE_NUMBER = tMgr.getLine1Number();
        }
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        ChkUse1 = pref.getBoolean("UserSelected1", false);
        ChkUse2 = pref.getBoolean("UserSelected2", false);
        ChkUse3 = pref.getBoolean("UserSelected3", false);
        ChkUse4 = pref.getBoolean("UserSelected4", false);
        ChkUse5 = pref.getBoolean("UserSelected5", false);
        ChkUse6 = pref.getBoolean("UserSelected6", false);
        emailAddr = pref.getString("EmailAddr",AllConfig.C2DM_SENDER);
        if (!ChkUse1) {
            Intent AnnotationView = new Intent(this, com.nari.util.AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse1);
            startActivityForResult(AnnotationView, iErrCode);
        } else if (!ChkUse2) {
            Intent AnnotationView = new Intent(this, com.nari.util.AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse2);
            startActivityForResult(AnnotationView, iErrCode);
        } else if (!ChkUse3) {
            Intent AnnotationView = new Intent(this, AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse3);
            startActivityForResult(AnnotationView, iErrCode);
        } else if (!ChkUse4) {
            Intent AnnotationView = new Intent(this, AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse4);
            startActivityForResult(AnnotationView, iErrCode);
        } else if (!ChkUse5) {
            Intent AnnotationView = new Intent(this, AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse5);
            startActivityForResult(AnnotationView, iErrCode);
        } else if (!ChkUse6) {
            Intent AnnotationView = new Intent(this, AnnotationView.class);
            AnnotationView.putExtra("UserSelected", ChkUse6);
            startActivityForResult(AnnotationView, iErrCode);
        }

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        View sView = navView.getHeaderView(0);
        TextView lblUserName = (TextView) sView.findViewById(R.id.lblUserName);
        TextView lblUserEmail = (TextView) sView.findViewById(R.id.lblUserEmail);
        if (!emailAddr.equals(AllConfig.C2DM_SENDER)) {
            String[] sName = emailAddr.split("@");
            lblUserName.setText(sName[0].toString());
            lblUserEmail.setText(emailAddr);
        }

        Uri calendars = null;

        if(checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR },
                    MY_PERMISSION_REQUEST_STORAGE);
        }

        if (Build.VERSION.SDK_INT == 7) {
            calendars = Uri.parse("content://calendar/calendars");
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars");
        }

        String[] projection_calendars = null;
        try {
            Cursor Cursor_calendars = null;
            if (Build.VERSION.SDK_INT < 14) {
                projection_calendars = new String[]{"_id", "name", "_sync_account_type"};
                Cursor_calendars = getContentResolver().query(calendars, projection_calendars, "selected=1", null, null);
            } else {
                projection_calendars = new String[]{"_id", "name", "account_type"};
                Cursor_calendars = getContentResolver().query(calendars, projection_calendars, "visible=1", null, null);
            }

            if (Cursor_calendars.moveToFirst()) {
                boolean chk_google = false;
                int[] _id = new int[Cursor_calendars.getCount()];
                String[] calendars_name = new String[Cursor_calendars.getCount()];
                String[] _sync_account_type = new String[Cursor_calendars.getCount()];

                for (int i = 0; i < calendars_name.length; i++) {
                    _id[i] = Cursor_calendars.getInt(0);
                    calendars_name[i] = Cursor_calendars.getString(1);
                    _sync_account_type[i] = Cursor_calendars.getString(2);
                    Log.d(TAG, "_id [" + _id[i] + "] Sync TYPE [" + _sync_account_type[i].toString() + "] Calendar Name [" + calendars_name[i].toString() + "]");
                    if (_sync_account_type[i].toString().equals("com.google")) {
                        chk_google = true;
                        if (calendars_name[i].toString().indexOf("@") > 0) {
                            mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor ed = mainPreference.edit();
                            ed.putString("GoogleCalendarId", String.valueOf(_id[i]));
                            ed.commit();
                        }
                    }
                    Cursor_calendars.moveToNext();
                }
                Cursor_calendars.close();

                if (!chk_google) {

                    DialogInterface.OnClickListener mClickLeft =
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    //	finish() ;
                                }
                            };

                    DialogInterface.OnClickListener mClickRight =
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            };

                    new AlertDialog.Builder(Main.this)
                            .setTitle(getStringID(R.string.Notice))
                            .setMessage(getStringID(R.string.mesg_not_sync))
                            .setPositiveButton(getStringID(R.string.label_Confirm), mClickLeft)
                            .setNegativeButton(getStringID(R.string.label_cancel), mClickRight)
                            .show();
                }
            }

        } catch (Exception e) {

            // 에러가 나는 경우 화면이 없어서 아무것도 하면 안됨.
        }

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getApplicationContext(), locationResult);

        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {// 유심이 없다.
            isUSIM = false;
        } else {//유심이 있다.
            isUSIM = true;
            phoneNum = telManager.getLine1Number();
        }

        if (isUSIM) registGCM(); // 이것 권한이랑 연관이 있을까 ?

    }

    /**
     * 권한 획득에 실폐 했다면 끝내야...
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "requestCode=" + requestCode) ;
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e(TAG, "권한 획득 완료") ;

                } else {

                    Log.e(TAG, "Permission always deny");

                    // 권한이 없는 경우는 프로그램을 종료 한다. 아무것도 보여줄 수 없다.

                    // finish();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googlemap) {
        mMap = googlemap ;

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    MY_PERMISSION_REQUEST_STORAGE);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.addMarker(new MarkerOptions().position(SEOUL).title("Seoul"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1500);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //CheckPermission();
        } else {
           startLocationUpdates();
        }
    }

    /**
     * 서비스가 살아 있는 지 확인 ? http://itmir.tistory.com/326
     * @return
     */
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.nari.mprc.GpsBackGroundService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
        Log.i(TAG, "");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng CURRENT_LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
        String msg = showLocationName(location);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        if (mMap != null) {
            mMap.clear();
            Marker seoul = mMap.addMarker(new MarkerOptions().position(CURRENT_LOCATION)
                    .title(msg));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 15));
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                    MY_PERMISSION_REQUEST_STORAGE);
        } else {
            LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
            if (locationAvailability.isLocationAvailable()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                Toast.makeText(this,"Location Unavialable", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void drawMarker(Location location) {

        //기존 마커 지우기
        if (mMap != null) {
            mMap.clear();
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            //currentPosition 위치로 카메라 중심을 옮기고 화면 줌을 조정한다. 줌범위는 2~21, 숫자클수록 확대
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 17));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

            String locatioTitle = showLocationName(location);

            //마커 추가
            mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .snippet(locatioTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(getStringID(R.string.label_location)));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            backPressCloseHandler.onBackPressed();
            // 이건 부터 클릭해서 종료하는 방법일때

            //Intent AdViewClose = new Intent(this, com.nari.mprc.AdViewClose.class) ;
            //startActivityForResult(AdViewClose, iErrCode);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case  R.id.nav_camera:
                Intent AnnotationView = new Intent (this, AnnotationView.class) ;
                AnnotationView.putExtra("UserSelected", ChkUse1);
                startActivityForResult(AnnotationView, iErrCode) ;
                break;
            case R.id.nav_gallery:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name_kr)
                        .setMessage(Html.fromHtml( getResources().getString(R.string.mesg_help1) + "<br><a href='http://6k2emg.blog.me'>http://6k2emg.blog.me</a>" +
                                "<br>" +  getResources().getString(R.string.mesg_help2) + " <a href='mailto:" + AllConfig.C2DM_SENDER + "'>" + AllConfig.C2DM_SENDER + "</a><br>"))
                        .setIcon(R.drawable.soo).setCancelable(false)
                        .setNegativeButton(getStringID(R.string.label_close), null).show();
                break;
            case R.id.nav_slideshow:
                Intent intentC2dm_send = new Intent (this, GCM_Send.class);
                intentC2dm_send.putExtra("TextIn", "C2dm_Send") ;
                startActivity(intentC2dm_send);
                break;

            case R.id.nav_manage:

                Intent configSetting = new Intent(this, ConfigSetting.class) ;
                configSetting.putExtra("UserSelected", ChkUse1);
                startActivity(configSetting) ;
                break;

            case R.id.menu_email_send_button:


                DialogInterface.OnClickListener mClickLeft =
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                // 2016.11.22 Firebase 오류 보고
                                if (mainPreference != null) {
                                    emailAddr = mainPreference.getString("EmailAddr", "test");
                                } else {
                                    emailAddr = "test" ;
                                }
                                if (isUSIM && !emailAddr.equals("test") && emailAddr.indexOf("@") > 0) { // 이 메일 주소가 없으면 하지 않는 다.
                                    try {

                                        final GMailSender2 sender = new GMailSender2(AllConfig.C2DM_SENDER,AllConfig.C2DM_SENDER_PSWD);

                                        Thread t = new Thread(new Runnable(){

                                            @Override
                                            public void run() {
                                                try {
                                                    sender.sendMail(
                                                            getStringID(R.string.mesg_mail_title),                    //subject.getText().toString(),
                                                            getString(GCM_BroadcastReceiver.registration_id),  //body.getText().toString(),
                                                            AllConfig.C2DM_SENDER,                                   //from.getText().toString(),
                                                            emailAddr                                                        //to.getText().toString()
                                                            , "", "", ""  // 첨부파일들이 있으면 보내준다고 하는데...
                                                    );
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        });

                                        t.start();

                                        new AlertDialog.Builder(Main.this)
                                                .setTitle(getStringID(R.string.label_mail_send_check))
                                                .setMessage(Html.fromHtml(
                                                        getStringID(R.string.mesg_mail_send_check) + " To:" + emailAddr + "<br>"))
                                                .setIcon(R.drawable.soo).setCancelable(false)
                                                .setNegativeButton(getStringID(R.string.label_close), null).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        new AlertDialog.Builder(Main.this)
                                                .setTitle(getStringID(R.string.mesg_mail_send_error))
                                                .setMessage(Html.fromHtml(
                                                        e.toString() + "<br>"))
                                                .setIcon(R.drawable.soo).setCancelable(false)
                                                .setNegativeButton(getStringID(R.string.label_close), null).show();
                                    }

                                } else {
                                    String altMesg = getStringID(R.string.mesg_no_usim);
                                    if ("".equals(emailAddr)) {
                                        altMesg = getStringID(R.string.mesg_confirm_email);
                                    }
                                    new AlertDialog.Builder(Main.this)
                                            .setTitle(R.string.app_name_kr)
                                            .setMessage(Html.fromHtml(altMesg))
                                            .setIcon(R.drawable.soo).setCancelable(false)
                                            .setNegativeButton(getStringID(R.string.label_close), null).show();
                                }
                            }
                        };

                DialogInterface.OnClickListener mClickRight =
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        };

                new AlertDialog.Builder(this)
                        .setTitle(getStringID(R.string.label_notice))
                        .setMessage(getStringID(R.string.mesg_send_to_mail) + "\n" + getStringID(R.string.mesg_proceed_confirm))
                        .setPositiveButton(getStringID(R.string.label_Confirm), mClickLeft)
                        .setNegativeButton(getStringID(R.string.label_cancel), mClickRight)
                        .show();


                break;

            case R.id.menu_send_dev_id:

                if (isUSIM) {
                    Intent SmsSend = new Intent (this, com.nari.autoreply.SmsSend.class);
                    SmsSend.putExtra("SMS_MESSAGE", GCM_BroadcastReceiver.registration_id);
                    SmsSend.putExtra("PHONE_SEARCH", GCM_BroadcastReceiver.registration_id);
                    startActivity(SmsSend);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.app_name_kr)
                            .setMessage(Html.fromHtml(
                                    getStringID(R.string.mesg_no_usim)))
                            .setIcon(R.drawable.soo).setCancelable(false)
                            .setNegativeButton(getStringID(R.string.label_close), null).show();
                }

                break;
            case R.id.menu_backup_data:

                if (isExternalStorageAvail()) {
                    new Main.ExportDatabaseTask().execute();
                    SystemClock.sleep(500);
                } else {
                    Toast.makeText(Main.this,
                            getStringID(R.string.mesg_unable_backup), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            
            case R.id.menu_restore_data:

                if (isExternalStorageAvail()) {
                    new Main.ImportDatabaseTask().execute();
                    SystemClock.sleep(500);
                } else {
                    Toast.makeText(Main.this,
                            getStringID(R.string.mesg_unable_backup), Toast.LENGTH_SHORT)
                            .show();
                }
                break ;
            case R.id.menu_take_picture:
                Intent cameraActivity = new Intent(this, CameraActivity.class);
                cameraActivity.putExtra("TAKEPIC", "TAKEPIC"); // TAKEPIC 이 다른 값이 가면 화면에 버튼이 동작해야 사진 촬영
                startActivity(cameraActivity);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        ChkUse1 = pref.getBoolean("UserSelected1", false) ;
        ChkUse2 = pref.getBoolean("UserSelected2", false) ;
        ChkUse3 = pref.getBoolean("UserSelected3", false) ;
        ChkUse4 = pref.getBoolean("UserSelected4", false) ;
        ChkUse5 = pref.getBoolean("UserSelected5", false) ;
        ChkUse6 = pref.getBoolean("UserSelected6", false) ;
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        if (ChkUse1 && ChkUse2 && ChkUse3 && ChkUse4 && ChkUse5 && ChkUse6) {
            Toast.makeText(this, "Confirm Your Message.", Toast.LENGTH_LONG) ;
        } else {

            DialogInterface.OnClickListener mClickLeft =
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            finish() ;
                        }
                    };

            DialogInterface.OnClickListener mClickRight =
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            finish() ;
                        }
                    };

            new AlertDialog.Builder(this)
                    .setTitle(getStringID(R.string.label_notice))
                    .setMessage(getStringID(R.string.mesg_agree_granted) + "\n" + getStringID(R.string.mesg_stop_apps))
                    .setPositiveButton(getStringID(R.string.label_Confirm), mClickLeft)
                    .setNegativeButton(getStringID(R.string.label_cancel), mClickRight)
                    .show() ;
        }

        /**
         * 호출했던 화면에서 종료에 해당 하는 값을 가지고 돌아온다면... 종료하는 것으로 다가.
         */
        switch(resultCode) {
            case 3:
            case 4: finish();
                break ;
        }
    }

    public void timeThread() {

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.setMessage(getStringID(R.string.label_wait_mo));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
            }
        }).start();
    }

    private  String showLocationName(Location loc) {

        double latitude = 33.526902  ; //  요기 어디 ?  제주도 우리집...
        double longitude = 126.589660 ;
        StringBuffer buff = new StringBuffer();

        coder = new Geocoder(this, Locale.KOREA);

        Log.d(TAG, "showLocationName ===============") ;
        try {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();

            Log.d(TAG, " latitude =" + String.valueOf(latitude) + ", longitude=" + String.valueOf(longitude)) ;

            List<Address> addrs = coder.getFromLocation(latitude, longitude, 1);
            for (Address addr : addrs) {
                int index = addr.getMaxAddressLineIndex();
                buff.append(addr.getAddressLine(index));
				/*
				for (int i = 0; i <= index; ++i) {
	                 buff.append(addr.getAddressLine(i));
	                 buff.append("\n");
	                 Log.d(TAG, "buff="+ buff.toString()) ;
				}
				*/
                buff.append("\n");
            }

        } catch (Exception e) {
            Log.d(TAG, "GeoCoder 오류 ...\n" + e.toString()) ;
        } finally  {
            //GeoPoint newPoint = new GeoPoint((int)(latitude * 1E6), (int)(longitude*1E6));
        }
        Log.d(TAG, "<<<<<<<<<<<<<<< showLocationName " + buff.toString()) ;

        return buff.toString();
    }

    private void registGCM() {
    	/* 내 번호 알아내기...*/
        if (isUSIM) {

            if(phoneNum.startsWith("+82")){
                phoneNum = phoneNum.replace("+82", "0");
            }
            Log.d(TAG, "내번호 <<<<<<<<<<<<<<<<<<<<\n" +  phoneNum + "\n>>>>>>>>>>>>>>");

            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);

            final String regId = GCMRegistrar.getRegistrationId(this);

            if("".equals(regId))   //구글 가이드에는 regId.equals("")로 되어 있는데 Exception을 피하기 위해 수정

                GCMRegistrar.register(this, GCMIntentService.SEND_ID);
            else {
                Log.d(TAG, regId);
                dbHandler = DBHandler.open(this) ;
                if (!dbHandler.checkC2dmPhoneNo(phoneNum)) {
                    dbHandler.insertC2dm_id(phoneNum, "id1", regId, regId) ;
                }
                dbHandler.close() ;
                GCM_BroadcastReceiver.registration_id = regId ;
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getStringID(R.string.app_name))
                    .setMessage(Html.fromHtml(
                            getStringID(R.string.mesg_no_usim)))
                    .setIcon(R.drawable.soo).setCancelable(false)
                    .setNegativeButton(getStringID(R.string.label_close), null).show();
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    private boolean isExternalStorageAvail() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private class ExportDatabaseTask extends AsyncTask<Void, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(Main.this);

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            dialog.setMessage(getStringID(R.string.mesg_backup_data));
            dialog.show();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Boolean doInBackground(final Void... args) {

            File dbFile = new File(Environment.getDataDirectory() + "/data/com.nari.mprc/databases/MPRC_DB");
            File exportDir = new File(Environment.getExternalStorageDirectory(), "MPRC_DB");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());

            try {
                file.createNewFile();
                FileUtil.copyFile(dbFile, file);
                return true;
            } catch (IOException e) {
                return false;
            }

        }

        // can use UI thread here
        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (success) {
                Toast.makeText(Main.this, getStringID(R.string.mesg_backup_complete), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Main.this, getStringID(R.string.mesg_backup_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(Main.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getStringID(R.string.mesg_recover_data));
            dialog.show();
        }

        // could pass the params used here in AsyncTask<String, Void, String> - but not being re-used
        @Override
        protected String doInBackground(final Void... args) {

            File dbBackupFile = new File(Environment.getExternalStorageDirectory() + "/MPRC_DB/MPRC_DB");
            if (!dbBackupFile.exists()) {
                return getStringID(R.string.mesg_recover_no_data);
            } else if (!dbBackupFile.canRead()) {
                return getStringID(R.string.mesg_recover_no_read);
            }

            File dbFile = new File(Environment.getDataDirectory() + "/data/com.nari.mprc/databases/MPRC_DB");
            if (dbFile.exists()) {
                dbFile.delete();
            }

            try {
                dbFile.createNewFile();
                FileUtil.copyFile(dbBackupFile, dbFile);
                return null;
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(final String errMsg) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (errMsg == null) {
                Toast.makeText(Main.this, getStringID(R.string.mesg_recover_complete), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Main.this, getStringID(R.string.mesg_recover_error) + errMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getString(String Dev_ID) {
        String return_value = null ;

        return_value =  mesg_uses1 + "\n" + mesg_uses2 + "\n" + "deviceToken <" + Dev_ID + ">" + mesg_uses3 + "\n" + mesg_uses4 + "\n" + mesg_uses5 ;

        return return_value;
    }

    /**
     * PlayStore 버전 확인하는 방법
     * 인터넷 펌 : http://dexx.tistory.com/124
     * 2016.11.29
     */
    public class BackgroundThread extends Thread {
        @Override
        public void run() {

            MarketVersionChecker marketVersionChecker = new MarketVersionChecker() ;
            // 패키지 네임 전달
            storeVersion = marketVersionChecker.getMarketVersionFast(getPackageName()).replaceAll(" ","").trim();

            // 디바이스 버전 가져옴
            try {
                deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            deviceVersionCheckHandler.sendMessage(deviceVersionCheckHandler.obtainMessage());
            // 핸들러로 메세지 전달
        }
    }

    private final DeviceVersionCheckHandler deviceVersionCheckHandler = new DeviceVersionCheckHandler(this);

    // 핸들러 객체 만들기
    private static class DeviceVersionCheckHandler extends Handler{
        private final WeakReference<Main> mainActivityWeakReference;
        public DeviceVersionCheckHandler(Main mainActivity) {
            mainActivityWeakReference = new WeakReference<Main>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            Main activity = mainActivityWeakReference.get();
            if (activity != null) {
                activity.handleMessage(msg);
                // 핸들메세지로 결과값 전달
            }
        }
    }

    private void handleMessage(Message msg) {
        //핸들러에서 넘어온 값 체크
        if (storeVersion != null) Log.d(TAG, "storeVersion=[" + storeVersion + "]");
        if (deviceVersion != null) Log.d(TAG, "deviceVersion=[" + deviceVersion + "]");
        Log.d(TAG, " msg Check=" + storeVersion.compareTo(deviceVersion));
        if (storeVersion != null && deviceVersion != null && storeVersion.compareTo(deviceVersion) > 0) {
            // 업데이트 필요
            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light));

            alertDialogBuilder.setTitle(getStringID(R.string.label_update));alertDialogBuilder
                    .setMessage(getStringID(R.string.mesg_new_version))
                    .setPositiveButton(getStringID(R.string.mesg_update_href), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 구글플레이 업데이트 링크
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            finish();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        } else {
            // 업데이트 불필요
        }
    }

    public String getStringID(int ResourceID) {
        return getResources().getString(ResourceID);
    }

}
