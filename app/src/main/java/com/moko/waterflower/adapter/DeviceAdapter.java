package com.moko.waterflower.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.moko.waterflower.R;
import com.moko.waterflower.entity.Device;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2017/2/13
 * @Author wenzheng.liu
 * @Description
 */

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<Device> mEntities;
    private WaterOnClickListener mListener;

    public DeviceAdapter(Context mContext, List<Device> mEntities, WaterOnClickListener listener) {
        this.mContext = mContext;
        this.mEntities = mEntities;
        this.mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_device, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        initializeItemView((MyViewHolder) holder, mEntities.get(position));
    }

    private void initializeItemView(MyViewHolder holder, Device device) {
        holder.tvDeviceId.setText(device.id);
        holder.tvDeviceTime.setText(device.time);
        holder.tvDevicePower.setText(String.format("%sV", device.power));
        holder.tvEnvHumidity.setText(String.format("环境湿度：%s", device.envHumidity + "%"));
        holder.tvSoilHumidity.setText(String.format("土壤湿度：%s", device.soilHumidity + "%"));
        holder.tvEnvTemp.setText(String.format("环境温度：%s℃", device.envTemp));
        holder.tvWater.setText(String.format("水量：%s", device.water + "%"));
        holder.tvDeviceLight.setText(String.format(" 光照：%slx", device.light));
        holder.btnWaterCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.waterCondition();
            }
        });
        holder.btnWaterTiming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.waterTiming();
            }
        });
        holder.btnWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.water();
            }
        });
        holder.btnPvm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.pvm();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEntities == null ? 0 : mEntities.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_device_id)
        TextView tvDeviceId;
        @Bind(R.id.tv_device_time)
        TextView tvDeviceTime;
        @Bind(R.id.tv_device_power)
        TextView tvDevicePower;
        @Bind(R.id.tv_env_humidity)
        TextView tvEnvHumidity;
        @Bind(R.id.tv_soil_humidity)
        TextView tvSoilHumidity;
        @Bind(R.id.tv_env_temp)
        TextView tvEnvTemp;
        @Bind(R.id.tv_water)
        TextView tvWater;
        @Bind(R.id.tv_device_light)
        TextView tvDeviceLight;
        @Bind(R.id.btn_water_condition)
        Button btnWaterCondition;
        @Bind(R.id.btn_water_timing)
        Button btnWaterTiming;
        @Bind(R.id.btn_water)
        Button btnWater;
        @Bind(R.id.btn_pvm)
        Button btnPvm;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface WaterOnClickListener {
        void waterCondition();

        void waterTiming();

        void water();

        void pvm();
    }
}
