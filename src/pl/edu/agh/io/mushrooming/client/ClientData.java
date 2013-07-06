package pl.edu.agh.io.mushrooming.client;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 08.05.13
 * Time: 22:41
 */
public class ClientData {

    private Client client;
    private String userName;
    private int result;

    public ClientData(Client client, String userName) {
        this.client = client;
        this.userName = userName;
        this.result = 0;
    }

    public String getUserName() {
        return userName;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Client getClient() {
        return client;
    }
}
