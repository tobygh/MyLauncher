package com.example.a84353.mylauncher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LauncherContentHandler {
    Handler theHandler;
    ImageView iv_background;
    LinearLayout ll_iconTable;
    WindowManager wm;
    WallpaperManager wpm;
    PackageManager pm;
    Resources resources;
    Context context;
    boolean updatable;
    boolean updateMetUpdate;
    final int name_length=10;
    final int iconSize=150;
    final int num_per_row=5;
    Timer theTimer;

    LauncherContentHandler(ImageView iv, LinearLayout ll, WindowManager _wm, WallpaperManager _wpm, PackageManager _pm,Resources _r,Context ct){
        iv_background=iv;
        ll_iconTable=ll;
        wm=_wm;
        wpm=_wpm;
        pm=_pm;
        resources=_r;
        context=ct;
        updatable=true;
        theTimer = new Timer();
        theHandler=new Handler();
        updateMetUpdate=false;
        updatable=true;
    }

    void update(){
        if (updatable){
            Log.i("debug","handle update");
            updatable=false;
            ll_iconTable.removeAllViews();
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    theHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fillPlane();
                            updatable=true;
                            if (updateMetUpdate){
                                Log.i("debug","handle extra update");
                                updateMetUpdate=false;
                                update();
                            }
                        }
                    });
                }
            };
            theTimer.schedule(task,1000);
        }
        else{
            updateMetUpdate=true;
        }
    }
    private List<List> loadApps(){
        Intent mainIntent=new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo>tempApps=pm.queryIntentActivities(mainIntent,0);
        List<MyApplication>allApps=new ArrayList<MyApplication>()  ;
        MyApplication curApp;ResolveInfo res;
        for (int i=0;i<tempApps.size();i++){
            res=tempApps.get(i);
            String str=""+res.loadLabel(pm);

            curApp=new MyApplication(i,str,res.activityInfo.packageName,res.loadIcon(pm));
            allApps.add(curApp);
        }
        if (allApps.isEmpty())return new ArrayList<List>();
        Comparator<MyApplication> cr=new Comparator<MyApplication>() {
            @Override
            public  int compare(MyApplication r1,MyApplication r2){
                return r1.sortName.compareTo(r2.sortName);
            }
        };
        Collections.sort(allApps,cr);
        char cur='A';
        List<List>appBlockLists=new ArrayList<List>();
        int idx=0;
        List<MyApplication> tempList=new ArrayList<MyApplication>();
        while(idx<allApps.size()&&allApps.get(idx).firstChar!=cur){
            tempList.add(allApps.get(idx));idx++;
        }
        appBlockLists.add(tempList);
        for (int i=0;i<26;i++,cur++){
            tempList=new ArrayList<MyApplication>();

            while(idx<allApps.size()&&allApps.get(idx).firstChar==cur){
                tempList.add(allApps.get(idx));idx++;
            }
            appBlockLists.add(tempList);
        }
        return appBlockLists;
    }
    public void fillPlane(){
        Log.i("debug","fillPlane");
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        Drawable wallpaper=wpm.getDrawable();
        Bitmap bm_wallpaper=((BitmapDrawable)wallpaper).getBitmap();
        int Sw=dm.widthPixels,Sh=dm.heightPixels,Pw=bm_wallpaper.getWidth(),Ph=bm_wallpaper.getHeight();
        Log.i("debug","Sw"+Sw+" Sh"+Sh+" Pw"+Pw+"Ph"+Ph);
        double scaleK1=1.0,scaleK2=1.0,scale;
        if (Sw>Pw)scaleK1=1.0*Sw/Pw;
        if(Sh>Ph)scaleK2=1.0*Sh/Ph;
        scale=Math.max(scaleK1,scaleK2);
        Matrix opMatrix=new Matrix();
        opMatrix.postScale((float) scale,(float) scale);

        bm_wallpaper=Bitmap.createBitmap(bm_wallpaper,0,0,Pw,Ph,opMatrix,true);
        bm_wallpaper=Bitmap.createBitmap(bm_wallpaper,0,0,Sw,Sh);
        BitmapDrawable bd_wallpaper=new BitmapDrawable(resources,bm_wallpaper);
        iv_background.setImageDrawable(bd_wallpaper);
        List<List>appBlocks=loadApps();
        for(int i=0;i<appBlocks.size();i++){
            LinearLayout ll=new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);

            TextView tv=new TextView(context);
            GridLayout gl;
            List block=appBlocks.get(i);
            if(!block.isEmpty()){
                ll.setPadding(20,10,0,10);
                gl=generateGrid(block);
                if (i==0)tv.setText("#");
                else tv.setText(""+(char)(i+'A'-1));
                tv.setTextSize(20);
                ll.addView(tv);
                ll.addView(gl);
            }

            int idd=resources.getIdentifier("table_row_"+i,"id",context.getPackageName());
            ll.setId(idd);
            ll_iconTable.addView(ll);
        }
    }
    private LinearLayout generateLinear(MyApplication appInfo){
        ImageView iv=new ImageView(context);
        RelativeLayout.LayoutParams rllp=new RelativeLayout.LayoutParams(iconSize,iconSize);
        rllp.setMargins(0,0,0,10);
        iv.setLayoutParams(rllp);
        MyClickListener cl=new MyClickListener(appInfo.pkgName);
        MyHoldListener hl=new MyHoldListener(appInfo.pkgName);
        iv.setImageDrawable(appInfo.drawable);
        iv.setOnClickListener(cl);
        iv.setOnLongClickListener(hl);
        TextView tv=new TextView(context);
        String str=appInfo.labelName;
        if (str.length()>name_length)str=str.substring(0,name_length-3)+"...";
        tv.setText(str);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(10);
        LinearLayout ll=new LinearLayout(context);
        //LinearLayout.LayoutParams lllp=new LinearLayout.LayoutParams(iconSize+12,-2);
        GridLayout.LayoutParams grlp=new GridLayout.LayoutParams();
        grlp.setMargins(0,0,0,10);
        grlp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1,1.0f);
        //ll.setWeightSum(1);
        ll.setLayoutParams(grlp);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.addView(iv);
        ll.addView(tv);

        return ll;
    }
    private LinearLayout emptyLinear(){
        TextView tv=new TextView(context);
        RelativeLayout.LayoutParams rllp=new RelativeLayout.LayoutParams(iconSize,iconSize);
        rllp.setMargins(0,0,0,10);
        tv.setLayoutParams(rllp);
        tv.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout ll=new LinearLayout(context);
        GridLayout.LayoutParams grlp=new GridLayout.LayoutParams();
        grlp.setMargins(0,0,0,10);
        grlp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1,1.0f);
        //ll.setWeightSum(1);

        ll.setLayoutParams(grlp);
        ll.addView(tv);
        return ll;
    }
    private GridLayout generateGrid(List<MyApplication> listApp){
        GridLayout gl=new GridLayout(context);
        RelativeLayout.LayoutParams rlly=new RelativeLayout.LayoutParams(-1,-2);
        if (!listApp.isEmpty())
            rlly.setMargins(0,0,0,30);
        gl.setLayoutParams(rlly);
        gl.setColumnCount(num_per_row);
        int sz=listApp.size();
        int nRow=sz/num_per_row;if (sz%num_per_row!=0)nRow++;
        gl.setRowCount(nRow);


        for (int j=0;j<sz;j++){
            gl.addView(generateLinear(listApp.get(j)));
        }

        int full=nRow*num_per_row;
        Log.i("debug","fill "+sz+","+full);
        for (int j=sz;j<full;j++)
            gl.addView(emptyLinear());
        return gl;
    }

    private class MyHoldListener implements View.OnLongClickListener{
        String pkgName;
        MyHoldListener(String name){
            pkgName=name;
        }
        @Override
        public boolean onLongClick(View view){
            Intent it=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            it.setData(Uri.parse("package:"+pkgName));
            context.startActivity(it);
            return false;
        }
    }
    private class MyClickListener implements View.OnClickListener {
        //ComponentName compName;
        String pkgName;
        MyClickListener(String name){
            pkgName=name;
        }
        @Override
        public void onClick(View view){
            Intent itt=pm.getLaunchIntentForPackage(pkgName);
            context.startActivity(itt);
            Log.i("debug","start "+pkgName);
        }
    }
}
