package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import pl.edu.agh.io.mushrooming.server.Server;
import pl.edu.agh.io.mushrooming.utils.Constants;
import pl.edu.agh.io.mushrooming.wifi.GameWiFiManager;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 20.04.13
 * Time: 10:46
 */
public class NewGame extends Activity {
    private static final String TAG = "MUSHROOM:NEW_GAME";
    public static Server server;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame);
    }

    public void startNewGame(View v) {
        String gameName = ((EditText) findViewById(R.id.name_of_new_game)).getText().toString();
        String userName = ((EditText) findViewById(R.id.new_user)).getText().toString();
        String numberOfPlayers = ((EditText) findViewById(R.id.number_of_players)).getText().toString();

        int numberOfPlayersInt;
        try {
            numberOfPlayersInt = Integer.parseInt(numberOfPlayers);
        } catch(NumberFormatException e) {
            Toast.makeText(this,
                    String.format(getResources().getString(R.string.improper_num_of_players_give_value) + " %d - %d",
                            Constants.MINIMAL_NUMBER_OF_PAWNS,
                            Constants.MAXIMAL_NUMBER_OF_PAWNS),
                            Toast.LENGTH_SHORT).show();
            return;
        }

        if(gameName.length() == 0
                || userName.length() == 0
                || numberOfPlayersInt < Constants.MINIMAL_NUMBER_OF_PAWNS
                || numberOfPlayersInt > Constants.MAXIMAL_NUMBER_OF_PAWNS) {
            Toast.makeText(this, getResources().getString(R.string.improper_or_incomplete_details), Toast.LENGTH_SHORT).show();
        } else {
            createGame(gameName, userName, numberOfPlayersInt);
        }
    }

    private void createGame(String gameName, String userName, int numberOfPlayers) {
        WifiManager wifiManager = GameWiFiManager.obtainWiFiManager(this);
        server = new Server(gameName, numberOfPlayers, wifiManager);
        server.start();

        Intent gameBoardIntent = new Intent(Constants.INTENT_GAME_BOARD);
        gameBoardIntent.putExtra(Constants.USER_NAME, userName);
        gameBoardIntent.putExtra(Constants.IS_SERVER_DEVICE, true);
        startActivity(gameBoardIntent);
        finish();
    }
}