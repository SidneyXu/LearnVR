package com.bookislife.firstvr;

import android.content.Context;

import com.bookislife.firstvr.model.Cube;
import com.bookislife.firstvr.model.Floor;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class World {

    public static final float Z_NEAR = 0.1f;
    public static final float Z_FAR = 100.0f;

    public static final float CAMERA_Z = 0.01f;
    public static final float TIME_DELTA = 0.3f;

    public static final float YAW_LIMIT = 0.12f;
    public static final float PITCH_LIMIT = 0.12f;

    public static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    public static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    public static final float MIN_MODEL_DISTANCE = 3.0f;
    public static final float MAX_MODEL_DISTANCE = 7.0f;

    public Cube cube;
    public Floor floor;

    public final CardboardAudioEngine cardboardAudioEngine;
    private WorldRender render;

    public World(Context context) {
        // 3D audio
        cardboardAudioEngine = new CardboardAudioEngine(context, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);

        render = new WorldRender(this);
    }

    public void onDraw() {
        cube.onDraw();
    }

    public void onPause() {
        cardboardAudioEngine.pause();
    }

    public void onResume() {
        cardboardAudioEngine.resume();
    }

    public void processInput(){

    }
}
