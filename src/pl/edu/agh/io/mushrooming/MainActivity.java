package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 25.03.13
 * Time: 17:09
 */
public class MainActivity extends Activity {
    private static final String TAG = "MUSHROOM:MAIN_ACTIVITY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.start_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("pl.edu.agh.io.mushrooming.NEW_GAME"));
            }
        });

        findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("pl.edu.agh.io.mushrooming.JOIN_GAME"));
            }
        });
    }
}
