package com.example.a84353.mylauncher;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;

public class MyApplication {
    char firstChar;
    Drawable drawable;
    String labelName;
    String sortName;
    String pkgName;
    //ComponentName compName;
    int id;
    public MyApplication(int _id,String ln,String pn,Drawable dr){
        id=_id;
        labelName=ln;
        sortName=ZhToPin(ln);
        firstChar=sortName.charAt(0);
        pkgName=pn;
        Log.i("debug","app packname "+pn);
        drawable=dr;
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
}
