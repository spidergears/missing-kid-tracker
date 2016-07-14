package com.shekhargulati.missingkidtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int ACCESS_LOCATION_REQUEST_CODE = 1;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String writeExternalStorageRationale = "You haven't given us permission to use Storage, please enable the permission to store images.";
    private static final String accessLocationRationale = "You haven't given us permission to use Location, please enable the permission to geotag images.";

    private final String tag = "MissingKid:Main";
    private String capturedPhotoPath;

    private GoogleApiClient googleApiClient;
    private Location location;
    private final LocationRequest locationRequest =  LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(10000) //10 seconds
            .setFastestInterval(5000); //5 seconds


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(tag, "GoogleApiClient connection failed with error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(tag, "GoogleApiClient Connected");
        captureCurrentLocation();
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        Log.i(tag, "LocationService: Location changed to:  " + newLocation.toString());
        location = newLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(tag, "GoogleApiClient Connection Suspended");
        googleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_take_photo:
                takePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        showRequestPermissionRationale(writeExternalStorageRationale, "requestPermissionWriteExternalStorage");
                        return;
                    }
                    else
                        Toast.makeText(MainActivity.this, writeExternalStorageRationale, Toast.LENGTH_LONG).show();
                }
            case ACCESS_LOCATION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showRequestPermissionRationale(accessLocationRationale, "requestPermissionAccessLocation");
                        return;
                    }
                    else
                        Toast.makeText(MainActivity.this, accessLocationRationale, Toast.LENGTH_LONG).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void takePhoto() {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //If application exists to capture image
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasLocationAccessPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED && hasLocationAccessPermission == PackageManager.PERMISSION_GRANTED)
                captureImageAndLaunchPreviewActivity();
            else {
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
                    requestPermissionWriteExternalStorage();
                if (hasLocationAccessPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionAccessLocation();
                }
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Could not find any Camera application.", Toast.LENGTH_LONG).show();
        }
    }

    private String getImageName() {
        return "IMG-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-";
    }

    private void requestPermissionWriteExternalStorage(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    private void requestPermissionAccessLocation(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_LOCATION_REQUEST_CODE);
    }


    private void showRequestPermissionRationale(String message, final String permissionMethodName){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            java.lang.reflect.Method method = MainActivity.class.getDeclaredMethod(permissionMethodName);
                            method.setAccessible(true);
                            method.invoke(MainActivity.this);
                        }
                        catch(SecurityException se) {Log.e(tag, String.format("Encountered SecurityException when invoking [%s]", permissionMethodName), se);}
                        catch(NoSuchMethodException nsme) {Log.e(tag, String.format("Encountered SecurityException when invoking [%s]", permissionMethodName), nsme);}
                        catch(IllegalAccessException iae) {Log.e(tag, String.format("Encountered SecurityException when invoking [%s]", permissionMethodName), iae);}
                        catch(InvocationTargetException ite) {Log.e(tag, String.format("Encountered SecurityException when invoking [%s]", permissionMethodName), ite);}
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void captureImageAndLaunchPreviewActivity() {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final String albumName = getString(R.string.app_name);
        final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        final String albumPath = galleryPath + File.separator + albumName;
        File albumDir = new File(albumPath);
        if (!albumDir.isDirectory() && !albumDir.mkdirs()) {
            Log.e(tag, String.format("Unable to create album directory at [%s]", albumPath));
            return;
        }
        try {
            File image = File.createTempFile(getImageName(), IMAGE_EXTENSION, albumDir);
            capturedPhotoPath = image.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        catch (IOException ioe){
            Log.e(tag, "Encountered IOException when creating image file", ioe);
        }
    }

    private void captureCurrentLocation(){
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        catch (SecurityException se){
            Log.e(tag, "Encountered SecurityException when accessing location", se);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("capturedPhotoPath", capturedPhotoPath);
        editor.commit();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        capturedPhotoPath = settings.getString("capturedPhotoPath", "");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(tag, String.format("Photo is successfully saved to [%s]", capturedPhotoPath));
            addPicToGallery();
            Intent previewIntent = new Intent(this, PreviewActivity.class);
            previewIntent.putExtra(PreviewActivity.CAPTURED_PHOTO_PATH, capturedPhotoPath);
            previewIntent.putExtra(PreviewActivity.CAPTURE_LOCATION, location);
            startActivity(previewIntent);
        }
    }

    private void addPicToGallery() {
        File f = new File(capturedPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
