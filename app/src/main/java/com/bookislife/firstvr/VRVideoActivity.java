package com.bookislife.firstvr;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.widgets.video.VrVideoEventListener;
import com.google.vrtoolkit.cardboard.widgets.video.VrVideoView;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by SidneyXu on 2016/05/02.
 */
public class VRVideoActivity extends Activity {

    public static final String TAG = VRVideoActivity.class.getSimpleName();

    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    private boolean loaded;
    private SeekBar seekBar;
    private TextView statusText;
    private VrVideoView vrVideoView;
    private boolean isPaused;
    private VideoLoaderTask backgroundVideoLoaderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        statusText = (TextView) findViewById(R.id.status_text);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    vrVideoView.seekTo(progress);
                    updateStatusText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        vrVideoView = (VrVideoView) findViewById(R.id.pano_view);
        vrVideoView.setEventListener(new VrVideoEventListener() {
            @Override
            public void onLoadSuccess() {
                loaded = true;
                seekBar.setMax((int) vrVideoView.getDuration());
                updateStatusText();
            }

            @Override
            public void onLoadError(String errorMessage) {
                loaded = false;
                Toast.makeText(VRVideoActivity.this, "Error occurs: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error occurs: " + errorMessage);
            }

            @Override
            public void onCompletion() {
                vrVideoView.seekTo(0);
            }

            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) vrVideoView.getCurrentPosition());
            }

            @Override
            public void onClick() {
                togglePause();
            }
        });

        loadVideo(null);
    }

    private void loadVideo(Uri fileUri) {
        if (backgroundVideoLoaderTask != null) {
            backgroundVideoLoaderTask.cancel(true);
        }
        backgroundVideoLoaderTask = new VideoLoaderTask();
        backgroundVideoLoaderTask.execute(fileUri);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(STATE_PROGRESS_TIME, vrVideoView.getCurrentPosition());
        outState.putLong(STATE_VIDEO_DURATION, vrVideoView.getDuration());
        outState.putBoolean(STATE_IS_PAUSED, isPaused);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        vrVideoView.seekTo(progressTime);
        seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
        seekBar.setProgress((int) progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            vrVideoView.pauseVideo();
        }
    }

    private void togglePause() {
        if (isPaused) {
            vrVideoView.playVideo();
        } else {
            vrVideoView.pauseVideo();
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format(Locale.ENGLISH, "%.2f", vrVideoView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(vrVideoView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        vrVideoView.pauseRendering();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        vrVideoView.resumeRendering();
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vrVideoView.shutdown();
    }

    class VideoLoaderTask extends AsyncTask<Uri, Void, Exception> {
        @Override
        protected Exception doInBackground(Uri... uri) {
            try {
                if (uri == null || uri[0] == null) {
                    vrVideoView.loadVideoFromAsset("congo.mp4");
                } else {
                    vrVideoView.loadVideo(uri[0]);
                }
            } catch (IOException e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (null != e) {
                loaded = false;
                vrVideoView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VRVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                e.printStackTrace();
            }
        }
    }

}
