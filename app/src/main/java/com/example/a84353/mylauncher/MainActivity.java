package com.example.a84353.mylauncher;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.KeyEventDispatcher;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Timer theTimer;
    Handler theHandler;
    ImageView iv_slideBar,iv_background;
    TextView tv_clock;
    MyScrollView iv_plane;
    LinearLayout tl_iconTable;
    Calendar cal;
    static int num_per_row=5;
    static int name_length=10;
    private static List<List> partApps;
    final int iconSize=150;
    LauncherContentHandler handler;
    /*private class MyApplicaiton{
        ResolveInfo resInfo;
        char firstChar;
        String name;
        String pkgName;
        //ComponentName compName;
        int id;
        MyApplicaiton(ResolveInfo r0,int _id){
            id=_id;
            resInfo=r0;
            name=ZhToPin(r0.loadLabel(getPackageManager()).toString());
            if (name.length()>15)name=name.substring(0,15);
            firstChar=name.charAt(0);
            pkgName=r0.activityInfo.packageName;
        }
    }*/
    private class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                //String reason=intent.getStringExtra("reason");
                //if (reason.equals("homekey")){
                    Log.i("debug","Home");

                    //Intent it=getPackageManager().getLaunchIntentForPackage(getPackageName());
                    //startActivity(it);
                //}
            }
        }
    }
    private class ApkReceiver extends BroadcastReceiver {
        boolean isUpdatable=true;
        @Override
        public void onReceive(Context context, Intent intent){
                Log.i("debug",intent.getAction()+intent.getData().getSchemeSpecificPart());
            if (intent.getPackage()==getPackageName()) return ;
            /*if (isUpdatable==false)return;
            isUpdatable=false;
            tl_iconTable.removeAllViews();
            TimerTask fillTask=new TimerTask() {
                @Override
                public void run() {
                    theHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fillPlane();
                            isUpdatable=true;
                        }
                    });

                }
            };
            theTimer.schedule(fillTask,1000);*/
            handler.update();
           // iv_plane.invalidate();
        }
    }
    HomeReceiver hr;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK){
            iv_plane.smoothScrollTo(0,0);
            Log.i("debug","返回键");
            return true;
        }
        else Log.i("debug",""+keyCode);
        return false;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(hr);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hr = new HomeReceiver();
        IntentFilter homeFilter=new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(hr,homeFilter);
        ApkReceiver ar=new ApkReceiver();
        IntentFilter apkFilter=new IntentFilter();
        apkFilter.addAction("android.intent.action.PACKAGE_ADDED");
        apkFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        apkFilter.addDataScheme("package");
        registerReceiver(ar,apkFilter);

        if (getSupportActionBar() != null)getSupportActionBar().hide();
        regComp();
        //startY=new int[30];
        iv_slideBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("debug",event.getX()+","+event.getY());
                int scY=0;
                int selected=(int)Math.round(event.getY()*27.0/iv_slideBar.getHeight()-0.5);
                Log.i("debug","H"+iv_slideBar.getHeight()+" Y"+event.getY()+" R"+selected);
                if (selected>26)selected=26;
                if (selected<0)selected=0;
                //selected=getResources().getIdentifier("table_row_"+selected,"id",getPackageName());

                //View gl=findViewById(selected);
                View gl=tl_iconTable.getChildAt(selected);
                if(gl!=null){
                    //scroll to center
                    int sy=Math.round(gl.getY()-iv_plane.getHeight()/2+gl.getHeight()/2);
                    iv_plane.smoothScrollTo(0,sy);
                    Log.i("debug",
                            "glY"+gl.getY()+
                            "glH"+gl.getHeight()+
                            "planeH"+iv_plane.getHeight());
                }


                return true;
            }
        });
        handler=new LauncherContentHandler(iv_background,tl_iconTable,getWindowManager(),WallpaperManager.getInstance(MainActivity.this)
        ,getPackageManager(),getResources(),MainActivity.this);
        iv_plane.setController(new DragAtBorderController(iv_plane,handler));
        handler.fillPlane();
        //fillPlane();
        //startService(new Intent(MainActivity.this,));
        TimerTask clock=new TimerTask() {
            @Override
            public void run() {
                cal=Calendar.getInstance();
                String str=""+
                        cal.get(Calendar.YEAR)+"/"+
                        (int)(cal.get(Calendar.MONTH)+1)+"/"+
                        cal.get(Calendar.DAY_OF_MONTH)+"\n"+
                        cal.get(Calendar.HOUR_OF_DAY)+":"+
                        cal.get(Calendar.MINUTE);
                tv_clock.setText(str);

            }
        };
        theTimer.scheduleAtFixedRate(clock,10,20000);
    }
    private void regComp(){
        iv_slideBar=findViewById(R.id.slideBar);
        iv_plane=findViewById(R.id.plane);
        tl_iconTable=findViewById(R.id.iconTable);
        theTimer=new Timer();
        theHandler=new Handler();
        iv_background=findViewById(R.id.background);
        tv_clock=findViewById(R.id.easyTime);
    }

