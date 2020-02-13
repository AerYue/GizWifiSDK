package com.aeryue.yunled;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aeryue.yunled.Adapter.LVDevicesAdapter;
import com.aeryue.yunled.Constants.Constants;
import com.aeryue.yunled.DevicesControl.DeviceDataTransferActivity;
import com.aeryue.yunled.DevicesControl.DevicesLedActivity;
import com.aeryue.yunled.DevicesControl.DevicesPetActivity;
import com.aeryue.yunled.Utils.SharePreUtil;
import com.aeryue.yunled.zxing.android.CaptureActivity;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private String _uid;
    private String _token;
    private ListView mListView;
    private LVDevicesAdapter mAdapter;
    private List<GizWifiDevice> mDevicesList;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    //设置下拉弹窗
    //private QMUITipDialog refleshTipDialog;
    private QMUITipDialog finnishTipDialog;


    /*扫描变量*/
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;

    //异步处理
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == 105){
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGizSDK();
        initView();
    }

    private void initView() {
        QMUITopBar topBar = findViewById(R.id.topBar);
        mListView = findViewById(R.id.listView1);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        topBar.setTitle("云LED");
        //右边添加加号图标
        topBar.addRightImageButton(R.mipmap.topbar_button,R.id.topbar_right_icon)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,NetConfigActivity.class));
                    }
        });

        topBar.addLeftImageButton(R.mipmap.scan_icon,R.id.topbar_scan_icon)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //动态权限申请
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            goScan();
                        }
                    }
                });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.white);
        //设置下拉颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_color_theme_1,R.color.app_color_theme_2
                ,R.color.app_color_theme_3,R.color.app_color_theme_4
                ,R.color.app_color_theme_5,R.color.app_color_theme_6
                ,R.color.app_color_theme_7,R.color.app_color_theme_7
                ,R.color.app_color_theme_9);
        //手动调用系统测量下拉长度
        mSwipeRefreshLayout.measure(0,0);
        mSwipeRefreshLayout.setRefreshing(true);
        //页面刷新
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新弹窗
                /*refleshTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                        .setTipWord("正在刷新")
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .create();
                refleshTipDialog.show();*/

                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(GizWifiSDK.sharedInstance().getDeviceList().size() != 0){
                            //如果获取到信息，更新
                           mDevicesList.clear();
                           mDevicesList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                           mAdapter.notifyDataSetChanged();
                        }
                        //消失弹窗和刷新
                        mSwipeRefreshLayout.setRefreshing(false);


                        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = manager.getActiveNetworkInfo();
                        if(info == null || !info.isConnected()){
                            //网络未连接
                            Log.i("AerYue","主界面:网络未连接");
                            finnishTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                    .setTipWord("无网络！")
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .create();
                            mListView.setVisibility(View.GONE);
                        }else {
                            mListView.setVisibility(View.VISIBLE);

                            /*refleshTipDialog.dismiss();*/
                            if (mDevicesList.size() == 0) {
                                finnishTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                        .setTipWord("暂无设备")
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                        .create();
                            } else {
                                finnishTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                        .setTipWord("刷新完成")
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                        .create();
                            }

                        }
                        finnishTipDialog.show();
                        mSwipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finnishTipDialog.dismiss();
                            }
                        },1500);//刷新完成，finnishTipDialog显示时间
                    }
                },1000);//刷新时间，旋转圆圈显示时间
            }
        });

        mDevicesList = new ArrayList<>();
        mAdapter = new LVDevicesAdapter(this,mDevicesList);
        mListView.setAdapter(mAdapter);

        //长按点击事件
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showLongDialogOnClick(mDevicesList.get(position));
                return true;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startControlDevice(mDevicesList.get(position));
            }
        });

        //mSwipeRefreshLayout设置1秒之后收回
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    //设备控制
    private void startControlDevice(GizWifiDevice device) {
        if(device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline){
            return;
        }
        device.setListener(mDeviceListener);
        //订阅     只有订阅后才可以将设备信息从云端获得
        device.setSubscribe(Constants.PRODUCT_SECRET,true);
    }

    //显示item长按弹窗
    private void showLongDialogOnClick(final GizWifiDevice device) {
        String[] items = new String[]{"重命名","解绑设备"};
        new QMUIDialog.MenuDialogBuilder(this).addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //重命名
                        showRenameDialog(device);
                        break;
                    case 1:
                        //解绑设备
                        showDeleteDialog(device);
                        break;
                }
                dialog.dismiss();
            }
        }).show();
    }

    //解绑设备弹窗
    private void showDeleteDialog(final GizWifiDevice device) {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("解绑远程设备")
                .setMessage("确定要解绑该设备吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                     public void onClick(QMUIDialog dialog, int index) {
                         dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        //无法解绑本地在线设备，因为在同一网关，若要解绑，需要设备与手机不同网关
                        GizWifiSDK.sharedInstance().unbindDevice(_uid,_token,device.getDid());
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //重命名弹窗
    private void showRenameDialog(final GizWifiDevice device) {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("重命名")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setPlaceholder("输入新名字")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String newName = builder.getEditText().getText().toString().trim();
                        if(newName.isEmpty()){
                            //新名字为空
                            Toast.makeText(
                                    MainActivity.this,"修改失败,输入不能为空", LENGTH_SHORT).show();
                        }else{
                            device.setListener(mDeviceListener);
                            device.setCustomInfo(null,newName);
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //初始化SDK
    private void initGizSDK() {
        GizWifiSDK.sharedInstance().setListener(mListener);
        // 设置 AppInfo
        ConcurrentHashMap<String, String> appInfo = new ConcurrentHashMap<>();
        appInfo.put("appId", Constants.APP_ID);
        appInfo.put("appSecret", Constants.APP_SECRET);
        // 设置要过滤的设备 productKey 列表。不过滤则直接传 null
        List<ConcurrentHashMap<String, String>> productInfo = new ArrayList<>();
        ConcurrentHashMap<String, String> product = new ConcurrentHashMap<>();
        product.put("productKey", Constants.PRODUCT_KEY);
        product.put("productSecret", Constants.PRODUCT_SECRET);
        productInfo.add(product);
        // 调用 SDK 的启动接口
        GizWifiSDK.sharedInstance().startWithAppInfo(this, appInfo, null, null, false);
        // 实现系统事件通知回调
    }

    //SDKListener
    private GizWifiSDKListener mListener = new GizWifiSDKListener() {
        //startWithAppInfo的回调
        @Override
        public void didNotifyEvent(GizEventType eventType, Object
                eventSource, GizWifiErrorCode eventID, String eventMessage) {
            Log.i("AerYue","当前事件:"+eventType.toString());
            //如果SDK初始化成功，就匿名登陆
            if(eventType == GizEventType.GizEventSDK) {
                //Toast.makeText(MainActivity.this,"SDK初始化成功", LENGTH_SHORT).show();
                //匿名登陆，不需要注册
                GizWifiSDK.sharedInstance().userLoginAnonymous();
            }else{
                Toast.makeText(MainActivity.this,"SDK失败", LENGTH_SHORT).show();
            }
        }
        //userLoginAnonymous的回调
        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            super.didUserLogin(result, uid, token);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 登录成功
                Toast.makeText(MainActivity.this,"登录成功", LENGTH_SHORT).show();
                Log.i("AerYue","登录成功");
                //Log.i("AerYue","uid:"+uid);
                //Log.i("AerYue","token:"+token);
                //存储uid和token
                SharePreUtil.putString(MainActivity.this,"_uid",uid);
                SharePreUtil.putString(MainActivity.this,"_token",token);
                //获取绑定的设备
                getBindDevices();
            } else {
                // 登录失败
                //Toast.makeText(MainActivity.this,"登录失败", LENGTH_SHORT).show();
                Log.i("AerYue","登录失败");
            }
        }

        //getBoundDevices的回调
        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            super.didDiscovered(result, deviceList);
            //发现设备
            Log.i("AerYue","结果:"+result.toString());
            Log.i("AerYue","设备信息:"+deviceList);
            mDevicesList.clear();
            mDevicesList.addAll(deviceList);

            for (int i = 0; i < deviceList.size(); i++) {
                //设备是否绑定
                //Log.i("AerYue","是否绑定:"+deviceList.get(i).isBind());
                if(!deviceList.get(i).isBind()){
                    startBindDevices(deviceList.get(i));
                }
            }
            mHandler.sendEmptyMessage(105);
        }
        //bindRemoteDevice的回调
        @Override
        public void didBindDevice(GizWifiErrorCode result, String did) {
            super.didBindDevice(result, did);
            Log.i("AerYue","GizWifiErrorCode:"+result);
            Log.i("AerYue","did:"+did);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 绑定成功
                Log.i("AerYue","didBindDevice"+"绑定成功");
            } else {
                // 绑定失败
                Log.i("AerYue","didBindDevice"+"绑定失败");
            }
        }

        //unbindDevice的回调
        @Override
        public void didUnbindDevice(GizWifiErrorCode result, String did) {
            super.didUnbindDevice(result, did);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 解绑成功
                Toast.makeText(MainActivity.this,"解绑成功", LENGTH_SHORT).show();
            } else {
                // 解绑失败
                Toast.makeText(MainActivity.this,"解绑失败", LENGTH_SHORT).show();
            }
        }


    };


    //获取绑定设备
    private void getBindDevices() {
        _uid = SharePreUtil.getString(MainActivity.this,"_uid",null);
        _token = SharePreUtil.getString(MainActivity.this,"_token",null);
        Log.i("AerYue","_uid:"+_uid);
        Log.i("AerYue","_token:"+_token);
        if(_uid!=null&&_token!=null) {
            Log.i("AerYue","_uid+_token");
            GizWifiSDK.sharedInstance().getBoundDevices(_uid,_token);
        }
    }

    //绑定设备到云端
    private void startBindDevices(GizWifiDevice device) {
        if(_uid!=null&&_token!=null){
            //绑定设备到云端
            GizWifiSDK.sharedInstance().bindRemoteDevice(
                    _uid,_token,device.getMacAddress(),Constants.PRODUCT_KEY,Constants.PRODUCT_SECRET,true);
        }
    }

    private GizWifiDeviceListener mDeviceListener = new GizWifiDeviceListener(){
        //setCustomInfo的回调
        @Override
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetCustomInfo(result, device);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 修改成功
                if(GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                    mDevicesList.clear();
                    mDevicesList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(
                            MainActivity.this,"修改成功", LENGTH_SHORT).show();
                }
            } else {
                // 修改失败
                Toast.makeText(
                        MainActivity.this,"修改失败", LENGTH_SHORT).show();
            }
        }
        //setSubscribe的回调
        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            super.didSetSubscribe(result, device, isSubscribed);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 订阅或解除订阅成功
                Intent intent = null;

                Log.i("AerYue","_device:"+device.getProductKey());
                switch (device.getProductKey()){
                    case "5ec8fbf290504633a70ce0b51a17768e":
                        intent = new Intent(MainActivity.this, DevicesPetActivity.class);
                        break;
                    case "7b4ed15eabd844ecb7042bb4706e7fb9":
                        intent = new Intent(MainActivity.this, DevicesLedActivity.class);
                        break;
                    case "11bcc0e0a58d4ab8aec31325dd52bfc7":
                        intent = new Intent(MainActivity.this, DeviceDataTransferActivity.class);
                        break;
                    default:
                        Toast.makeText(MainActivity.this,"暂不支持该设备", LENGTH_SHORT).show();
                        return;
                }
                intent.putExtra("_device",device);
                startActivity(intent);
            } else {
                // 失败
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //保证每次打开页面能够正常的回掉
        // 设置 SDK 监听
        GizWifiSDK.sharedInstance().setListener(mListener);
    }


    /**
     * 跳转到扫码界面扫码
     */
    private void goScan(){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //返回的文本内容
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                startBindDevicesByQRCode(content);
                Log.i("AerYue","你扫描到的内容是：" + content);
            }
        }
    }

    private void startBindDevicesByQRCode(String text){

        if(text.contains("product_key=") && text.contains("did=") && text.contains("passcode=")) {
            String product_key = getParamFomeUrl(text, "product_key");
            String did = getParamFomeUrl(text, "did");
            String passcode = getParamFomeUrl(text, "passcode");
            Log.i("AerYue","did->"+did);
            Log.i("AerYue","passcode->"+passcode);
            GizWifiSDK.sharedInstance().bindDevice(_uid,_token,did,passcode,null);
        }
    }

    //机智云APP工具函数
    private String getParamFomeUrl(String url, String param) {
        String product_key = "";
        int startindex = url.indexOf(param + "=");
        startindex += (param.length() + 1);
        String subString = url.substring(startindex);
        int endindex = subString.indexOf("&");
        if (endindex == -1) {
            product_key = subString;
        } else {
            product_key = subString.substring(0, endindex);
        }
        return product_key;
    }


}
