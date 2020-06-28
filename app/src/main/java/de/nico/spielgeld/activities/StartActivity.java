package de.nico.spielgeld.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import de.nico.spielgeld.R;

public class StartActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_start);

        findViewById(R.id.create_game_button).setOnClickListener((view) -> startActivity(
                new Intent(this, CreateGameActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.create_game_transition)
                ).toBundle()
        ));
        findViewById(R.id.join_game_button).setOnClickListener((view) -> startActivity(
                new Intent(this, JoinGameActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.join_game_transition)
                ).toBundle()
        ));
    }
}
