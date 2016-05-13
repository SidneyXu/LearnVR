package com.bookislife.firstvr.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class Vertices {

    private int vertexSize = 4;
    private FloatBuffer vertices;

    public Vertices(float[] data) {
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(data.length * vertexSize);
        bbVertices.order(ByteOrder.nativeOrder());
        vertices = bbVertices.asFloatBuffer();
        vertices.put(data);
        vertices.position(0);
    }
}
