package com.shekhargulati.missingkidtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class PreviewActivity extends AppCompatActivity {

    public static final String CAPTURED_PHOTO_PATH = "capturedPhotoPath";
    private String capturedPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent intent = getIntent();
        this.capturedPhotoPath = intent.getStringExtra(CAPTURED_PHOTO_PATH);
        setPic();
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
                        Log.e(getString(R.string.app_name), "Exception occurred while loading image", e);
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

}
