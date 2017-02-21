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

public class PWMPopupWindow extends PopupWindow {


    @Bind(R.id.et_pwm)
    EditText etPwm;
    @Bind(R.id.et_duration)
    EditText etDuration;

    private String mId;
    private MainActivity mMainActivity;

    public PWMPopupWindow(MainActivity activity) {
        this(activity, "");
    }

    public PWMPopupWindow(MainActivity activity, String id) {
        mMainActivity = activity;
        mId = id;
        View layout = View.inflate(activity, R.layout.popup_water_pwm, null);
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
                if (TextUtils.isEmpty(etPwm.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写PWM占比");
                    return;
                }
                if (TextUtils.isEmpty(etDuration.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写高电压时间");
                    return;
                }
                int pwm = Integer.valueOf(etPwm.getText().toString());
                if (pwm < 0 || pwm > 100) {
                    ToastUtils.showToast(mMainActivity, "PWM占比范围在0~100");
                    return;
                }
                int duration = Integer.valueOf(etDuration.getText().toString());
                if (duration < 0 || duration > 10) {
                    ToastUtils.showToast(mMainActivity, "高电压时间范围在0~10");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("8008");
                sb.append(mId);
                String strPwm = Utils.intToHexString(pwm, 1);
                String strDuration = Utils.intToHexString(duration * 2, 1);
                sb.append(strPwm);
                sb.append(strDuration);
                mMainActivity.getBtService().getSocketThread().send(
                        mMainActivity.getBtService().renderSendMess(sb.toString()));
                dismiss();
                break;
        }
    }
}
