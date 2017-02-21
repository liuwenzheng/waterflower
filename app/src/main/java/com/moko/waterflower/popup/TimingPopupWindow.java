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

public class TimingPopupWindow extends PopupWindow {


    @Bind(R.id.et_timing)
    EditText etTiming;
    @Bind(R.id.et_duration)
    EditText etDuration;
    private String mId;
    private MainActivity mMainActivity;

    public TimingPopupWindow(MainActivity activity) {
        this(activity, "");
    }

    public TimingPopupWindow(MainActivity activity, String id) {
        mMainActivity = activity;
        mId = id;
        View layout = View.inflate(activity, R.layout.popup_water_timing, null);
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
                if (TextUtils.isEmpty(etTiming.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写定时时长");
                    return;
                }
                if (TextUtils.isEmpty(etDuration.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写浇水时长");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("8005");
                sb.append(mId);
                String time;
                String duration;
                if (Integer.valueOf(etTiming.getText().toString()) == 0
                        || Integer.valueOf(etDuration.getText().toString()) == 0) {
                    time = "FFFF";
                    duration = "FFFF";
                } else {
                    time = Utils.intToHexString(Integer.valueOf(etTiming.getText().toString()), 2);
                    duration = Utils.intToHexString(Integer.valueOf(etDuration.getText().toString()) * 2, 2);
                }
                sb.append(time);
                sb.append(duration);
                mMainActivity.getBtService().getSocketThread().send(
                        mMainActivity.getBtService().renderSendMess(sb.toString()));
                dismiss();
                break;
        }
    }
}
