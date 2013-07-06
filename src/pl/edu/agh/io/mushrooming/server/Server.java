/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 14.04.13
 * Time: 21:17
 */

package pl.edu.agh.io.mushrooming.server;

import android.net.wifi.WifiManager;
import android.util.Log;
import pl.edu.agh.io.mushrooming.loaders.Element;
import pl.edu.agh.io.mushrooming.utils.Communicator;
import pl.edu.agh.io.mushrooming.utils.MessageType;
import pl.edu.agh.io.mushrooming.wifi.GameWiFiManager;
import pl.edu.agh.io.mushrooming.wifi.exception.GameEndedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    private static final String TAG = "MUSHROOM:SERVER";

    private static List<Element> fields;

    private GameWiFiManager gameWiFiManager;
    private String gameName;
    private PlayerDetails[] players;
    private int numberOfPlayers;
    private int currentPlayer = 0;

    public Server(String gameName, int numberOfPlayers, WifiManager wifiManager) {
        this.numberOfPlayers = numberOfPlayers;
        this.gameName = gameName;
        currentPlayer = new Random().nextInt(numberOfPlayers);
        players = new PlayerDetails[numberOfPlayers];
        gameWiFiManager = new GameWiFiManager(wifiManager);
    }

    @Override
    public void run() {
        try {
            gameWiFiManager.initializeAccessPoint(gameName);
            gameWiFiManager.initializeServerSocket();
            waitForPlayers();
            runGame();
        } catch(GameEndedException gee) {
            Log.d(TAG, "game ended exception caught!! (server.run())");
        } catch (Exception e) {
            Log.d(TAG, "Error while starting game");
            try {
                notifyAllClients(MessageType.GAME_OVER, "");
            } catch(Exception ex) {
                //nothing can be done
            }
        } finally {
            try {
                gameWiFiManager.closeServerSocket();
            } catch(Exception ex) {
                //nothing can be done
            }
            gameWiFiManager.bringBackInitialApConfig();
            gameWiFiManager.bringBackInitialNetwork();
        }
    }

    private void waitForPlayers() throws IOException, GameEndedException {
        for(int i = 0; i < numberOfPlayers; i++) {
            Socket socket = gameWiFiManager.acceptConnection();
            socket.setSoTimeout(500);
            players[i] = new PlayerDetails();
            players[i].setInputStream(socket.getInputStream());
            players[i].setOutputStream(socket.getOutputStream());
            Log.d(TAG, "Connection from client accepted");
        }
    }

    private void runGame() throws GameEndedException, IOException {
        getPlayersNames();
        while(true) {
            String playersName = players[currentPlayer].getName();
            notifyAllClients(MessageType.TURN_CHANGE, playersName + ";" + currentPlayer);

            String moveValue = getAndProcessPlayerMove();
            notifyAllClients(MessageType.MOVE_UPDATE, playersName + ";" + currentPlayer + ";" + moveValue);

            List<PlayerDetails> players = getSortedGameSummaryIfGameOver();
            if(players != null) {
                notifyAllClients(MessageType.GAME_OVER, prepareGameSummaryMessage(players));
                return;
            }
            pickNextPlayer();
        }
    }

    private void getPlayersNames() throws IOException, GameEndedException {
        for(int i = 0; i < numberOfPlayers; i++) {
            DataInputStream playersInputStream = players[i].getInputStream();
            MessageType type = Communicator.readMessageType(playersInputStream);
            if(type != MessageType.NAME_INTRODUCTION) {
                Log.d(TAG, "Wrong message type during name introduction: " + type);
            }
            String name = Communicator.readString(playersInputStream);
            players[i].setName(name);
            Log.d(TAG, "Name got: " + name);
        }
        Log.d(TAG, "Names collected");

        notifyAllClients(MessageType.REGISTRATION_CONFIRMED, String.valueOf(numberOfPlayers));
    }

    private void notifyAllClients(MessageType type, String message) throws IOException {
        for(int i = 0; i < numberOfPlayers; i++) {
            DataOutputStream playersOutputStream = players[i].getOutputStream();
            Communicator.sendMessage(playersOutputStream, type, message);
        }
    }

    private String getAndProcessPlayerMove() throws IOException, GameEndedException {
        Log.d(TAG, "Getting player's move");
        PlayerDetails currentPlayer = players[this.currentPlayer];
        DataInputStream playersInputStream = currentPlayer.getInputStream();
        MessageType type = Communicator.readMessageType(playersInputStream);
        if(type != MessageType.DICE_ROLL_RESULT) {
            Log.d(TAG, "Wrong message type when expecting dice roll result: " + type);
            return null;
        }
        Log.d(TAG, "Message type: " + type);
        String moveUpdate = Communicator.readString(playersInputStream);
        Log.d(TAG, "Player " + currentPlayer.getName() + " sent dice roll result: " + moveUpdate);

        int moveValue = Integer.parseInt(moveUpdate);
        int newPosition = currentPlayer.getCurrentPosition() + moveValue;
        if(newPosition < fields.size()) {
            currentPlayer.setCurrentPosition(newPosition);
            int resultChange = fields.get(newPosition).getValue();
            currentPlayer.setResult(currentPlayer.getResult() + resultChange);
        }

        return moveUpdate;
    }

    private List<PlayerDetails> getSortedGameSummaryIfGameOver() {
        int i;
        int lastFieldIndex = fields.size() - 1;
        for(i = 0; i < numberOfPlayers; ++i) {
            if(players[i].getCurrentPosition() == lastFieldIndex) {
                break;
            }
        }
        if(i == numberOfPlayers) {
            return null;
        }
        List<PlayerDetails> playersList = new LinkedList<PlayerDetails>(Arrays.asList(players));
        Collections.sort(playersList);
        Collections.reverse(playersList);
        return playersList;
    }

    private String prepareGameSummaryMessage(List<PlayerDetails> players) {
        StringBuilder result = new StringBuilder();
        int firstOfKindIndex = 0;
        for(int playerNumber = 0; playerNumber < players.size(); ++playerNumber) {
            if(players.get(firstOfKindIndex).compareTo(players.get(playerNumber)) != 0) {
                firstOfKindIndex = playerNumber;
            }
            result.append(";")
                    .append(firstOfKindIndex + 1)
                    .append(";")
                    .append(players.get(playerNumber).getName())
                    .append(";")
                    .append(players.get(playerNumber).getResult());
        }

        return result.substring(1);
    }

    private void pickNextPlayer() {
        currentPlayer = (currentPlayer + 1) % numberOfPlayers;
    }

    public static void setFields(List<Element> fields) {
        Server.fields = fields;
    }
}