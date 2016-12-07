package com.apps.my.liener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.security.PublicKey;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by rahul on 3/12/16.
 */

public class BubbleHead implements View.OnTouchListener {
    private static final String TAG = BubbleHead.class.getSimpleName();
    public View view;
    public int height, widthMid;
    WindowManager.LayoutParams layoutParams;

    @Retention(SOURCE)
    @IntDef({HEAD_TYPE_DELETE, HEAD_TYPE_MAIN, HEAD_TYPE_TAB})
    public @interface HEAD_TYPE {
    }

    public static final int HEAD_TYPE_MAIN = 0;
    public static final int HEAD_TYPE_DELETE = 1;
    public static final int HEAD_TYPE_TAB = 2;

    int BId;

    int defaultType;

    public BubbleHead(Context context, int height, int widthMid, @HEAD_TYPE int head_type, int BId) {
        this.BId = BId;
        defaultType = head_type;
        this.widthMid = widthMid;
        this.height = height;
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = li.inflate(R.layout.browser_bubblehead, null);
        if (defaultType != HEAD_TYPE_DELETE) {
            setListener();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch() called with: v = [" + v + "], event = [" + event + "]");
        return false;
    }


    public void setProgressVisibility(int visibility) {
        // visibility can be View.VISIBLE or View.INVISIBLE
        view.findViewById(R.id.progressBar).setVisibility(visibility);
    }

    public void initParams(int x, int y) {
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.width = 100;
        layoutParams.height = 100;
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        layoutParams.x = x;
        layoutParams.y = y;
        if (defaultType == HEAD_TYPE_DELETE) {
            switchToDelete();
        }
    }

    public void switchToSmall() {
        layoutParams.width = Constant.BubbleSizeSmall;
        layoutParams.height = Constant.BubbleSizeSmall;
    }

    public void switchToLarge() {
        layoutParams.width = Constant.BubbleSizeLarge;
        layoutParams.height = Constant.BubbleSizeLarge;
    }

    public void switchToDelete() {
        ImageView delete = (ImageView) view.findViewById(R.id.headimage);
        delete.setImageResource(R.mipmap.delete);
        layoutParams.width = Constant.BubbleSizeDelete;
        layoutParams.height = Constant.BubbleSizeDelete;
    }


    BubbleListener fetchListener = null;

    public void setListener(BubbleListener listener) {
        this.fetchListener = listener;
    }

    public void sendTouchEvent(@BubbleListener.TOUCH_EVENT_TYPE int event_type) {
        Log.d(TAG, "sendTouchEvent() called with: event_type = [" + event_type + "]");
        if (this.fetchListener != null)
            this.fetchListener.onTouchEvent(event_type, defaultType, BId);
    }

    public void sendClickEvent(@BubbleListener.CLICK_EVENT_TYPE int event_type) {
        Log.d(TAG, "sendClickEvent() called with: event_type = [" + event_type + "]");
        if (this.fetchListener != null)
            this.fetchListener.onClickEvent(event_type, defaultType, BId);
    }


    boolean onRightSide = true;

    public void setListener() {
        view.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_ADD_DELETE);
                        if (defaultType == HEAD_TYPE_TAB)
                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_REMOVE_BROWSER);
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if ((!onRightSide) && defaultType == HEAD_TYPE_MAIN) {
                            layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        } else {
                            layoutParams.x = initialX - (int) (event.getRawX() - initialTouchX);
                        }
                        layoutParams.y = initialY - (int) (event.getRawY() - initialTouchY);
                        if (onDeleteCheck()) {
                            //removeDeleteView();
                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_MOVE_DELETE);
                            //addDeleteView();
                        } else {
                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_REMOVE_DELETE);
                        if (onDeleteCheck()) {
                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_DELETE);
                            return true;
                        } else {
                            switch (defaultType) {
                                case HEAD_TYPE_DELETE:
                                    break;
                                case HEAD_TYPE_MAIN:
                                    moveBubbleToSide();
                                    break;
                                case HEAD_TYPE_TAB:
                                    moveBubbleToOldPosition(initialX, initialY);
                                    sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
                                    sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_ADD_BROWSER);
                                    break;
                            }
                            sendTouchEvent(BubbleListener.TOUCH_EVENT_TYPE_UPDATE);
                            Log.d(TAG, "else in action_up");
                        }
                        return false;
                }
                return false;
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (defaultType) {
                    case HEAD_TYPE_DELETE:
                        break;
                    case HEAD_TYPE_MAIN:
                        sendClickEvent(BubbleListener.CLICK_EVENT_TYPE_EXPAND);
                        break;
                    case HEAD_TYPE_TAB:
                        sendClickEvent(BubbleListener.CLICK_EVENT_TYPE_MINIMIZE);
                        break;
                }
            }
        });
    }

    public boolean onDeleteCheck() {
        int y = (layoutParams.y - height / 4);
        int x = (layoutParams.x - widthMid + Constant.BubbleSizeDelete / 2);
        Log.d(TAG, "onDeleteCheck() called " + " height: " + height + " widthmid: " + widthMid + " x: " + x + " y: " + y);
        if ((x > -100 && x < 100) && (y > -100 && y < 100)) {
            Log.d("TESTING", "deleted");
            return true;
        } else return false;
    }

    public void moveBubbleToSide() {
        if (layoutParams.x > widthMid) {
            if (onRightSide) {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                onRightSide = false;
            } else {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                onRightSide = true;
            }
        }
        layoutParams.x = 0;
    }

    public void moveBubbleToOldPosition(int initialX, int initialY) {
        layoutParams.x = initialX;
        layoutParams.y = initialY;
    }
}