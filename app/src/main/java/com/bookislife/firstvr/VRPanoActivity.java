package com.bookislife.firstvr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;

/**
 * Created by SidneyXu on 2016/05/02.
 */
public class VRPanoActivity extends Activity {

    public static final String TAG = VRPanoActivity.class.getSimpleName();

    private VrPanoramaView vrPanoramaView;
    private boolean loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pano);

        vrPanoramaView = (VrPanoramaView) findViewById(R.id.pano_view);
        vrPanoramaView.setEventListener(new VrPanoramaEventListener() {
            @Override
            public void onLoadSuccess() {
                loaded = true;
            }

            @Override
            public void onLoadError(String errorMessage) {
                loaded = false;
                Toast.makeText(VRPanoActivity.this, "Error occurs: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Error occurs:  " + errorMessage);
            }
        });

        // Prepare image
        Glide.with(VRPanoActivity.this)
                .load(Uri.parse("file:///android_asset/andes.jpg"))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        // Load into VrPanoramaView
                        VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
                        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
                        vrPanoramaView.loadImageFromBitmap(resource, panoOptions);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        vrPanoramaView.pauseRendering();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vrPanoramaView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vrPanoramaView.shutdown();
    }

}
