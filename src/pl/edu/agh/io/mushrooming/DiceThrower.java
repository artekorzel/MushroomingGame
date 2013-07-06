package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import pl.edu.agh.io.mushrooming.utils.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 18.04.13
 * Time: 19:39
 */
public class DiceThrower extends Activity {
    private static final String TAG = "MUSHROOM:DICETHROWER";

    private ImageView cubeView;
    private AnimationDrawable animatedCube;
    private TextView result;

    private String userName;
    private int throwResult = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cube);

        result = (TextView) findViewById(R.id.cube_result);
        cubeView = (ImageView) findViewById(R.id.cube);

        cubeView.setImageResource(R.drawable.cube_animation);
        animatedCube = (AnimationDrawable) cubeView.getDrawable();

        userName = getIntent().getStringExtra(Constants.USER_NAME);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus && throwResult == -1) {
            animatedCube.start();
        }
    }

    public void stopChoosing(View v) {
        animatedCube.stop();
        Drawable currentFrame = animatedCube.getCurrent();
        int index;
        for(index = 0; index < animatedCube.getNumberOfFrames() && currentFrame != animatedCube.getFrame(index); ++index) {}
        throwResult = index % 6 + 1;
        result.setText("Wylosowano: " + throwResult);
    }

    public void continueGame(View v) {
        if(throwResult != -1) {
            getIntent().putExtra(Constants.DICE_THROW_RESULT, throwResult);
            getIntent().putExtra(Constants.USER_NAME, userName);
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }
}