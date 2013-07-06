package pl.edu.agh.io.mushrooming.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import pl.edu.agh.io.mushrooming.JoinGame;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 21.05.13
 * Time: 20:17
 */
public class MushroomingWifiReceiver extends WifiReceiver {

    private Context context;
    private ProgressBar lookingForGamesProgress;
    private Spinner foundGamesSpinner;

    public MushroomingWifiReceiver(JoinGame joinGameActivity, WifiManager wifiManager) {
        super(wifiManager);

        this.context = joinGameActivity;
        this.foundGamesSpinner = joinGameActivity.getFoundGamesSpinner();
        this.lookingForGamesProgress = joinGameActivity.getLookingForGamesProgress();
    }

    @Override
    protected void processFoundGames() {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, new ArrayList<String>(getFoundGames().keySet()));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foundGamesSpinner.setAdapter(dataAdapter);
        lookingForGamesProgress.setVisibility(Spinner.GONE);
        foundGamesSpinner.setVisibility(Spinner.VISIBLE);
    }
}