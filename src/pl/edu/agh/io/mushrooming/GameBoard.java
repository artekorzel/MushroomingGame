package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.xml.sax.InputSource;
import pl.edu.agh.io.mushrooming.client.Client;
import pl.edu.agh.io.mushrooming.client.ClientData;
import pl.edu.agh.io.mushrooming.loaders.BoardLoader;
import pl.edu.agh.io.mushrooming.server.Server;
import pl.edu.agh.io.mushrooming.utils.Communicator;
import pl.edu.agh.io.mushrooming.utils.Constants;
import pl.edu.agh.io.mushrooming.utils.GameStateChangeReceiver;
import pl.edu.agh.io.mushrooming.utils.MessageType;
import pl.edu.agh.io.mushrooming.views.BoardView;
import pl.edu.agh.io.mushrooming.wifi.GameWiFiManager;

import java.io.DataOutputStream;
import java.io.IOException;

public class GameBoard extends Activity {
    private static final String TAG = "MUSHROOM:GAME_BOARD";

    private static final int DICE_THROW_REQUEST = 1;
    private static final int JOIN_GAME_REQUEST = 2;

    private GameStateChangeReceiver gameStateChangeReceiver;
    private ClientData[] clients;
    private boolean isServerDevice;
    private GameWiFiManager gameWiFiManager;

    private BoardView boardView;
    private Button throwDiceRequest;
    private ImageView currentPlayerPawn;
    private TextView currentPlayerName;
    private TextView localCurrentPlayerResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        clients = new ClientData[Constants.NUMBER_OF_PLAYERS_PER_DEVICE];
        gameWiFiManager = new GameWiFiManager(this);
        isServerDevice = getIntent().getBooleanExtra(Constants.IS_SERVER_DEVICE, false);

        obtainViewElements();

        if(isServerDevice) {
            Server.setFields(boardView.getFields());
        }
    }

    private void obtainViewElements() {
        boardView = (BoardView) findViewById(R.id.board_view);
        InputSource boardLevelOne = null;
        try {
            boardLevelOne = new InputSource(getAssets().open("boards/board_01.xml"));
        } catch (IOException e) {
            Log.d(TAG, "IO error while reading board data");
        }
        BoardLoader boardLoader = new BoardLoader(boardLevelOne, getResources(), getPackageName());
        boardLoader.load();
        boardView.setBoardLoader(boardLoader);

        gameStateChangeReceiver = new GameStateChangeReceiver(this, boardView);

        throwDiceRequest = (Button) findViewById(R.id.throw_dice_req);
        localCurrentPlayerResult = (TextView) findViewById(R.id.result);

        currentPlayerPawn = (ImageView) findViewById(R.id.current_player_pawn);
        currentPlayerName = (TextView) findViewById(R.id.current_player_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(clients[0] == null) {
            try {
                if(isServerDevice) {
                    gameWiFiManager.waitForAccessPoint();
                } else {
                    gameWiFiManager.waitForWiFi();
                }

                String userName = getIntent().getStringExtra(Constants.USER_NAME);
                Client client = new Client(userName, this);
                clients[0] = new ClientData(client, userName);
                client.start();
                Log.d(TAG, "client created, name: " + userName);
            } catch (Exception e) {
                Log.d(TAG, "Problem with initialising connection and starting client");
            }

            IntentFilter gameStateChangeIntentFilter = new IntentFilter(Constants.INTENT_GAME_STATE_CHANGE);
            registerReceiver(gameStateChangeReceiver, gameStateChangeIntentFilter);
            gameWiFiManager.lockWiFi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gameboard_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (isMaxNumberOfPlayersOnDevice() || isGameStarted())
            menu.getItem(0).setEnabled(false);
        return true;
    }

    private boolean isGameStarted() {
        return currentPlayerName.getText().length() > 0;
    }

    private boolean isMaxNumberOfPlayersOnDevice() {
        int lastClient = Constants.NUMBER_OF_PLAYERS_PER_DEVICE - 1;
        return clients[lastClient] != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.join_existing_game:
                Intent intent = new Intent(Constants.INTENT_JOIN_EXISTING_GAME);
                startActivityForResult(intent, JOIN_GAME_REQUEST);
                return true;
            case R.id.menu_quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (ClientData data : clients) {
            if(data != null) {
                data.getClient().interrupt();
                Log.d(TAG, "Client interrupted: " + data.getClient().getClientName());
            }
        }

        if(isServerDevice) {
            NewGame.server.interrupt();
            Log.d(TAG, "Server interrupted");
            gameWiFiManager.turnOffAccessPoint();
        } else {
            gameWiFiManager.turnOffWiFi();
        }
        gameWiFiManager.unlockWiFi();
        gameWiFiManager.bringBackInitialApConfig();
        gameWiFiManager.bringBackInitialNetwork();
        unregisterReceiver(gameStateChangeReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == DICE_THROW_REQUEST) {
                throwDiceRequest.setVisibility(Button.GONE);
                processDiceThrow(data);
            }else if(requestCode == JOIN_GAME_REQUEST) {
                processJoinGame(data);
            }
        }
    }

    private void processDiceThrow(Intent data) {
        Log.d(TAG, "Getting diceThrowResult and userName");
        final int result = data.getIntExtra(Constants.DICE_THROW_RESULT, 0);
        Log.d(TAG, "diceThrowResult = " + result);
        final String userName = data.getStringExtra(Constants.USER_NAME);
        Log.d(TAG, "userName = " + userName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < Constants.NUMBER_OF_PLAYERS_PER_DEVICE; ++i) {
                    if(clients[i] != null && clients[i].getUserName().equals(userName)) {
                        DataOutputStream outputStream = clients[i].getClient().getOutputStream();
                        try {
                            Communicator.sendMessage(outputStream, MessageType.DICE_ROLL_RESULT, String.valueOf(result));
                        } catch (IOException e) {
                            Log.d(TAG, "error while sending roll to server socket");
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    private void processJoinGame(Intent data) {
        String userName = data.getStringExtra(Constants.USER_NAME);

        for(int i = 0; i < Constants.NUMBER_OF_PLAYERS_PER_DEVICE; ++i) {
            if(clients[i] == null) {
                Client client = new Client(userName, this);
                clients[i] = new ClientData(client, userName);
                client.start();
                Log.d(TAG, "Next client started");
                return;
            }
        }
        Toast.makeText(this, getResources().getString(R.string.unsuccessfull_game_add), Toast.LENGTH_SHORT).show();
    }

    public void throwDice(View v) {
        Intent diceIntent = new Intent(Constants.INTENT_DICE_THROWER);
        diceIntent.putExtra(Constants.USER_NAME, gameStateChangeReceiver.getCurrentUserName());
        startActivityForResult(diceIntent, DICE_THROW_REQUEST);
    }

    public ClientData[] getClients() {
        return clients;
    }

    public void setCurrentPlayerPawnImageAndName(String userName, int userIndex) {
        currentPlayerName.setText(userName);
        currentPlayerPawn.setImageBitmap(boardView.getPlayers().get(userIndex).getMiniBitmap());
    }

    public TextView getLocalCurrentPlayerResult() {
        return localCurrentPlayerResult;
    }

    public Button getThrowDiceRequest() {
        return throwDiceRequest;
    }

    public GameStateChangeReceiver getGameStateChangeReceiver() {
        return gameStateChangeReceiver;
    }
}
