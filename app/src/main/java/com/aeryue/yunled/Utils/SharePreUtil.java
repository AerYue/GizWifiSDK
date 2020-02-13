package com.aeryue.yunled.Utils;

import android.content.Context;
import android.content.SharedPreferences;

    /*
    * SharedPreferences处理，保存uid和token
    * */
public class SharePreUtil {
    private static final String SP_NAME = "sp_config";

    /*  存储封装
    * mContext:上下文
    * key:键
    * value:值
    * */
    public static void putString(Context mContext,String key, String value){
        //拿到本地的SharedPreferences的一个对象，只有本地应用才能读取
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        //拿到SharedPreferences的操作对象
        SharedPreferences.Editor editor = sp.edit();
        //存储
        editor.putString(key, value);
        //需要应用一下
        editor.apply();
    }
    /*
    *获取值
    * */
    public static String getString(Context mContext,String key,String defValue){
        //拿到本地的SharedPreferences的一个对象，只有本地应用才能读取
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        //通过键返回值，如果对应值为null,则默认为defValue
        return sp.getString(key,defValue);
    }
}
