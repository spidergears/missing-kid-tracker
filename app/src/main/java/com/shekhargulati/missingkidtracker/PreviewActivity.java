package com.shekhargulati.missingkidtracker;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    public static final String CAPTURED_PHOTO_PATH = "capturedPhotoPath";
    public static final String CAPTURE_LOCATION  = "captureLocation";
    private String capturedPhotoPath;
    private Location captureLocation;
    private ShareActionProvider shareActionProvider;
    private Intent shareIntent = new Intent();
    private final String tag = "MissingKid:Preview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent intent = getIntent();
        this.capturedPhotoPath = intent.getStringExtra(CAPTURED_PHOTO_PATH);
        this.captureLocation = intent.getParcelableExtra(CAPTURE_LOCATION);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(capturedPhotoPath)));
        shareIntent.setType("image/jpeg");

        setPic();
        setLocation();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.preview_menu, menu);
        MenuItem shareMenuItem = menu.findItem(R.id.menu_share_photo);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        setShareIntent(shareIntent);
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void setPic() {
        ImageView previewView = (ImageView) this.findViewById(R.id.image_preview);
        File file = new File(capturedPhotoPath);
        Glide
                .with(this)
                .load(file)
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.e(tag, "Exception occurred while loading image", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .error(R.drawable.kid)
                .into(previewView);
    }

    private void setLocation(){
        TextView locationTextView = (TextView) this.findViewById(R.id.location_textView);

        if (captureLocation != null){
            String locationText = String.format("Latitude:%f, Longitude:%f\nAccurate upto :%.2f meters",
                    captureLocation.getLatitude(),
                    captureLocation.getLongitude(),
                    captureLocation.getAccuracy()
                    );
            locationTextView.setText(locationText);
        }
        else {
            locationTextView.setText("Could not recognise location. Please try again");
        }
    }

}
