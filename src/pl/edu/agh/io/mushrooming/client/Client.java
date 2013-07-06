package pl.edu.agh.io.mushrooming.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import pl.edu.agh.io.mushrooming.utils.Communicator;
import pl.edu.agh.io.mushrooming.utils.Constants;
import pl.edu.agh.io.mushrooming.utils.MessageType;
import pl.edu.agh.io.mushrooming.wifi.exception.GameEndedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 18.04.13
 * Time: 19:34
 */
public class Client extends Thread {
    private static final String TAG = "MUSHROOM:CLIENT";
    private Context context;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String name;
    private Socket socket;

    public Client(String name, Context context) {
        this.name = name;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            init();
            sendMessageToServer(MessageType.NAME_INTRODUCTION, name);
            while(true) {
                handleServerMessage();
            }
        } catch (Exception e) {
            Log.d(TAG, "IO problem with sockets: " + e.getMessage());
            emitLocalStateChange(MessageType.GAME_OVER, "");
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                //nothing can be done
            }
        }
    }

    private void init() throws IOException {
        Log.d(TAG, "Connecting to server...");
        socket = new Socket(Constants.HOST, Constants.PORT);
        Log.d(TAG, "Server socket: " + socket);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        Log.d(TAG, "Connection to server established");
    }

    private void handleServerMessage() throws IOException, GameEndedException {
        Log.d(TAG, "Waiting for message from server...");
        MessageType type = Communicator.readMessageType(inputStream);
        Log.d(TAG, "Got message: " + type);
        type.handleServerMessage(this);
    }

    private void sendMessageToServer(MessageType type, String message) {
        try {
            Communicator.sendMessage(outputStream, type, message);
        } catch (IOException e) {
            Log.d(TAG, "Sending message to server failed");
        }
    }

    public void emitLocalStateChange(MessageType type, String message) {
        Log.d(TAG, "Informing UI: " + type + ", " + message);
        Intent intent = new Intent(Constants.INTENT_GAME_STATE_CHANGE);
        intent.putExtra(Constants.USER_NAME, name);
        intent.putExtra(Constants.MESSAGE_TYPE, type.value());
        intent.putExtra(Constants.MESSAGE, message);
        context.sendBroadcast(intent);
        Log.d(TAG, "UI informed");
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public String getClientName() {
        return name;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }
}
