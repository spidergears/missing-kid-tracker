package com.shekhargulati.missingkidtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String IMAGE_EXTENSION = ".jpg";
    private String capturedPhotoPath;
    private final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12345;
    private final String tag = "MissingKid:Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    saveImageAndLaunchPreviewActivity();
                }
                else{
                    Toast.makeText(MainActivity.this, "You need to allow permission to Write to External Storage", Toast.LENGTH_SHORT)
                            .show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void takePhoto() {

        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //If application exists to capture image
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //if does not have permission to write to external storage
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Application needs access to READ/WRITE LocalStorage to store images.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissionWriteToLocalStorage();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
                else {
                    requestPermissionWriteToLocalStorage();
                }
                return;
            }
            saveImageAndLaunchPreviewActivity();
        }
    }

    private String getImageName() {
        return "IMG-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-";
    }

    private void requestPermissionWriteToLocalStorage(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }
    private void saveImageAndLaunchPreviewActivity() {
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
            Log.e(tag, "Exception encountered while creating file for storing image", ioe);
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
