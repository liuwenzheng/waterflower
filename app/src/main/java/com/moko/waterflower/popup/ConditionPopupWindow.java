package com.moko.waterflower.popup;

import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.moko.waterflower.R;
import com.moko.waterflower.activity.MainActivity;
import com.moko.waterflower.utils.ToastUtils;
import com.moko.waterflower.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/2/14
 * @Author wenzheng.liu
 * @Description
 */

public class ConditionPopupWindow extends PopupWindow {

    @Bind(R.id.et_humidity)
    EditText etHumidity;
    @Bind(R.id.et_duration)
    EditText etDuration;

    private String mId;
    private MainActivity mMainActivity;

    public ConditionPopupWindow(MainActivity activity) {
        this(activity, "");
    }

    public ConditionPopupWindow(MainActivity activity, String id) {
        mMainActivity = activity;
        mId = id;
        View layout = View.inflate(activity, R.layout.popup_water_condition, null);
        ButterKnife.bind(this, layout);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setContentView(layout);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_close, R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_close:
                dismiss();
                break;
            case R.id.btn_save:
                // 保存
                if (TextUtils.isEmpty(etHumidity.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写土壤湿度");
                    return;
                }
                if (TextUtils.isEmpty(etDuration.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写浇水时长");
                    return;
                }
                int humidity = Integer.valueOf(etHumidity.getText().toString());
                if (humidity < 0 || humidity > 1024) {
                    ToastUtils.showToast(mMainActivity, "土壤湿度范围在0~1024");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("8006");
                sb.append(mId);
                String strHum;
                String duration;
                if (humidity == 0 || Integer.valueOf(etDuration.getText().toString()) == 0) {
                    strHum = "FFFF";
                    duration = "FFFF";
                } else {
                    strHum = Utils.intToHexString(Integer.valueOf(etHumidity.getText().toString()), 2);
                    duration = Utils.intToHexString(Integer.valueOf(etDuration.getText().toString()) * 2, 2);
                }
                sb.append(strHum);
                sb.append(duration);
                mMainActivity.getBtService().getSocketThread().send(
                        mMainActivity.getBtService().renderSendMess(sb.toString()));
                dismiss();
                break;
        }
    }
}
