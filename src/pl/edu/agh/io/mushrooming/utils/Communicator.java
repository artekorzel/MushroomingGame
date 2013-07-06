package pl.edu.agh.io.mushrooming.utils;

import android.util.Log;
import pl.edu.agh.io.mushrooming.wifi.exception.GameEndedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 21.05.13
 * Time: 21:14
 */
public class Communicator {
    private static final String TAG = "MUSHROOM:COMMUNICATOR";

    public static void sendMessage(DataOutputStream outputStream, MessageType messageType, String messageContent) throws IOException {
        Log.d(TAG, "Sending message: " + messageType + ", " + messageContent);
        outputStream.writeInt(messageType.value());
        outputStream.writeUTF(messageContent);
        outputStream.flush();
        Log.d(TAG, "Message sent");
    }

    public static MessageType readMessageType(DataInputStream stream) throws IOException, GameEndedException {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                return MessageType.valueOf(stream.readInt());
            } catch (SocketTimeoutException ex) {
                //intentionally empty
            }
        }
        throw new GameEndedException();
    }

    public static String readString(DataInputStream stream) throws IOException, GameEndedException {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                return stream.readUTF();
            } catch (SocketTimeoutException ex) {
                //intentionally empty
            }
        }
        throw new GameEndedException();
    }
}
