package com.aeryue.yunled.DevicesControl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import com.aeryue.yunled.R;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;

import java.util.concurrent.ConcurrentHashMap;

public class DevicesLedActivity  extends BaseDevicesControlActivity implements View.OnClickListener {

    private ToggleButton mTB_Led_1_1,mTB_Led_1_2,mTB_Led_1_3,mTB_Led_1_4,mTB_Led_1_5;
    private ToggleButton mTB_Led_2_1,mTB_Led_2_2,mTB_Led_2_3,mTB_Led_2_4,mTB_Led_2_5;

    private final static String KEY_LED_1_1 = "LED_1_1";
    private final static String KEY_LED_1_2 = "LED_1_2";
    private final static String KEY_LED_1_3 = "LED_1_3";
    private final static String KEY_LED_1_4 = "LED_1_4";
    private final static String KEY_LED_1_5 = "LED_1_5";
    private final static String KEY_LED_2_1 = "LED_2_1";
    private final static String KEY_LED_2_2 = "LED_2_2";
    private final static String KEY_LED_2_3 = "LED_2_3";
    private final static String KEY_LED_2_4 = "LED_2_4";
    private final static String KEY_LED_2_5 = "LED_2_5";

    private boolean showLed_1_1 = false;
    private boolean showLed_1_2 = false;
    private boolean showLed_1_3 = false;
    private boolean showLed_1_4 = false;
    private boolean showLed_1_5 = false;
    private boolean showLed_2_1 = false;
    private boolean showLed_2_2 = false;
    private boolean showLed_2_3 = false;
    private boolean showLed_2_4 = false;
    private boolean showLed_2_5 = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1055:
                    updateUI();
                    break;
            }
        }
    };

    private void updateUI() {
        mTB_Led_1_1.setChecked(showLed_1_1);
        setButtonColor(mTB_Led_1_1,showLed_1_1);
        mTB_Led_1_2.setChecked(showLed_1_2);
        setButtonColor(mTB_Led_1_2,showLed_1_2);
        mTB_Led_1_3.setChecked(showLed_1_3);
        setButtonColor(mTB_Led_1_3,showLed_1_3);
        mTB_Led_1_4.setChecked(showLed_1_4);
        setButtonColor(mTB_Led_1_4,showLed_1_4);
        mTB_Led_1_5.setChecked(showLed_1_5);
        setButtonColor(mTB_Led_1_5,showLed_1_5);
        mTB_Led_2_1.setChecked(showLed_2_1);
        setButtonColor(mTB_Led_2_1,showLed_2_1);
        mTB_Led_2_2.setChecked(showLed_2_2);
        setButtonColor(mTB_Led_2_2,showLed_2_2);
        mTB_Led_2_3.setChecked(showLed_2_3);
        setButtonColor(mTB_Led_2_3,showLed_2_3);
        mTB_Led_2_4.setChecked(showLed_2_4);
        setButtonColor(mTB_Led_2_4,showLed_2_4);
        mTB_Led_2_5.setChecked(showLed_2_5);
        setButtonColor(mTB_Led_2_5,showLed_2_5);
    }

    private void setButtonColor(ToggleButton button, boolean flag) {

        if (flag) {
            // ff是表示透明度，000000表示颜色。
            button.setTextColor(0xff000000);
            button.setBackgroundDrawable(getApplication().getDrawable(R.drawable.bg_led_open_button));
        } else {
            button.setTextColor(0xffffffff);
            button.setBackgroundDrawable(getApplication().getDrawable(R.drawable.bg_led_close_button));
        }
    }
    private void setLocalButtonColor(ToggleButton button) {
        setButtonColor(button,button.isChecked());
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_led);
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
        bundView();
    }

    private void bundView() {
        mTB_Led_1_1 = findViewById(R.id.led_1_1);
        mTB_Led_1_2 = findViewById(R.id.led_1_2);
        mTB_Led_1_3 = findViewById(R.id.led_1_3);
        mTB_Led_1_4 = findViewById(R.id.led_1_4);
        mTB_Led_1_5 = findViewById(R.id.led_1_5);
        mTB_Led_2_1 = findViewById(R.id.led_2_1);
        mTB_Led_2_2 = findViewById(R.id.led_2_2);
        mTB_Led_2_3 = findViewById(R.id.led_2_3);
        mTB_Led_2_4 = findViewById(R.id.led_2_4);
        mTB_Led_2_5 = findViewById(R.id.led_2_5);

        mTB_Led_1_1.setOnClickListener(this);
        mTB_Led_1_2.setOnClickListener(this);
        mTB_Led_1_3.setOnClickListener(this);
        mTB_Led_1_4.setOnClickListener(this);
        mTB_Led_1_5.setOnClickListener(this);
        mTB_Led_2_1.setOnClickListener(this);
        mTB_Led_2_2.setOnClickListener(this);
        mTB_Led_2_3.setOnClickListener(this);
        mTB_Led_2_4.setOnClickListener(this);
        mTB_Led_2_5.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.led_1_1:
                sendCommand(KEY_LED_1_1,mTB_Led_1_1.isChecked());
                setLocalButtonColor(mTB_Led_1_1);
                break;
            case R.id.led_1_2:
                sendCommand(KEY_LED_1_2,mTB_Led_1_2.isChecked());
                setLocalButtonColor(mTB_Led_1_2);
                break;
            case R.id.led_1_3:
                sendCommand(KEY_LED_1_3,mTB_Led_1_3.isChecked());
                setLocalButtonColor(mTB_Led_1_3);
                break;
            case R.id.led_1_4:
                sendCommand(KEY_LED_1_4,mTB_Led_1_4.isChecked());
                setLocalButtonColor(mTB_Led_1_4);
                break;
            case R.id.led_1_5:
                sendCommand(KEY_LED_1_5,mTB_Led_1_5.isChecked());
                setLocalButtonColor(mTB_Led_1_5);
                break;
            case R.id.led_2_1:
                sendCommand(KEY_LED_2_1,mTB_Led_2_1.isChecked());
                setLocalButtonColor(mTB_Led_2_1);
                break;
            case R.id.led_2_2:
                sendCommand(KEY_LED_2_2,mTB_Led_2_2.isChecked());
                setLocalButtonColor(mTB_Led_2_2);
                break;
            case R.id.led_2_3:
                sendCommand(KEY_LED_2_3,mTB_Led_2_3.isChecked());
                setLocalButtonColor(mTB_Led_2_3);
                break;
            case R.id.led_2_4:
                sendCommand(KEY_LED_2_4,mTB_Led_2_4.isChecked());
                setLocalButtonColor(mTB_Led_2_4);
                break;
            case R.id.led_2_5:
                sendCommand(KEY_LED_2_5,mTB_Led_2_5.isChecked());
                setLocalButtonColor(mTB_Led_2_5);
                break;
        }
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
                    case KEY_LED_1_1:
                        showLed_1_1 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_1_2:
                        showLed_1_2 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_1_3:
                        showLed_1_3 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_1_4:
                        showLed_1_4 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_1_5:
                        showLed_1_5 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_2_1:
                        showLed_2_1 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_2_2:
                        showLed_2_2 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_2_3:
                        showLed_2_3 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_2_4:
                        showLed_2_4 = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_2_5:
                        showLed_2_5 = (boolean) tempDataMap.get(dataKey);
                        break;

                }
            }
            mHandler.sendEmptyMessage(1055);
        }
    }

}
