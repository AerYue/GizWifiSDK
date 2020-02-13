package com.aeryue.yunled.DevicesControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aeryue.yunled.Constants.Constants;
import com.aeryue.yunled.R;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.concurrent.ConcurrentHashMap;

//设备控制父类
public abstract class BaseDevicesControlActivity extends AppCompatActivity {
    private QMUITipDialog mDialog;
    protected GizWifiDevice mDevice;
    protected QMUITopBar mTopBar;

    private boolean netWorkStatus = false;

    private NetWorkChangedReceiver mBroadReceiver;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDevices();
        initBroadReceiver();
    }

    //网络广播初始化
    private void initBroadReceiver(){

        mBroadReceiver = new NetWorkChangedReceiver();
        //此处拦截安卓系统的网络状态改变
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadReceiver,intentFilter);
    }

    protected  void initDevices(){
        //拿到上个界面传来的device对象
        mDevice = this.getIntent().getParcelableExtra("_device");
        //设置设备的云端回调结果监听
        mDevice.setListener(mListener);


        mDialog = new QMUITipDialog.Builder(this)
                .setTipWord("同步中...")
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mDialog.show();



        //主动获取最新状态
        getStatus();

    }

    protected void getStatus() {
        //如果设备可以控制,获取最新状态
        if(mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled)
        {
            mDevice.getNetStatus();
            mDialog.dismiss();
        }
    }

    /*
    * param:key 标志名
    * param:value   值
    * */
    protected void sendCommand(String key,Object value){
        if(value == null)
            return;
        ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();
        dataMap.put(key,value);
        mDevice.write(dataMap,0);
    }


    protected void receiveCloudData(GizWifiErrorCode result,ConcurrentHashMap<String, Object> dataMap){
        if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS)
        {
            mDialog.dismiss();
        }
    }

    private void updateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus){
        //收到设备信息，弹窗消失
        if(netStatus == GizWifiDeviceNetStatus.GizDeviceOffline){
            if(mDialog.isShowing()) {
                mDialog.dismiss();
            }
            Toast.makeText(this, "设备离线", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    protected void updateDevicesInfo(){
        mDevice.getDeviceStatus(null);
    }


    private GizWifiDeviceListener mListener = new GizWifiDeviceListener(){

        //设备信息回调
        @Override
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            super.didReceiveData(result, device, dataMap, sn);
            receiveCloudData(result,dataMap);
        }

        //设备的状态回调
        //该回调主动上报设备的网络状态变化，当设备重上电、断电或可控时会触发该回调
        @Override
        public void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
            super.didUpdateNetStatus(device, netStatus);
            updateNetStatus(device, netStatus);
        }
    };


    //内部类 获取手机网络状态的改变
    private class NetWorkChangedReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();

            if(info == null || !info.isConnected()){
                //网络未连接
                Log.i("AerYue","网络未连接");
                netWorkStatus = true;
                //Toast.makeText(getApplicationContext(),"网络已断开！",Toast.LENGTH_SHORT).show();
            }
            if(info == null){
                return;
            }
            switch (info.getType()){
                case ConnectivityManager.TYPE_MOBILE:
                    //切换到移动网络
                    Log.i("AerYue","切换到手机网络");
                    if(netWorkStatus) {
                        Toast.makeText(getApplicationContext(), "已切换到手机网络", Toast.LENGTH_SHORT).show();
                        netWorkStatus = false;
                    }
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    //切换到WIFI网络
                    Log.i("AerYue","切换到WIFI网络");
                    netWorkStatus = false;
                    //Toast.makeText(getApplicationContext(),"已切换到WIFI网络",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }



    //取消订阅云端消息
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevice.setListener(null);
        mDevice.setSubscribe(Constants.PRODUCT_SECRET,false);

        //销毁广播
        unregisterReceiver(mBroadReceiver);
    }
}
