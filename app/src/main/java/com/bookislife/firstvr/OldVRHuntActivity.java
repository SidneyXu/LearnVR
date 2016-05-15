//package com.bookislife.firstvr;
//
//import android.content.Context;
//import android.opengl.GLES20;
//import android.opengl.Matrix;
//import android.os.Bundle;
//import android.os.Vibrator;
//import android.util.Log;
//
//import com.bookislife.firstvr.util.AndroidIO;
//import com.google.vrtoolkit.cardboard.CardboardActivity;
//import com.google.vrtoolkit.cardboard.CardboardView;
//import com.google.vrtoolkit.cardboard.Eye;
//import com.google.vrtoolkit.cardboard.HeadTransform;
//import com.google.vrtoolkit.cardboard.Viewport;
//import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.FloatBuffer;
//
//import javax.microedition.khronos.egl.EGLConfig;
//
///**
// * Created by SidneyXu on 2016/05/12.
// */
//public class OldVRHuntActivity
//        extends CardboardActivity implements CardboardView.StereoRenderer {
//    private static final String TAG = OldVRHuntActivity.class.getSimpleName();
//
//    private static final float Z_NEAR = 0.1f;
//    private static final float Z_FAR = 100.0f;
//
//    private static final float CAMERA_Z = 0.01f;
//    private static final float TIME_DELTA = 0.3f;
//
//    private static final float YAW_LIMIT = 0.12f;
//    private static final float PITCH_LIMIT = 0.12f;
//
//    private static final int COORDS_PER_VERTEX = 3;
//
//    // We keep the light always position just above the user.
//    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};
//
//    private static final float MIN_MODEL_DISTANCE = 3.0f;
//    private static final float MAX_MODEL_DISTANCE = 7.0f;
//
//    private static final String SOUND_FILE = "cube_sound.wav";
//
//    private final float[] lightPosInEyeSpace = new float[4];
//
//    private FloatBuffer floorVertices;
//    private FloatBuffer floorColors;
//    private FloatBuffer floorNormals;
//
//    private FloatBuffer cubeVertices;
//    private FloatBuffer cubeColors;
//    private FloatBuffer cubeFoundColors;
//    private FloatBuffer cubeNormals;
//
//    private int cubeProgram;
//    private int floorProgram;
//
//    private int cubePositionParam;
//    private int cubeNormalParam;
//    private int cubeColorParam;
//    private int cubeModelParam;
//    private int cubeModelViewParam;
//    private int cubeModelViewProjectionParam;
//    private int cubeLightPosParam;
//
//    private int floorPositionParam;
//    private int floorNormalParam;
//    private int floorColorParam;
//    private int floorModelParam;
//    private int floorModelViewParam;
//    private int floorModelViewProjectionParam;
//    private int floorLightPosParam;
//
//    private float[] modelCube;
//    private float[] camera;
//    private float[] view;
//    private float[] headView;
//    private float[] modelViewProjection;
//    private float[] modelView;
//    private float[] modelFloor;
//
//    private float[] modelPosition;
//    private float[] headRotation;
//
//    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
//    private float floorDepth = 20f;
//
//    private Vibrator vibrator;
//
//    private CardboardAudioEngine cardboardAudioEngine;
//    private volatile int soundId = CardboardAudioEngine.INVALID_ID;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_hunt);
//
//        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
//        cardboardView.setRenderer(this);
//        cardboardView.setTransitionViewEnabled(true);
//        cardboardView.setOnCardboardBackButtonListener(new Runnable() {
//            @Override
//            public void run() {
//                onBackPressed();
//            }
//        });
//        setCardboardView(cardboardView);
//
//        modelCube = new float[16];
//        camera = new float[16];
//        view = new float[16];
//        modelViewProjection = new float[16];
//        modelView = new float[16];
//        modelFloor = new float[16];
//        // Model first appears directly in front of user.
//        modelPosition = new float[]{0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
//        headRotation = new float[4];
//        headView = new float[16];
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//
//        // 3D audio
//        cardboardAudioEngine = new CardboardAudioEngine(this, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        cardboardAudioEngine.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        cardboardAudioEngine.resume();
//    }
//
//    // 处理输入
//    @Override
//    public void onCardboardTrigger() {
//        Log.i(TAG, "onCardboardTrigger");
//
//        if (isLookingAtObject()) {
//            hideObject();
//        }
//
//        // Always give user feedback.
//        vibrator.vibrate(50);
//    }
//
//    private void hideObject() {
//        float[] rotationMatrix = new float[16];
//        float[] posVec = new float[4];
//
//        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
//        // the object's distance from the user.
//        float angleXZ = (float) Math.random() * 180 + 90;
//        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
//        float oldObjectDistance = objectDistance;
//        objectDistance =
//                (float) Math.random() * (MAX_MODEL_DISTANCE - MIN_MODEL_DISTANCE) + MIN_MODEL_DISTANCE;
//        float objectScalingFactor = objectDistance / oldObjectDistance;
//        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
//        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);
//
//        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
//        angleY = (float) Math.toRadians(angleY);
//        float newY = (float) Math.tan(angleY) * objectDistance;
//
//        modelPosition[0] = posVec[0];
//        modelPosition[1] = newY;
//        modelPosition[2] = posVec[2];
//
//        updateModelPosition();
//    }
//
//
//    // 每次进行渲染时调用,用于更新模型
//    @Override
//    public void onNewFrame(HeadTransform headTransform) {
//        // Build the Model part of the ModelView matrix.
//        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
//
//        // Build the camera matrix and apply it to the ModelView.
//        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
//
//        headTransform.getHeadView(headView, 0);
//
//        // Update the 3d audio engine with the most recent head rotation.
//        headTransform.getQuaternion(headRotation, 0);
//        cardboardAudioEngine.setHeadRotation(
//                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
//        // Regular update call to cardboard audio engine.
//        cardboardAudioEngine.update();
//
//        checkGLError("onReadyToDraw");
//    }
//
//    // 针对眼睛参数不同的每只眼睛进行调用
//    @Override
//    public void onDrawEye(Eye eye) {
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//
//        checkGLError("colorParam");
//
//        // Apply the eye transformation to the camera.
//        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
//
//        // Set the position of the light
//        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
//
//        // Build the ModelView and ModelViewProjection matrices
//        // for calculating cube position and light.
//        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
//        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
//        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
//        drawCube();
//
//        // Set modelView for the floor, so we draw floor in the correct location
//        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
//        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
//        drawFloor();
//    }
//
//    public void drawCube() {
//        GLES20.glUseProgram(cubeProgram);
//
//        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);
//
//        // Set the Model in the shader, used to calculate lighting
//        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);
//
//        // Set the ModelView in the shader, used to calculate lighting
//        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);
//
//        // Set the position of the cube
//        GLES20.glVertexAttribPointer(
//                cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);
//
//        // Set the ModelViewProjection matrix in the shader.
//        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);
//
//        // Set the normal positions of the cube, again for shading
//        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
//        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
//                isLookingAtObject() ? cubeFoundColors : cubeColors);
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
//        checkGLError("Drawing cube");
//    }
//
//    public void drawFloor() {
//        GLES20.glUseProgram(floorProgram);
//
//        // Set ModelView, MVP, position, normals, and color.
//        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
//        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
//        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
//        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);
//        GLES20.glVertexAttribPointer(
//                floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
//        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
//        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);
//
//        checkGLError("drawing floor");
//    }
//
//    @Override
//    public void onFinishFrame(Viewport viewport) {
//
//    }
//
//    @Override
//    public void onSurfaceChanged(int i, int i1) {
////        Log.i(TAG, "onSurfaceChanged()");
//    }
//
//    @Override
//    public void onSurfaceCreated(EGLConfig eglConfig) {
//        Log.i(TAG, "onSurfaceCreated()");
//
//        // 背景色
//        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f);
//
//        // 初始化 Cube
//        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
//        bbVertices.order(ByteOrder.nativeOrder());
//        cubeVertices = bbVertices.asFloatBuffer();
//        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
//        cubeVertices.position(0);
//
//        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
//        bbColors.order(ByteOrder.nativeOrder());
//        cubeColors = bbColors.asFloatBuffer();
//        cubeColors.put(WorldLayoutData.CUBE_COLORS);
//        cubeColors.position(0);
//
//        ByteBuffer bbFoundColors =
//                ByteBuffer.allocateDirect(WorldLayoutData.CUBE_FOUND_COLORS.length * 4);
//        bbFoundColors.order(ByteOrder.nativeOrder());
//        cubeFoundColors = bbFoundColors.asFloatBuffer();
//        cubeFoundColors.put(WorldLayoutData.CUBE_FOUND_COLORS);
//        cubeFoundColors.position(0);
//
//        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4);
//        bbNormals.order(ByteOrder.nativeOrder());
//        cubeNormals = bbNormals.asFloatBuffer();
//        cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
//        cubeNormals.position(0);
//
//        // 初始化 Floor
//        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
//        bbFloorVertices.order(ByteOrder.nativeOrder());
//        floorVertices = bbFloorVertices.asFloatBuffer();
//        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
//        floorVertices.position(0);
//
//        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
//        bbFloorNormals.order(ByteOrder.nativeOrder());
//        floorNormals = bbFloorNormals.asFloatBuffer();
//        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
//        floorNormals.position(0);
//
//        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
//        bbFloorColors.order(ByteOrder.nativeOrder());
//        floorColors = bbFloorColors.asFloatBuffer();
//        floorColors.put(WorldLayoutData.FLOOR_COLORS);
//        floorColors.position(0);
//
//        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
//        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
//        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);
//
//        cubeProgram = GLES20.glCreateProgram();
//        GLES20.glAttachShader(cubeProgram, vertexShader);
//        GLES20.glAttachShader(cubeProgram, passthroughShader);
//        GLES20.glLinkProgram(cubeProgram);
//        GLES20.glUseProgram(cubeProgram);
//
//        checkGLError("Cube program");
//
//        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
//        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
//        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");
//
//        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
//        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
//        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
//        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");
//
//        GLES20.glEnableVertexAttribArray(cubePositionParam);
//        GLES20.glEnableVertexAttribArray(cubeNormalParam);
//        GLES20.glEnableVertexAttribArray(cubeColorParam);
//
//        checkGLError("Cube program params");
//
//        floorProgram = GLES20.glCreateProgram();
//        GLES20.glAttachShader(floorProgram, vertexShader);
//        GLES20.glAttachShader(floorProgram, gridShader);
//        GLES20.glLinkProgram(floorProgram);
//        GLES20.glUseProgram(floorProgram);
//
//        checkGLError("Floor program");
//
//        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
//        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
//        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
//        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");
//
//        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
//        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
//        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");
//
//        GLES20.glEnableVertexAttribArray(floorPositionParam);
//        GLES20.glEnableVertexAttribArray(floorNormalParam);
//        GLES20.glEnableVertexAttribArray(floorColorParam);
//
//        checkGLError("Floor program params");
//
//        Matrix.setIdentityM(modelFloor, 0);
//        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.
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
//        checkGLError("onSurfaceCreated");
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
//        checkGLError("updateCubePosition");
//    }
//
//    private int loadGLShader(int type, int resId) {
//        String code = AndroidIO.readRawTextFile(getResources(), resId);
//        int shader = GLES20.glCreateShader(type);
//        GLES20.glShaderSource(shader, code);
//        GLES20.glCompileShader(shader);
//
//        // Get the compilation status.
//        final int[] compileStatus = new int[1];
//        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
//
//        // If the compilation failed, delete the shader.
//        if (compileStatus[0] == 0) {
//            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
//            GLES20.glDeleteShader(shader);
//            shader = 0;
//        }
//
//        if (shader == 0) {
//            throw new RuntimeException("Error creating shader.");
//        }
//
//        return shader;
//    }
//
//    @Override
//    public void onRendererShutdown() {
////        Log.i(TAG, "onRendererShutdown()");
//    }
//
//    private static void checkGLError(String label) {
//        int error;
//        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
//            Log.e(TAG, label + ": glError " + error);
//            throw new RuntimeException(label + ": glError " + error);
//        }
//    }
//
//    private boolean isLookingAtObject() {
//        float[] initVec = {0, 0, 0, 1.0f};
//        float[] objPositionVec = new float[4];
//
//        // Convert object space to camera space. Use the headView from onNewFrame.
//        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
//        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);
//
//        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
//        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);
//
//        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
//    }
//}