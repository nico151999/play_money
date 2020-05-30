package de.nico.spielgeld.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;

import de.nico.spielgeld.R;

public class StartActivity extends MainActivity {
    private MaterialButton mCreateGameButton;
    private MaterialButton mJoinGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mCreateGameButton = findViewById(R.id.create_game_button);
        mCreateGameButton.setOnClickListener((view) -> startActivity(new Intent(this, CreateGameActivity.class)));
        mJoinGameButton = findViewById(R.id.join_game_button);
        mJoinGameButton.setOnClickListener((view) -> startActivity(new Intent(this, JoinGameActivity.class)));
    }
}
