package pl.edu.agh.io.mushrooming.utils;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import pl.edu.agh.io.mushrooming.GameBoard;
import pl.edu.agh.io.mushrooming.R;
import pl.edu.agh.io.mushrooming.client.Client;
import pl.edu.agh.io.mushrooming.client.ClientData;
import pl.edu.agh.io.mushrooming.loaders.Element;
import pl.edu.agh.io.mushrooming.views.BoardView;
import pl.edu.agh.io.mushrooming.wifi.exception.GameEndedException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 18.04.13
 * Time: 19:28
 */
public enum MessageType {
    NAME_INTRODUCTION(1),

    TURN_CHANGE(2) {
        @Override
        public void handleServerMessage(Client client) throws IOException, GameEndedException {
            String turn = Communicator.readString(client.getInputStream());
            Log.d(TAG, "Turn change: " + turn);
            String currentPlayerName = turn.split(";")[0];
            if(currentPlayerName.equals(client.getClientName())) {
                client.emitLocalStateChange(MessageType.YOUR_TURN, turn);
            } else {
                client.emitLocalStateChange(MessageType.OTHERS_TURN, turn);
            }
        }
    },

    DICE_ROLL_RESULT(3),

    GAME_OVER(4) {
        @Override
        public void handleServerMessage(Client client) throws IOException, GameEndedException {
            String gameSummary = Communicator.readString(client.getInputStream());
            Log.d(TAG, "Game over: " + gameSummary);
            client.emitLocalStateChange(this, gameSummary);
        }

        @Override
        public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
            GameBoard gameBoard = receiver.getGameBoard();
            if(!gameBoard.getClients()[0].getUserName().equals(userName)) {
                return;
            }
            if(message.length() == 0) {
                Toast.makeText(gameBoard, gameBoard.getResources().getString(R.string.game_over_error), Toast.LENGTH_LONG).show();
            } else {
                Intent gameOverIntent = new Intent(Constants.INTENT_GAME_OVER);
                gameOverIntent.putExtra(Constants.GAME_OVER_MESSAGE, message);
                gameBoard.startActivity(gameOverIntent);
            }
            gameBoard.finish();
        }
    },

    MOVE_UPDATE(5) {

        @Override
        public void handleServerMessage(Client client) throws IOException, GameEndedException {
            String moveDetails = Communicator.readString(client.getInputStream());
            Log.d(TAG, "Move update: " + moveDetails);
            client.emitLocalStateChange(this, moveDetails);
        }

        @Override
        public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
            ClientData[] clients = receiver.getGameBoard().getClients();
            if(!clients[0].getUserName().equals(userName)) {
                return;
            }

            BoardView boardView = receiver.getBoardView();
            String[] moveDetails = message.split(";");
            if(moveDetails.length != 3) {
                Log.d(TAG, "Problem with moving pawns = wrong message");
                return;
            }

            String movingUserName = moveDetails[0];
            int pawnNumber = Integer.parseInt(moveDetails[1]);
            int moveValue = Integer.parseInt(moveDetails[2]);

            Element currentPawn = boardView.getPlayers().get(pawnNumber);
            int currentPosition = currentPawn.getCurrentFieldId();
            int lastPosition = boardView.getFields().size();
            int newPosition = currentPosition + moveValue;

            if(newPosition < lastPosition) {
                currentPawn.setWantedId(newPosition);

                int newPositionValue = boardView.getFields().get(newPosition).getValue();
                for(ClientData details : clients) {
                    if(details != null && details.getUserName().equals(movingUserName)) {
                        int result = details.getResult() + newPositionValue;
                        details.setResult(result);
                        Log.d(TAG, "RESULT: " + result);
                        break;
                    }
                }
            }
        }
    },

    REGISTRATION_CONFIRMED(6) {
        @Override
        public void handleServerMessage(Client client) throws IOException, GameEndedException {
            String message = Communicator.readString(client.getInputStream());
            Log.d(TAG, "Number of players: " + message);
            client.emitLocalStateChange(this, message);
        }

        @Override
        public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
            BoardView boardView = receiver.getBoardView();
            int numberOfPlayers = Integer.parseInt(message);
            boardView.setPawnsCount(numberOfPlayers);
            Log.d(TAG, "UI updated");
        }
    },

    YOUR_TURN(7) {
        @Override
        public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
            GameBoard gameBoard = receiver.getGameBoard();
            gameBoard.getGameStateChangeReceiver().setCurrentUserName(userName);
            ClientData[] localClients = gameBoard.getClients();
            for(ClientData details : localClients) {
                if(details.getUserName().equals(userName)) {
                    gameBoard.getLocalCurrentPlayerResult().setText(String.valueOf(details.getResult()));
                    break;
                }
            }
            String firstLocalClientName = localClients[0].getUserName();
            if(firstLocalClientName.equals(userName)) {
                int currentPlayerPawnNumber = Integer.parseInt(message.split(";")[1]);
                gameBoard.setCurrentPlayerPawnImageAndName(userName, currentPlayerPawnNumber);
            }
            gameBoard.getThrowDiceRequest().setVisibility(Button.VISIBLE);
            Log.d(TAG, "UI updated");
        }
    },

    OTHERS_TURN(8) {
        @Override
        public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
            GameBoard gameBoard = receiver.getGameBoard();
            if(userName.equals(gameBoard.getClients()[0].getUserName())) {
                String[] messageParts = message.split(";");
                String otherPlayerName = messageParts[0];
                int otherPlayerPawnNumber = Integer.parseInt(messageParts[1]);
                gameBoard.setCurrentPlayerPawnImageAndName(otherPlayerName, otherPlayerPawnNumber);
            }
            Log.d(TAG, "UI updated");
        }
    },

    INVALID(0);

    private static final String TAG = "MUSHROOM:MESSAGE_TYPE";
    private int value;

    private MessageType(int v) {
        value = v;
    }

    public int value() {
        return value;
    }

    public static MessageType valueOf(int value) {
        switch(value) {
            case 1: return NAME_INTRODUCTION;
            case 2: return TURN_CHANGE;
            case 3: return DICE_ROLL_RESULT;
            case 4: return GAME_OVER;
            case 5: return MOVE_UPDATE;
            case 6: return REGISTRATION_CONFIRMED;
            case 7: return YOUR_TURN;
            case 8: return OTHERS_TURN;
            default: return INVALID;
        }
    }

    public void processMessage(GameStateChangeReceiver receiver, String userName, String message) {
        Log.d(TAG, "received type: " + this + ", msg: " + message);
    }

    public void handleServerMessage(Client client) throws IOException, GameEndedException {
        Log.d(TAG, "Unknown message: " + this);
    }
}
