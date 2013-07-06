package pl.edu.agh.io.mushrooming.views;

import android.view.View;

import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 26.05.13
 * Time: 19:13
 */
public class ViewRefresher extends TimerTask {

    private final View view;

    public ViewRefresher(View view) {
        this.view = view;
    }

    @Override
    public void run() {
        view.postInvalidate();
    }
}
