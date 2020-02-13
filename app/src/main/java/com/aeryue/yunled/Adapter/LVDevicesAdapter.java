package com.aeryue.yunled.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aeryue.yunled.R;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;

import java.util.List;

public class LVDevicesAdapter extends BaseAdapter {

    private Context mContext;
    private List<GizWifiDevice> mGizWifiDeviceList;
    private LayoutInflater mLayoutInflater;

    public LVDevicesAdapter(Context context,List<GizWifiDevice> gizWifiDeviceList){
        mContext = context;
        mGizWifiDeviceList = gizWifiDeviceList;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mGizWifiDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mGizWifiDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodlerListView mHodlerListView = null;
        View view;
        GizWifiDevice devices = mGizWifiDeviceList.get(position);
        if(convertView==null){
            view = mLayoutInflater.inflate(R.layout.item_listview_device,null);
            mHodlerListView = new ViewHodlerListView();
            mHodlerListView.mDevicesIcon = view.findViewById(R.id.image_device_icon);
            mHodlerListView.mDevicesName = view.findViewById(R.id.devices_name);
            mHodlerListView.mDevicesStatus = view.findViewById(R.id.devices_status);
            mHodlerListView.mRightIcon = view.findViewById(R.id.image_right);
            view.setTag(mHodlerListView);
        }else {
            view = convertView;
            mHodlerListView = (ViewHodlerListView) view.getTag();
        }
        //设置名字
        //如果用户设置了别名，优先显示别名
        if(devices.getAlias().isEmpty()) {
            mHodlerListView.mDevicesName.setText(devices.getProductName());
        }else {
            mHodlerListView.mDevicesName.setText(devices.getAlias());
        }

        //设置状态
        if(devices.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline){
            //离线
            mHodlerListView.mDevicesStatus.setTextColor(
                    mContext.getResources().getColor(R.color.app_color_red));
            mHodlerListView.mDevicesStatus.setText("离线");
            mHodlerListView.mRightIcon.setVisibility(View.INVISIBLE);
            //mHodlerListView.mRightIcon.setVisibility(View.GONE);
        }else{
            //在线
            //判断局域网还是远程连接
            if(devices.isLAN()){
                mHodlerListView.mDevicesStatus.setText("本地在线");
            }else{
                mHodlerListView.mDevicesStatus.setText("远程在线");
            }
            mHodlerListView.mRightIcon.setVisibility(View.VISIBLE);
            mHodlerListView.mDevicesStatus.setTextColor(
                    mContext.getResources().getColor(R.color.app_color_green));
        }

        return view;
    }

    private class ViewHodlerListView{
        //设备图标,右箭头图标
        ImageView mDevicesIcon;
        ImageView mRightIcon;

        //设备名称，状态
        TextView mDevicesName;
        TextView mDevicesStatus;
    }

}
