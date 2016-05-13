package com.bookislife.firstvr.world;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class WorldRender implements CardboardView.StereoRenderer {

    private static final String TAG = WorldRender.class.getSimpleName();

    private float[] camera;
    private float[] view;
    private float[] headRotation;
    private float[] headView;

    private final float[] lightPosInEyeSpace = new float[4];

    private World world;

    public final CardboardAudioEngine cardboardAudioEngine;

    private volatile int soundId = CardboardAudioEngine.INVALID_ID;

    public WorldRender(World world) {
        this.world = world;
        // 3D audio
        cardboardAudioEngine = new CardboardAudioEngine(world.context, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    public void onPause() {
        cardboardAudioEngine.pause();
    }

    public void onResume() {
        cardboardAudioEngine.resume();
    }


    // 每次进行渲染时调用,用于更新模型
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the Model part of the ModelView matrix.
        Matrix.rotateM(modelCube, 0, World.TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, World.CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
        cardboardAudioEngine.setHeadRotation(
                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
        // Regular update call to cardboard audio engine.
        cardboardAudioEngine.update();
    }

    // 针对眼睛参数不同的每只眼睛进行调用
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, World.LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(World.Z_NEAR, World.Z_FAR);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        world.cube.onDraw();

        // Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        world.cube.onDraw();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.i(TAG, "onSurfaceChanged()");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated()");

        // 背景色
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);

        world.onSurfaceCreated(eglConfig);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                        //soundId handle is stored and allows for repositioning the sound object whenever
                        // the cube position changes.
                        float[] modelPosition = world.cube.getModelPosition();
                        cardboardAudioEngine.preloadSoundFile(World.SOUND_FILE);
                        soundId = cardboardAudioEngine.createSoundObject(World.SOUND_FILE);
                        cardboardAudioEngine.setSoundObjectPosition(
                                soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
                        cardboardAudioEngine.playSound(soundId, true /* looped playback */);
                    }
                })
                .start();

        world.update();

        if (soundId != CardboardAudioEngine.INVALID_ID) {
            float[] modelPosition = world.cube.getModelPosition();
            cardboardAudioEngine.setSoundObjectPosition(
                    soundId, modelPosition[0], modelPosition[1], modelPosition[2]);
        }
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown()");
    }
}
