package com.shekhargulati.missingkidtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String IMAGE_EXTENSION = ".jpg";
    private String capturedPhotoPath;

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

    private void takePhoto() {
        final String tag = getString(R.string.app_name);
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                final String albumName = getString(R.string.app_name);
                final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
                final String albumPath = galleryPath + File.separator + albumName;
                File albumDir = new File(albumPath);
                if (!albumDir.isDirectory() && !albumDir.mkdirs()) {
                    Log.e(tag, String.format("Unable to create album directory at [%s]", albumPath));
                    return;
                }
                File image = File.createTempFile(getImageName(), IMAGE_EXTENSION, albumDir);
                capturedPhotoPath = image.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } catch (IOException e) {
                Log.e(tag, "Exception encountered while creating file for storing image", e);
            }
        }
    }

    private String getImageName() {
        return "IMG-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-";
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
        final String tag = getString(R.string.app_name);
        if (resultCode == RESULT_OK) {
            Log.d(tag, String.format("Photo is successfully saved to [%s]", capturedPhotoPath));
            addPicToGallery();
            Intent previewIntent = new Intent(this, PreviewActivity.class);
            previewIntent.putExtra(PreviewActivity.CAPTURED_PHOTO_PATH, capturedPhotoPath);
            startActivity(previewIntent);
        }
    }

    private void addPicToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(capturedPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



}
