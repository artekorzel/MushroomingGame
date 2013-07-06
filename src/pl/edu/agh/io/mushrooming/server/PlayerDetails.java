package pl.edu.agh.io.mushrooming.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 04.05.13
 * Time: 21:56
 */
public class PlayerDetails implements Comparable<PlayerDetails> {
    private String name;
    private int currentPosition;
    private int result;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    @Override
    public int compareTo(PlayerDetails playerDetails) {
        if(result > playerDetails.getResult()) {
            return 1;
        }

        if(result < playerDetails.getResult()) {
            return -1;
        }

        if(currentPosition > playerDetails.getCurrentPosition()) {
            return 1;
        }

        if(currentPosition < playerDetails.getCurrentPosition()) {
            return -1;
        }

        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = new DataInputStream(inputStream);
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }
}