/*
    private LinearLayout generateLinear(MyApplicaiton appInfo){
        ImageView iv=new ImageView(MainActivity.this);
        RelativeLayout.LayoutParams rllp=new RelativeLayout.LayoutParams(iconSize,iconSize);

        rllp.setMargins(0,0,0,10);
        iv.setLayoutParams(rllp);
        MyClickListener cl=new MyClickListener(appInfo.pkgName);
        MyHoldListener hl=new MyHoldListener(appInfo.pkgName);
        iv.setImageDrawable(appInfo.resInfo.loadIcon(getPackageManager()));
        iv.setOnClickListener(cl);
        iv.setOnLongClickListener(hl);

        TextView tv=new TextView(MainActivity.this);
        String str=appInfo.resInfo.loadLabel(getPackageManager()).toString();
        if (str.length()>name_length)str=str.substring(0,name_length-3)+"...";
        tv.setText(str);
        tv.setGravity(Gravity.CENTER);
       // tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setTextSize(10);

        LinearLayout ll=new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams lllp=new LinearLayout.LayoutParams(iconSize+12,-2);
        lllp.setMargins(10,0,10,20);
        ll.setLayoutParams(lllp);
        ll.setOrientation(LinearLayout.VERTICAL);
        //ll.setBackground(getDrawable(R.drawable.block_background));
        ll.setGravity(Gravity.CENTER);
        //ll.setPadding(20,20,20,20);
        ll.addView(iv);
        ll.addView(tv);
        return ll;
    }
    private GridLayout generateGrid(List<MyApplicaiton> listApp){
        GridLayout gl=new GridLayout(MainActivity.this);
        GridLayout.LayoutParams gllp=new GridLayout.LayoutParams();
        if (!listApp.isEmpty())
         gllp.setMargins(0,0,0,30);
        gl.setLayoutParams(gllp);
        //gl.setPadding(0,20,0,20);
        gl.setColumnCount(num_per_row);
        gl.setRowCount(listApp.size()/num_per_row+((listApp.size()%num_per_row)!=0?1:0));

        RelativeLayout.LayoutParams rllp=new RelativeLayout.LayoutParams(iconSize,iconSize);
        rllp.setMargins(10,20,10,20);


        for (int j=0;!listApp.isEmpty()&&j<listApp.size();j++){
            gl.addView(generateLinear(listApp.get(j)));
        }


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
            startActivity(it);
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
            Intent itt=getPackageManager().getLaunchIntentForPackage(pkgName);
            startActivity(itt);
            Log.i("debug","start "+pkgName);
        }
    }
    public String ZhToPin(String Zh){
        String res="";
        for (int i=0;i<Zh.length();i++){
            if (Pinyin.isChinese(Zh.charAt(i)))
                res+=Pinyin.toPinyin(Zh.charAt(i));
            else if ('a'<=Zh.charAt(i)&&Zh.charAt(i)<='z')
                    res+=(char)(Zh.charAt(i)-'a'+'A');
            //else if (Zh.charAt(i)==' ')continue;
            else res+=Zh.charAt(i);

        }
        return res;
    }

    private void loadApps(){
        Intent mainIntent=new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo>tempApps=getPackageManager().queryIntentActivities(mainIntent,0);
        List<MyApplicaiton>allApps=new ArrayList<MyApplicaiton>()  ;
        MyApplicaiton curApp;
        for (int i=0;i<tempApps.size();i++){
            curApp=new MyApplicaiton(tempApps.get(i),i);
           // Log.i("debug","P: "+tempApps.get(i).activityInfo.packageName+"A: "+tempApps.get(i).activityInfo.name);
            allApps.add(curApp);
        }
        if (allApps.isEmpty())return;
        Comparator<MyApplicaiton> cr=new Comparator<MyApplicaiton>() {
            @Override
            public  int compare(MyApplicaiton r1,MyApplicaiton r2){
               return r1.name.compareTo(r2.name);
            }
        };
        Collections.sort(allApps,cr);
        char cur='A';
        partApps=new ArrayList<List>();
        int idx=0;
        List<MyApplicaiton> tempList=new ArrayList<MyApplicaiton>();
        while(idx<allApps.size()&&allApps.get(idx).firstChar!=cur){
            tempList.add(allApps.get(idx));idx++;
        }
        partApps.add(tempList);
        for (int i=0;i<26;i++,cur++){
            tempList=new ArrayList<MyApplicaiton>();

            while(idx<allApps.size()&&allApps.get(idx).firstChar==cur){
                tempList.add(allApps.get(idx));idx++;
            }
            partApps.add(tempList);
        }
    }
    private void fillPlane(){
        Log.i("debug","fillPlane");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WallpaperManager wm=WallpaperManager.getInstance(MainActivity.this);
        Drawable wallpaper=wm.getDrawable();

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
        BitmapDrawable bd_wallpaper=new BitmapDrawable(getResources(),bm_wallpaper);
        iv_background.setImageDrawable(bd_wallpaper);
        loadApps();
        for(int i=0;i<partApps.size();i++){
            LinearLayout ll=new LinearLayout(MainActivity.this);
            ll.setOrientation(LinearLayout.VERTICAL);

            TextView tv=new TextView(MainActivity.this);


            GridLayout gl;
            if(!partApps.get(i).isEmpty()){
                ll.setPadding(20,10,0,10);
                gl=generateGrid(partApps.get(i));
                if (i==0)tv.setText("#");
                else tv.setText(""+(char)(i+'A'-1));
                tv.setTextSize(20);
                ll.addView(tv);
                ll.addView(gl);
            }

            int idd=getResources().getIdentifier("table_row_"+i,"id",getPackageName());
            ll.setId(idd);
            tl_iconTable.addView(ll);
        }


    }
*/
}
