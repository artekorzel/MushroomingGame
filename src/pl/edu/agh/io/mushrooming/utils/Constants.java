package pl.edu.agh.io.mushrooming.utils;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 21.05.13
 * Time: 22:22
 */
public interface Constants {
    String HOST = "192.168.43.1";
    int PORT = 3000;

    int MAXIMAL_NUMBER_OF_PAWNS = 4;
    int MINIMAL_NUMBER_OF_PAWNS = 2;
    int NUMBER_OF_PLAYERS_PER_DEVICE = 2;

    String IS_SERVER_DEVICE = "isServerDevice";
    String USER_NAME = "userName";
    String DICE_THROW_RESULT = "diceThrowResult";
    String GAME_OVER_MESSAGE = "gameOverMessage";
    String MESSAGE_TYPE = "messageType";
    String MESSAGE = "message";

    String INTENT_GAME_STATE_CHANGE = "MUSHROOMING_PROTOCOL";
    String INTENT_JOIN_EXISTING_GAME = "pl.edu.agh.io.mushrooming.JOIN_EXISTING_GAME";
    String INTENT_DICE_THROWER = "pl.edu.agh.io.mushrooming.DICE_THROWER";
    String INTENT_GAME_BOARD = "pl.edu.agh.io.mushrooming.GAME_BOARD";
    String INTENT_GAME_OVER = "pl.edu.agh.io.mushrooming.GAME_OVER";
}
