package com.bookislife.firstvr.model;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.bookislife.firstvr.world.World;
import com.bookislife.firstvr.world.WorldLayoutData;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class Cube implements GLObject {

    private int cubeProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private float[] modelCube;
    private float[] modelViewProjection;
    private float[] modelView;

    private final float[] lightPosInEyeSpace = new float[4];

    private static final int COORDS_PER_VERTEX = 3;

    private Vertices coordsVertices;
    private Vertices colorVertices;
    private Vertices foundColodrVertices;
    private Vertices normalVertices;
    private float[] modelPosition;
    private World world;

    public Cube(World world, int vertexShader, int passthroughShader) {
        modelCube = new float[16];
        modelPosition = new float[]{0.0f, 0.0f, -World.MAX_MODEL_DISTANCE / 2.0f};

        coordsVertices = new Vertices(WorldLayoutData.CUBE_COORDS);
        colorVertices = new Vertices(WorldLayoutData.CUBE_COLORS);
        foundColodrVertices = new Vertices(WorldLayoutData.CUBE_FOUND_COLORS);
        normalVertices = new Vertices(WorldLayoutData.CUBE_NORMALS);

        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);

    }

    @Override
    public void onDraw() {
        GLES20.glUseProgram(cubeProgram);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(
                cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, coordsVertices.vertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, normalVertices.vertices);
        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
                world.isLookingAtObject() ? foundColodrVertices.vertices : colorVertices.vertices);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    @Override
    public void onUpdate() {
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);

    }

    public float[] getModelPosition() {
        return modelPosition;
    }

    public float[] getModelCube() {
        return modelCube;
    }
}
