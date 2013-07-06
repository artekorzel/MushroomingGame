package pl.edu.agh.io.mushrooming.wifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import pl.edu.agh.io.mushrooming.wifi.exception.GameEndedException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 21.05.13
 * Time: 16:48
 */
public class GameWiFiManager {
    public static final String GAME_TAG = "MUSHROOMING-";
    private static final String TAG = "MUSHROOM:GAME_WIFI_MANAGER";
    private static final long CONNECTION_RECHECK_TIME_MILLIS = 100L;
    private static final int PORT = 3000;

    private Context context;
    private WifiManager wifiManager;
    private ServerSocket serverSocket;

    private WifiReceiver wifiReceiver;
    private IntentFilter wifiIntentFilter;
    private WifiManager.WifiLock wifiLock;
    private int initialNetworkConfigId = -1;
    private WifiConfiguration initialApConfiguration = null;

    public GameWiFiManager(WifiManager wifiManager) {
        this(null, wifiManager);
    }

    public GameWiFiManager(Context context) {
        this(context, obtainWiFiManager(context));
    }

    public GameWiFiManager(Context context, WifiManager wifiManager) {
        this.context = context;
        this.wifiManager = wifiManager;
    }

    public void initializeAccessPoint(String gameName) throws Exception {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + GAME_TAG + gameName + "\"";
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        if(wifiManager.isWifiEnabled()) {
            initialNetworkConfigId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.setWifiEnabled(false);
        }

        initialApConfiguration = (WifiConfiguration) wifiManager.getClass().getDeclaredMethod("getWifiApConfiguration")
                .invoke(wifiManager);

        wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE)
                .invoke(wifiManager, wifiConfiguration, true);

        Log.d(TAG, "WiFi initialized");
    }

    public void waitForAccessPoint() throws NoSuchMethodException, InterruptedException, InvocationTargetException, IllegalAccessException {
        Method wifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
        boolean wifiApEnabled;

        do {
            Thread.sleep(CONNECTION_RECHECK_TIME_MILLIS);
            wifiApEnabled = (Boolean) wifiApEnabledMethod.invoke(wifiManager);
        } while(!wifiApEnabled);

        Log.d(TAG, "WiFi AP should be enabled");
    }

    public void turnOffAccessPoint() {
        try {
            wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE)
                    .invoke(wifiManager, null, false);

            Log.d(TAG, "WiFi AP turned off");

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void startLookingForWiFi(WifiReceiver wifiReceiver) {
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();

        this.wifiReceiver = wifiReceiver;
        wifiIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiReceiver, wifiIntentFilter);
    }

    public void waitForWiFi() throws InterruptedException {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        do {
            Thread.sleep(CONNECTION_RECHECK_TIME_MILLIS);
            networkInfo = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } while(!networkInfo.isConnected());

        Log.d(TAG, "WiFi should be connected");
    }

    public void turnOffWiFi() {
        wifiManager.setWifiEnabled(false);
    }

    public void lockWiFi() {
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "GameWifiLock");
        wifiLock.acquire();
    }

    public void unlockWiFi() {
        wifiLock.release();
    }

    public void initializeServerSocket() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.setSoTimeout(500);
        Log.d(TAG, "Server socket created");
    }

    public Socket acceptConnection() throws IOException, GameEndedException {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                return serverSocket.accept();
            } catch (SocketTimeoutException ex) {
                //intentionally empty
            }
        }
        throw new GameEndedException();
    }

    public void closeServerSocket() throws IOException {
        serverSocket.close();
        Log.d(TAG, "Server socket closed");
    }

    public void connectToTheGame(String gameName) {
        if(wifiManager.isWifiEnabled()) {
            initialNetworkConfigId = wifiManager.getConnectionInfo().getNetworkId();
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.SSID = "\"" + gameName + "\"";

        int netId = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    public void stopLookingForWiFi() {
        context.unregisterReceiver(wifiReceiver);
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public static WifiManager obtainWiFiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void bringBackInitialApConfig() {
        if(initialApConfiguration != null) {
            //double set is essential! calling with enabled=false only does not actually restore settings.
            try {
                wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE)
                        .invoke(wifiManager, initialApConfiguration, true);
                wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE)
                        .invoke(wifiManager, initialApConfiguration, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void bringBackInitialNetwork() {
        if(initialNetworkConfigId != -1) {
            wifiManager.setWifiEnabled(true);
            wifiManager.disconnect();
            wifiManager.enableNetwork(initialNetworkConfigId, true);
            wifiManager.reconnect();
        }
    }
}
