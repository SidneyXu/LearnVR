package com.bookislife.firstvr;

import android.opengl.GLES20;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by SidneyXu on 2016/05/02.
 */
public class VRActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private CardboardAudioEngine cardboardAudioEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        cardboardView.setTransitionViewEnabled(true);
        setCardboardView(cardboardView);
        cardboardView.setOnCardboardBackButtonListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });

        //3D 音频
        cardboardAudioEngine =
                new CardboardAudioEngine(this, CardboardAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cardboardAudioEngine.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
    }

    //处理输入
    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }

    //每次进行渲染时调用,用于更新模型
    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    //针对眼睛参数不同的每只眼睛进行调用
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {

    }

    @Override
    public void onRendererShutdown() {

    }
}
