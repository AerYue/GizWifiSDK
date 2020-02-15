package com.aeryue.yunled;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.List;

public class NetConfigActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mWifiName;
    private EditText mWifiPasswd;
    private ImageButton mWifiGetGpsButton;
    private Button mConfirmButton;
    private CheckBox mCheckBox;
    private ProgressDialog mDialog;

    private int GPS_REQUEST_CODE = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 105:
                    mDialog.setMessage("配网成功！");
                    //有结果时显示两个按钮
                    mDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                    //mDialog.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                    break;
                case 106:
                    mDialog.setMessage("配网失败.");
                    //有结果时显示两个按钮
                    mDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                    //mDialog.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_config);
        getPermissions();
        initNetView();
    }

    private void getPermissions() {
        //如果手机版本为6.0及以上，需要动态申请权限
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            requestRunPermisson(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
                    ,Manifest.permission.ACCESS_WIFI_STATE});
        }
    }

    private void requestRunPermisson(String[] strings){
        for (String permisson:strings) {
            //检查当前权限是否已经授权
            if (ContextCompat.checkSelfPermission(this,permisson)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,strings,108);
                Toast.makeText(NetConfigActivity.this,"部分功能需要该权限\n使用时手动打开",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String ssid =getWifiConnectedSsid();
        Log.i("ssid",ssid);
        //获取WIFI
        if(!ssid.equals("<unknown ssid>")){
            //已连接WIFI
            mWifiName.setText(ssid);
            mWifiPasswd.setEnabled(true);
            mConfirmButton.setEnabled(true);
        }else{
            //未连接
            mWifiName.setText("未连接");
            mWifiPasswd.setEnabled(false);
            mConfirmButton.setEnabled(false);
        }
    }
    //获取wifi名称
    /*
    *   需要打开定位才可以获取
    * */
    public String getWifiConnectedSsid() {
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //Log.i("ssid",wifiInfo.toString());
        return wifiInfo.getSSID().replace("\"","");
    }

    private void initNetView() {
        mWifiName = findViewById(R.id.wifi_name);
        mWifiPasswd = findViewById(R.id.edit_wifi_passwd);
        mConfirmButton=findViewById(R.id.confirm_button);
        mWifiGetGpsButton = findViewById(R.id.wifi_name_get_gps);
        QMUITopBar topBar2 = findViewById(R.id.topBar2);
        topBar2.setTitle("添加设备");
        topBar2.addLeftImageButton(R.mipmap.left_back_button,R.id.left_back_icon)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });
        //文本框输入内容时显示眼睛，否则隐藏
        mWifiPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){//有内容时
                    mCheckBox.setVisibility(View.VISIBLE);
                }else{
                    mCheckBox.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCheckBox = findViewById(R.id.cb_button);
        mCheckBox.setVisibility(View.GONE);//默认隐藏
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //显示
                    mWifiPasswd.setInputType(0x90);
                }else{
                    //隐藏
                    mWifiPasswd.setInputType(0x81);
                }
            }
        });

        mWifiGetGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGPSSEtting();
            }
        });

        mConfirmButton.setOnClickListener(this);
    }

    private boolean checkGpsIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    private void openGPSSEtting() {
        if (checkGpsIsOpen()){
            Toast.makeText(this, "已打开定位", Toast.LENGTH_SHORT).show();
        }else {
            new AlertDialog.Builder(this).setTitle("打开定位")
                    .setMessage("获取当前连接的WIFI名称需要打开定位.\n是否打开？")
                    //  取消选项
                    .setNegativeButton("取消",new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "已取消", Toast.LENGTH_SHORT).show();
                            // 关闭dialog
                            dialogInterface.dismiss();
                        }
                    })
                    //  确认选项
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //跳转到手机原生设置页面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent,GPS_REQUEST_CODE);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==GPS_REQUEST_CODE){
            openGPSSEtting();
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.confirm_button){
            String name = mWifiName.getText().toString().intern();//WIFI名字
            String passwd = mWifiPasswd.getText().toString().intern();//WIFI密码
            if(!name.isEmpty()){
                mDialog = new ProgressDialog(NetConfigActivity.this);
                mDialog.setMessage("正在配网中...");
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//样式为旋转
                mDialog.setCancelable(false);//弹窗外不可点击
                mDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                        Toast.makeText(NetConfigActivity.this,"取消",Toast.LENGTH_SHORT).show();
                    }
                });
                mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                        //完成时摧毁当前界面
                        finish();
                    }
                });
                mDialog.show();

                //没有回掉结果时隐藏"好的"按钮
                mDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                //mDialog.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                startAirLink(name,passwd);
            }
        }
    }
    private void startAirLink(String wifiname,String wifipasswd){
        GizWifiSDK.sharedInstance().setListener(mlistener);
        List<GizWifiGAgentType> types = new ArrayList<>();
        //只添加ESP
        types.add(GizWifiGAgentType.GizGAgentESP);
        //GizWifiSDK.sharedInstance().setDeviceOnboardingDeploy();新的方式
        GizWifiSDK.sharedInstance().setDeviceOnboarding(
                wifiname,wifipasswd, GizWifiConfigureMode.GizWifiAirLink,null,60,types);//使用旧方法
    }
    private GizWifiSDKListener mlistener = new GizWifiSDKListener(){
        @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetDeviceOnboarding(result, device);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            // 配置成功
                mHandler.sendEmptyMessage(105);
            } else {
            // 配置失败
                mHandler.sendEmptyMessage(106);
            }
        }
    };



}
