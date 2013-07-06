package pl.edu.agh.io.mushrooming.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 21.05.13
 * Time: 20:37
 */
public abstract class WifiReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private Map<String, String> foundGames;

    public WifiReceiver(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        foundGames = new HashMap<String, String>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        List<ScanResult> resultList = wifiManager.getScanResults();
        String gameNamePrepend = "\"" + GameWiFiManager.GAME_TAG;
        for(ScanResult result : resultList) {
            if(result.SSID.startsWith(gameNamePrepend)) {
                String name = result.SSID.replace(gameNamePrepend, "").replace("\"", "");
                foundGames.put(name, result.SSID);
            }
        }

        if(!foundGames.isEmpty()) {
            processFoundGames();
        }
    }

    protected abstract void processFoundGames();

    public Map<String, String> getFoundGames() {
        return foundGames;
    }
}
