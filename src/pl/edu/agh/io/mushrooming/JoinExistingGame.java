package pl.edu.agh.io.mushrooming;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import pl.edu.agh.io.mushrooming.utils.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: artur
 * Date: 08.05.13
 * Time: 23:12
 */
public class JoinExistingGame extends Activity {
    private EditText joiningUserEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_existing_game);
        joiningUserEditText = (EditText) findViewById(R.id.joining_user);
    }

    public void joinExistingButtonClicked(View v) {
        String userName = joiningUserEditText.getText().toString();
        if(userName.equals("")) {
            Toast.makeText(JoinExistingGame.this, getResources().getString(R.string.type_your_nick), Toast.LENGTH_SHORT).show();
        } else {
            getIntent().putExtra(Constants.USER_NAME, userName);
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }
}