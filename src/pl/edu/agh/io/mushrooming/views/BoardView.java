package pl.edu.agh.io.mushrooming.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import pl.edu.agh.io.mushrooming.loaders.BoardLoader;
import pl.edu.agh.io.mushrooming.loaders.Element;

import java.util.List;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 11.05.13
 * Time: 18:14
 */
public class BoardView extends View {

    private static final String TAG = "MUSHROOMING:BOARD_VIEW";
    private static final int TIMER_DELAY = 500;
    private static final int REFRESH_RATE = 40;
    private static final int SINGLE_STEP = 30;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    private float positionX = 0.f;
    private float positionY = 0.f;
    private float lastTouchX;
    private float lastTouchY;
    private static float MIN_ZOOM = 0.0f;
    private static float MAX_ZOOM = 0.5f;
    private float scaleFactor = MAX_ZOOM;
    private ScaleGestureDetector detector;
    private List<Element> fields;
    private List<Element> players;
    private List<Element> addons;
    private int numberOfPlayers = 0;
    private List<Element> foreground;

    public BoardView(Context context) {
        this(context, null, 0);
    }

    public BoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public void setBoardLoader(BoardLoader boardLoader) {
        fields = boardLoader.getFields();
        players = boardLoader.getPlayers();
        addons = boardLoader.getAddons();
        foreground = boardLoader.getForeground();
        Timer timer = new Timer();
        timer.schedule(new ViewRefresher(this), TIMER_DELAY, REFRESH_RATE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        correctScaling(canvas);
        if(isConfigured()) {
            drawBasicElements(canvas, fields);
            drawBasicElements(canvas, addons);
            drawPlayers(canvas);
            drawBasicElements(canvas, foreground);
        }
        canvas.restore();
    }

    private void correctScaling(Canvas canvas) {
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(positionX / scaleFactor, positionY / scaleFactor);
    }

    private boolean isConfigured() {
        return fields != null && addons != null && players != null && foreground != null;
    }

    private void drawBasicElements(Canvas canvas, List<Element> elements) {
        for(Element element : elements) {
            canvas.drawBitmap(element.getBitmap(), element.getX(), element.getY(), null);
        }
    }

    private void drawPlayers(Canvas canvas) {
        for(int playerId = 0; playerId < numberOfPlayers; playerId++) {
            drawPlayer(canvas, playerId, players.get(playerId));
        }
    }

    private void drawPlayer(Canvas canvas, int playerId, Element player) {
        int currentFieldId = player.getCurrentFieldId();

        player.setDestinationX(fields.get(currentFieldId).getX());
        player.setDestinationY(fields.get(currentFieldId).getY());

        int currentX = player.getX();
        int currentY = player.getY();

        if(currentX < player.getDestinationX()) {
            currentX += SINGLE_STEP;
        } else if(currentX > player.getDestinationX()) {
            currentX -= SINGLE_STEP;
        }

        if(currentY < player.getDestinationY()) {
            currentY += SINGLE_STEP;
        } else if(currentY > player.getDestinationY()) {
            currentY -= SINGLE_STEP;
        }

        player.setX(currentX);
        player.setY(currentY);

        if(currentX == fields.get(currentFieldId).getX() && currentY == fields.get(currentFieldId).getY()) {
            //reached new field
            if(currentFieldId < player.getWantedId()) {
                //but not there yet
                player.setCurrentFieldId(player.getCurrentFieldId() + 1);
            }
        }

        canvas.drawBitmap(player.getBitmap(), currentX, currentY, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);

        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                handleDown(ev);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                handleMove(ev);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                handleUp(ev);
                break;
            }
        }
        return true;
    }

    private void handleDown(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        lastTouchX = x;
        lastTouchY = y;
        activePointerId = ev.getPointerId(0);
    }

    private void handleMove(MotionEvent ev) {
        int pointerIndex = ev.findPointerIndex(activePointerId);
        float x = ev.getX(pointerIndex);
        float y = ev.getY(pointerIndex);

        if (!detector.isInProgress()) {
            final float dx = x - lastTouchX;
            final float dy = y - lastTouchY;

            positionX += dx;
            positionY += dy;
        }

        lastTouchX = x;
        lastTouchY = y;
    }

    private void handleUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            lastTouchX = ev.getX(newPointerIndex);
            lastTouchY = ev.getY(newPointerIndex);
            activePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            return true;
        }
    }

    public void setPawnsCount(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public List<Element> getPlayers() {
        return players;
    }

    public List<Element> getFields() {
        return fields;
    }

}
