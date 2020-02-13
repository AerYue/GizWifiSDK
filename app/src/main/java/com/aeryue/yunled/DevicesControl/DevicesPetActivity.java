package com.aeryue.yunled.DevicesControl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.aeryue.yunled.R;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;

import java.util.concurrent.ConcurrentHashMap;

public class DevicesPetActivity extends BaseDevicesControlActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Switch mSw_LedOnOff;
    private Spinner mSp_LedColor;
    private TextView mTV_RedResult;
    private SeekBar mSB_RedResult;
    private TextView mTV_GreenResult;
    private SeekBar mSB_GreenResult;
    private TextView mTV_BlueResult;
    private SeekBar mSB_BlueResult;
    private TextView mTV_MotorResult;
    private SeekBar mSB_MotorResult;
    private Switch mSw_Infrared;
    private TextView mTV_TempeValue;
    private TextView mTV_HumidityValue;


    private final static String KEY_LED_ONOFF = "LED_OnOff";
    private final static String KEY_LED_COLOR = "LED_Color";
    private final static String KEY_LED_R = "LED_R";
    private final static String KEY_LED_G = "LED_G";
    private final static String KEY_LED_B = "LED_B";
    private final static String KEY_MOTOR_SPEED = "Motor_Speed";
    private final static String KEY_INFRARED = "Infrared";
    private final static String KEY_TEMPERATURE = "Temperature";
    private final static String KEY_HUMIDITY = "Humidity";

    //临时全局变量
    private boolean showLedStatus = false;
    private int showLedGroupColor = 0;
    private int showRedValue = 0;
    private int showGreenValue = 0;
    private int showBlueValue = 0;
    private int showMotorValue = 0;
    private boolean showInfraredStatus = false;
    private int showTemperature = 0;
    private int showHumidity = 0;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
              if(msg.what == 108){
                  updateUI();
              }
        }
    };

    private void updateUI() {
        mSw_LedOnOff.setChecked(showLedStatus);
        mSp_LedColor.setSelection(showLedGroupColor);
        mTV_RedResult.setText(showRedValue+"");
        mSB_RedResult.setProgress(showRedValue);
        mTV_GreenResult.setText(showGreenValue+"");
        mSB_GreenResult.setProgress(showGreenValue);
        mTV_BlueResult.setText(showBlueValue+"");
        mSB_BlueResult.setProgress(showBlueValue);
        mTV_MotorResult.setText(showMotorValue+"");
        mSB_MotorResult.setProgress(showMotorValue+5);
        mSw_Infrared.setChecked(showInfraredStatus);
        mTV_TempeValue.setText(showTemperature+"°");
        mTV_HumidityValue.setText(showHumidity+"%");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_pet);
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

    private void bundView(){
        mSw_LedOnOff = findViewById(R.id.switch_led_onoff);
        mSp_LedColor = findViewById(R.id.spinner_led_group_color);
        mTV_RedResult = findViewById(R.id.textview_red_result);
        mSB_RedResult = findViewById(R.id.seekbar_red_value);
        mTV_GreenResult = findViewById(R.id.textview_green_result);
        mSB_GreenResult = findViewById(R.id.seekbar_green_value);
        mTV_BlueResult = findViewById(R.id.textview_blue_result);
        mSB_BlueResult = findViewById(R.id.seekbar_blue_value);
        mTV_MotorResult = findViewById(R.id.textview_motor_result);
        mSB_MotorResult = findViewById(R.id.seekbar_motor_value);
        mSw_Infrared = findViewById(R.id.swich_infrared_detection);
        mTV_TempeValue = findViewById(R.id.textview_temperature_value);
        mTV_HumidityValue = findViewById(R.id.textview_humidity_value);

        mSB_RedResult.setMax(254);
        mSB_GreenResult.setMax(254);
        mSB_BlueResult.setMax(254);
        mSB_MotorResult.setMax(10);

        //UI界面点击事件初始化
        mSw_LedOnOff.setOnClickListener(this);
        mSp_LedColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>=0&&position<=3){
                    sendCommand(KEY_LED_COLOR,position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSB_RedResult.setOnSeekBarChangeListener(this);
        mSB_GreenResult.setOnSeekBarChangeListener(this);
        mSB_BlueResult.setOnSeekBarChangeListener(this);
        mSB_MotorResult.setOnSeekBarChangeListener(this);
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
                    case KEY_LED_ONOFF:
                        showLedStatus = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_COLOR:
                        showLedGroupColor = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_R:
                        showRedValue = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_G:
                        showGreenValue = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_LED_B:
                        showBlueValue = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_MOTOR_SPEED:
                        showMotorValue = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_INFRARED:
                        showInfraredStatus = (boolean) tempDataMap.get(dataKey);
                        break;
                    case KEY_TEMPERATURE:
                        showTemperature = (int) tempDataMap.get(dataKey);
                        break;
                    case KEY_HUMIDITY:
                        showHumidity = (int) tempDataMap.get(dataKey);
                        break;
                }
            }
            mHandler.sendEmptyMessage(108);
        }
    }

    //Switch点击事件
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.switch_led_onoff){
            sendCommand(KEY_LED_ONOFF,mSw_LedOnOff.isChecked());
        }
    }

    //SeekBar的回调
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()){
            case R.id.seekbar_red_value:
                sendCommand(KEY_LED_R,mSB_RedResult.getProgress());
                break;
            case R.id.seekbar_green_value:
                sendCommand(KEY_LED_G,mSB_GreenResult.getProgress());
                break;
            case R.id.seekbar_blue_value:
                sendCommand(KEY_LED_B,mSB_BlueResult.getProgress());
                break;
            case R.id.seekbar_motor_value:
                sendCommand(KEY_MOTOR_SPEED,mSB_MotorResult.getProgress()-5);
                break;
            default:
                    break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seekbar_red_value:
                mTV_RedResult.setText(progress+"");
                break;
            case R.id.seekbar_green_value:
                mTV_GreenResult.setText(progress+"");
                break;
            case R.id.seekbar_blue_value:
                mTV_BlueResult.setText(progress+"");
                break;
            case R.id.seekbar_motor_value:
                mTV_MotorResult.setText(progress-5+"");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


}
