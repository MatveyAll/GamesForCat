
package com.example.gamesforcat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<GameButton> gameButtons = new ArrayList<>();

    static SharedPreferences preferences;
    static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        gameButtons.add(new GameButton(
                findViewById(R.id.game_image_1_IB), R.mipmap.mouse_ic_foreground,
                findViewById(R.id.game_name_1_TV), "ИГРА С МЫШЬЮ"));
        gameButtons.add(new GameButton(
                findViewById(R.id.game_image_2_IB), R.mipmap.fly_ic_foreground,
                findViewById(R.id.game_name_2_TV), "ИГРА С МУХОЙ"));
        gameButtons.add(new GameButton(
                findViewById(R.id.game_image_3_IB), R.mipmap.cat_says_ic_foreground,
                findViewById(R.id.game_name_3_TV), "КОШАЧИЙ ПЕРЕВОДЧИК"));

        preferences = getPreferences(MODE_PRIVATE);
        editor = preferences.edit();

        for (int i = 0; i < gameButtons.size(); i++) {
            final int i1 = i;
            gameButtons.get(i).button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame(i1);
                }
            });
        }
    }

    public void startGame(int chosenGame) {
        Intent[] games = { // в качестве поля не работает
                new Intent(this, MouseGameActivity.class),
                new Intent(this, FlyGameActivity.class),
                new Intent(this, CatTranslatorActivity.class)
        };
        startActivity(games[chosenGame]);
    }

    static class GameButton {

        ImageButton button;
        TextView gameNameView;

        public GameButton(ImageButton button, int imageResource, TextView gameNameView, String gameName) {
            this.button = button;
            this.gameNameView = gameNameView;

            // я делаю всю эту настройку здесь, потому что в разметке пришлось бы слишком много копировать одно и то же
            this.button.setBackgroundResource(R.drawable.rounded_shape);
            this.button.setImageResource(imageResource);

            this.gameNameView.setGravity(Gravity.CENTER);
            this.gameNameView.setTextSize(15);
            this.gameNameView.setTextColor(Color.WHITE);
            this.gameNameView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            this.gameNameView.setText(gameName);
        }
    }
}