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

public class CloudPlatformPopupWindow extends PopupWindow {


    @Bind(R.id.et_ip)
    EditText etIp;
    @Bind(R.id.et_port)
    EditText etPort;
    private MainActivity mMainActivity;

    public CloudPlatformPopupWindow(MainActivity activity) {
        this(activity, "", "");
    }

    public CloudPlatformPopupWindow(MainActivity activity, String ip, String port) {
        mMainActivity = activity;
        View layout = View.inflate(activity, R.layout.popup_cloud_platform, null);
        ButterKnife.bind(this, layout);
        etIp.setText(ip);
        etPort.setText(port);
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
                if (TextUtils.isEmpty(etIp.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写IP");
                    return;
                }
                if (TextUtils.isEmpty(etPort.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写端口号");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("8009");
                String ip = Utils.string2HexString(etIp.getText().toString());
                sb.append(Utils.intToHexString(ip.length() / 2, 1));
                sb.append(ip);
                sb.append(Utils.intToHexString(Integer.valueOf(etPort.getText().toString()), 2));
                mMainActivity.getBtService().renderSendMess(sb.toString());
                dismiss();
                break;
        }
    }
}
