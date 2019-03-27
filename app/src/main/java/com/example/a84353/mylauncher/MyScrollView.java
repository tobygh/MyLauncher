package com.example.a84353.mylauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.Date;

public class MyScrollView extends ScrollView {
    DragAtBorderController controller;
    public MyScrollView(Context context){
        super(context);
    }
    public MyScrollView(Context context, AttributeSet atts){
        super(context,atts);
        //setOverScrollMode(ListView.OVER_SCROLL_NEVER);
    }
    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        /*if (getChildCount()>0){
            View v=getChildAt(0);
            controller=new DragAtBorderController(v);
        }*/
    }
    public void setController(DragAtBorderController dc){
        controller=dc;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override public boolean onTouchEvent(MotionEvent me){

        if (controller!=null )
            superOnTouchEvent(me);
        return super.onTouchEvent(me);
    }

    public void superOnTouchEvent(MotionEvent me){

        int action=me.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                Log.i("debug","down");
                controller.beginDrag(me.getY());break;
            case MotionEvent.ACTION_UP:
                Log.i("debug","up "+controller.endDrag(me.getY()));
            case MotionEvent.ACTION_MOVE:
                Log.i("debug","move");
                controller.dragTo(me.getY());

        }
    }


    //public
}
