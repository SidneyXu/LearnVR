package com.bookislife.firstvr;

import android.os.Bundle;
import android.os.Vibrator;

import com.bookislife.firstvr.world.World;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import java.nio.FloatBuffer;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class VRHuntActivity
        extends CardboardActivity {
    private static final String TAG = VRHuntActivity.class.getSimpleName();

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;

    private static final String SOUND_FILE = "cube_sound.wav";

    private final float[] lightPosInEyeSpace = new float[4];

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeFoundColors;
    private FloatBuffer cubeNormals;

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private float[] modelPosition;
    private float[] headRotation;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private float floorDepth = 20f;

    private Vibrator vibrator;

    private CardboardAudioEngine cardboardAudioEngine;
    private volatile int soundId = CardboardAudioEngine.INVALID_ID;

    private World world;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);

        // TODO: 16/5/13
        world = new World(this);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(world.render);
        cardboardView.setTransitionViewEnabled(true);
        cardboardView.setOnCardboardBackButtonListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        setCardboardView(cardboardView);


        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        // Model first appears directly in front of user.
        modelPosition = new float[]{0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[4];
        headView = new float[16];

    }

    @Override
    protected void onPause() {
        super.onPause();
        world.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        world.onResume();
    }

    // 处理输入
    @Override
    public void onCardboardTrigger() {
        world.processInput();
    }

//    @Override
//    public void onSurfaceCreated(EGLConfig eglConfig) {
//        Log.i(TAG, "onSurfaceCreated()");
//
//        // 背景色
//        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);
//
//        // 初始化
//        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
//        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
//        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);
//
//        Cube cube = new Cube(vertexShader, passthroughShader);
//        Floor floor = new Floor(vertexShader, gridShader);
//
//        // Avoid any delays during start-up due to decoding of sound files.
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // Start spatial audio playback of SOUND_FILE at the model postion. The returned
//                        //soundId handle is stored and allows for repositioning the sound object whenever
//                        // the cube position changes.
//                        cardboardAudioEngine.preloadSoundFile(SOUND_FILE);
//                        soundId = cardboardAudioEngine.createSoundObject(SOUND_FILE);
//                        cardboardAudioEngine.setSoundObjectPosition(
//                                soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
//                        cardboardAudioEngine.playSound(soundId, true /* looped playback */);
//                    }
//                })
//                .start();
//
//        updateModelPosition();
//
//    }
//
//    private void updateModelPosition() {
//        Matrix.setIdentityM(modelCube, 0);
//        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);
//
//        // Update the sound location to match it with the new cube position.
//        if (soundId != CardboardAudioEngine.INVALID_ID) {
//            cardboardAudioEngine.setSoundObjectPosition(
//                    soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
//        }
//    }

}