package com.example.a84353.mylauncher;

import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;

class DragAtBorderController{
    ScrollView sview;
    View displayView;
    float lastTopY;
    boolean atTop;
    Handler theHandlr=new Handler();
    LauncherContentHandler handler;
    public DragAtBorderController(ScrollView s,LauncherContentHandler hd){
        sview=s;
        handler=hd;
        if (sview.getChildCount()>0)
            displayView=sview.getChildAt(0);
        atTop=false;
    }

    public void beginDrag(float mouseY){
        atTop=false;
        if (sview.getScrollY()==0){
            atTop=true;
            lastTopY=mouseY;
        }
    }
    public void dragTo(float mouseY){
        if (sview.getScrollY()!=0){
            atTop=false;
        }
        if (atTop==false){
            atTop=true;
            lastTopY=mouseY;
        }
    }
    public boolean endDrag(float mouseY){
        if (atTop==false)return false;
        if (mouseY-lastTopY>sview.getHeight()/4.0){
            theHandlr.post(new Runnable() {
                @Override
                public void run() {
                    handler.update();
                }
            });

        }
        return false;
    }
}
