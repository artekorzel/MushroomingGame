package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import pl.edu.agh.io.mushrooming.utils.Constants;
import pl.edu.agh.io.mushrooming.wifi.GameWiFiManager;
import pl.edu.agh.io.mushrooming.wifi.MushroomingWifiReceiver;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 20.04.13
 * Time: 18:48
 */
public class JoinGame extends Activity {
    private static final String TAG = "MUSHROOM:JOIN_GAME";

    private GameWiFiManager gameWiFiManager;
    private MushroomingWifiReceiver wifiReceiver;
    private EditText newUserEditText;
    private Spinner foundGamesSpinner;
    private ProgressBar lookingForGamesProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);

        newUserEditText = (EditText) findViewById(R.id.new_user);
        foundGamesSpinner = (Spinner) findViewById(R.id.found_games);
        lookingForGamesProgress = (ProgressBar) findViewById(R.id.looking_for_games);

        gameWiFiManager = new GameWiFiManager(this);
        wifiReceiver = new MushroomingWifiReceiver(this, gameWiFiManager.getWifiManager());
        gameWiFiManager.startLookingForWiFi(wifiReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameWiFiManager.stopLookingForWiFi();
    }

    public void joinButtonClicked(View v) {
        String userName = newUserEditText.getText().toString();
        Object gameNameObject = foundGamesSpinner.getSelectedItem();
        if (userName.equals("") || gameNameObject == null) {
            Toast.makeText(JoinGame.this, getResources().getString(R.string.type_your_nick_and_choose_game), Toast.LENGTH_SHORT).show();
            return;
        }

        String gameName = wifiReceiver.getFoundGames().get(gameNameObject.toString());
        gameWiFiManager.connectToTheGame(gameName);
        Intent gameBoardIntent = new Intent(Constants.INTENT_GAME_BOARD);
        gameBoardIntent.putExtra(Constants.USER_NAME, userName);
        startActivity(gameBoardIntent);
        finish();
    }

    public Spinner getFoundGamesSpinner() {
        return foundGamesSpinner;
    }

    public ProgressBar getLookingForGamesProgress() {
        return lookingForGamesProgress;
    }
}
