package com.bookislife.firstvr.world;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Vibrator;
import android.util.Log;

import com.bookislife.firstvr.R;
import com.bookislife.firstvr.model.Cube;
import com.bookislife.firstvr.model.Floor;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import javax.microedition.khronos.egl.EGLConfig;

import static com.bookislife.firstvr.util.AndroidIO.loadGLShader;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class World {
    public static final String TAG = World.class.getSimpleName();
    public static final String SOUND_FILE = "cube_sound.wav";

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

    private Vibrator vibrator;

    private volatile int soundId = CardboardAudioEngine.INVALID_ID;

    public final WorldRender render;
    public final Context context;
    private Resources resources;

    private float[] modelViewProjection;
    private float[] modelView;

    private float[] modelPosition;
    private float[] headRotation;
    private float[] view;

    private float[] headView;
    private float[] camera;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;



    public World(Context context) {
        this.context = context;
        this.resources = context.getResources();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        render = new WorldRender(this);
        camera = new float[16];
        headView = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        // Model first appears directly in front of user.
        modelPosition = new float[]{0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[4];
        view = new float[16];
    }

    public void onDraw() {
        cube.onDraw();
    }

    public void onPause() {
        render.onPause();
    }

    public void onResume() {
        render.onResume();
    }

    public void processInput() {
        Log.i(TAG, "processInput()");

        if (isLookingAtObject()) {
            hideObject();
        }

        vibrator.vibrate(50);
    }

    public void initGLObjects() {
        // 初始化
        int vertexShader = loadGLShader(resources, GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(resources, GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int passthroughShader = loadGLShader(resources, GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        cube = new Cube(this, vertexShader, passthroughShader);
        floor = new Floor(vertexShader, gridShader);
    }

    public void update() {
        cube.onUpdate();
    }

    public void updateModelView(Eye eye){
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(World.Z_NEAR, World.Z_FAR);
        Matrix.multiplyMM(modelView, 0, view, 0, cube.getModelCube(), 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        cube.onDraw();

        // Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, floor.getModelFloor(), 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
       cube.onDraw();
    }

    public boolean isLookingAtObject() {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, cube.getModelCube(), 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    private void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = objectDistance;
        objectDistance =
                (float) Math.random() * (MAX_MODEL_DISTANCE - MIN_MODEL_DISTANCE) + MIN_MODEL_DISTANCE;
        float objectScalingFactor = objectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, cube.getModelCube(), 12);

        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY) * objectDistance;

        modelPosition[0] = posVec[0];
        modelPosition[1] = newY;
        modelPosition[2] = posVec[2];

        cube.onUpdate();
    }
}