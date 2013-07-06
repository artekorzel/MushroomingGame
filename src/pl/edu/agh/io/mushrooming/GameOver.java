package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import pl.edu.agh.io.mushrooming.utils.Constants;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 19.05.13
 * Time: 12:52
 */
public class GameOver extends Activity {

    private static final String TAG = "MUSHROOM:GAME_OVER";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        ListView winnersList = (ListView) findViewById(R.id.winners_list);
        ArrayAdapter<String> winners = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        winnersList.setAdapter(winners);

        String[] gameSummary = getIntent().getStringExtra(Constants.GAME_OVER_MESSAGE).split(";");
        int gameSummaryTableLength = gameSummary.length;
        if(gameSummaryTableLength % 3 != 0) {
            Log.d(TAG, "Wrong winners details: " + Arrays.toString(gameSummary));
        }
        for(int i = 0; i < gameSummaryTableLength; i += 3) {
            winners.add(String.format("%2s  %-20s%4s", gameSummary[i], gameSummary[i + 1], gameSummary[i + 2]));
        }
    }

    public void goToMainMenu(View v) {
        finish();
    }
}