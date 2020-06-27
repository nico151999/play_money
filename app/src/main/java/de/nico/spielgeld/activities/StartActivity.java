package de.nico.spielgeld.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;

import de.nico.spielgeld.R;

public class StartActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_start);

        Transition fade = new Fade();
        fade.excludeTarget(R.id.appbar_layout, true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);

        findViewById(R.id.create_game_button).setOnClickListener((view) -> startActivity(
                new Intent(this, CreateGameActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.create_game_transition)
                ).toBundle()
        ));
        findViewById(R.id.join_game_button).setOnClickListener((view) -> startActivity(
                new Intent(this, JoinGameActivity.class)
        ));
    }
}
