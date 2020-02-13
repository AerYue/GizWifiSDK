package com.aeryue.yunled;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 107){
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkAndroidPermisson();
    }

    private void checkAndroidPermisson(){
        //如果手机版本为6.0及以上，需要动态申请权限
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            requestRunPermisson(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.ACCESS_FINE_LOCATION
                    ,Manifest.permission.ACCESS_WIFI_STATE
                    ,Manifest.permission.ACCESS_NETWORK_STATE
                    ,Manifest.permission.READ_PHONE_STATE});
        }else{
            mHandler.sendEmptyMessageDelayed(107,1000);
        }
    }

    private void requestRunPermisson(String[] strings){
        int status = 0;
        for (String permisson:strings) {
            //检查当前权限是否已经授权
            if (ContextCompat.checkSelfPermission(this,permisson)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,strings,108);
            }else{
                status++;
            }
            if (status==6){
                //权限已经全部授权
                //mHandler.sendEmptyMessageDelayed(107,500);
                mHandler.sendEmptyMessage(107);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 108:
                if(grantResults.length>0){
                    //将被拒绝的权限添加进denioedPermisson
                    List<String> denioedPermisson = new ArrayList<>();
                    for (int i=0;i<grantResults.length;i++){
                        int grandPermisson = grantResults[i];
                        String permissons = permissions[i];
                        if(grandPermisson!=PackageManager.PERMISSION_GRANTED){
                            denioedPermisson.add(permissons);
                        }
                    }
                    if(denioedPermisson.isEmpty()){
                        //权限全部通过
                        mHandler.sendEmptyMessage(107);
                    }else{
                        Toast.makeText(this,"你拒绝了部分权限，请手动开启！",Toast.LENGTH_SHORT).show();
                        mHandler.sendEmptyMessageDelayed(107,2000);
                    }
                }
                break;
        }
    }
}
