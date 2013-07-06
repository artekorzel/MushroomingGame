package pl.edu.agh.io.mushrooming.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import pl.edu.agh.io.mushrooming.GameBoard;
import pl.edu.agh.io.mushrooming.views.BoardView;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 01.05.13
 * Time: 21:41
 */
public class GameStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "MUSHROOM:GAME_STATE_CHANGE_RECEIVER";
    private GameBoard gameBoard;
    private BoardView boardView;
    private String currentUserName;

    public GameStateChangeReceiver(GameBoard gameBoard, BoardView boardView) {
        this.gameBoard = gameBoard;
        this.boardView = boardView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MessageType type = MessageType.valueOf(intent.getIntExtra(Constants.MESSAGE_TYPE, -1));
        String message = intent.getStringExtra(Constants.MESSAGE);
        String userName = intent.getStringExtra(Constants.USER_NAME);

        Log.d(TAG, "Received message: " + type + ", " + message);
        type.processMessage(this, userName, message);
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public BoardView getBoardView() {
        return boardView;
    }
}
