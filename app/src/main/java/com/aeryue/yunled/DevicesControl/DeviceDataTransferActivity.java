package com.aeryue.yunled.DevicesControl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aeryue.yunled.R;
import com.aeryue.yunled.Utils.CodeConverUtil;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceDataTransferActivity extends BaseDevicesControlActivity {

    private EditText mET_DataInput;
    private Button mB_DataTransfer;
    private TextView mTV_DataShow;
    private CheckBox mCB_Scale;
    private boolean isSixteenScale = false;
    private String receiveByteData = "";
    private String receiveStringData = "";
    private final static String KEY_YUNDATA = "YunData";
    private String showYunData = "00000000";

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1008){
                updateUI();
            }
            //改变进制模式
            if(msg.what == 1022){
               //16进制显示
                Toast.makeText(DeviceDataTransferActivity.this,"16进制显示",Toast.LENGTH_SHORT).show();
                showYunData = receiveByteData;
                updateUI();
            }else if(msg.what == 1033){
                //字符显示
                showYunData = receiveStringData;
                Toast.makeText(DeviceDataTransferActivity.this,"字符显示",Toast.LENGTH_SHORT).show();
                updateUI();
            }

        }
    };

    private void updateUI() {
        mTV_DataShow.setText(showYunData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_datatransfer);
        initView();
    }
    private void initView() {
        mTopBar = findViewById(R.id.device_topBar);
        //优先显示别名，若别名为空，显示产品名
        String deviceTitle = mDevice.getAlias().isEmpty()
                ? mDevice.getProductName():mDevice.getAlias();
        mTopBar.setTitle(deviceTitle);
        mTopBar.addLeftImageButton(R.mipmap.left_back_button,R.id.left_back_icon)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });
        mTopBar.addRightImageButton(R.mipmap.right_refresh_button,R.id.right_refresh_icon)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDevicesInfo();
                    }
                });
        bundView();
    }
    private void bundView() {
        mET_DataInput = findViewById(R.id.et_data_input);
        mB_DataTransfer = findViewById(R.id.b_data_transfer);
        mTV_DataShow = findViewById(R.id.tv_data_transfer);
        mCB_Scale = findViewById(R.id.cb_scale);

        mB_DataTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mET_DataInput.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"输入不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    String edittestText = mET_DataInput.getText().toString();
                    Log.i("AerYue",CodeConverUtil.toByteArray(edittestText).toString());
                    if(isSixteenScale){
                        //显示十六进制，发送十六进制
                        sendCommand(KEY_YUNDATA, CodeConverUtil.toByteArray(edittestText));
                    }else {
                        //输入字符，发送十六进制
                        sendCommand(KEY_YUNDATA, edittestText.getBytes());
                    }
                    updateDevicesInfo();
                }
            }
        });

        mCB_Scale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isSixteenScale = true;
                    mHandler.sendEmptyMessage(1022);
                } else{
                    isSixteenScale = false;
                    mHandler.sendEmptyMessage(1023);
                }
                updateDevicesInfo();
            }
        });
    }

    @Override
    protected void receiveCloudData(GizWifiErrorCode result, ConcurrentHashMap<String, Object> dataMap) {
        super.receiveCloudData(result, dataMap);
        if(result.equals(GizWifiErrorCode.GIZ_SDK_SUCCESS)){
            if(!dataMap.isEmpty()){
                parseReceiveData(dataMap);
            }
        }else{
            Log.i("AerYue","发生错误,结果:"+result);
        }
    }

    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {
        if(dataMap.get("data") != null){
            ConcurrentHashMap<String, Object> tempDataMap = (ConcurrentHashMap<String, Object>) dataMap.get("data");
            Log.i("AerYue","ReceiveData数据为:"+tempDataMap);
            for(String dataKey:tempDataMap.keySet()){
                switch (dataKey){
                    case KEY_YUNDATA:
                        byte[] tempReceiveBytes = (byte[])tempDataMap.get(dataKey);
                        Log.i("AerYue","tempReceiveBytes:"+tempReceiveBytes.length);
                        if(!isSixteenScale) {
                            //接收十六进制，显示字符
                            receiveStringData = new String(tempReceiveBytes);
                            showYunData = ByteProcessString(receiveStringData);
                        }else {
                            //接收十六进制，显示十六进制
                            receiveByteData = CodeConverUtil.toHexString(tempReceiveBytes);
                            showYunData = ByteProcessString(receiveByteData);
                        }
                        //showYunData = str;
                        Log.i("AerYue","KEY_YUNDATA:长度:"+ showYunData.length()+"数据:"+showYunData);
                        break;
                }
            }
            mHandler.sendEmptyMessage(1008);
        }
    }

    private String ByteProcessString(String str) {
        byte[] garbage = {0x00,0x00,0x00};
        String garbageStr = new String(garbage);
        return str.replace(garbageStr,"");
    }

}
