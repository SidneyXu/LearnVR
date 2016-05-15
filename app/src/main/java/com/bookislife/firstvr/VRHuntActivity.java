package com.bookislife.firstvr;

import android.os.Bundle;

import com.bookislife.firstvr.world.World;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

/**
 * Created by SidneyXu on 2016/05/12.
 */
public class VRHuntActivity
        extends CardboardActivity {

    private World world;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);

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

        world = new World(this);
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

    @Override
    public void onCardboardTrigger() {
        world.processInput();
    }

}