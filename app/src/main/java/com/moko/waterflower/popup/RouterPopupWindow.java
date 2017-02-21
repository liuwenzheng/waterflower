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

public class RouterPopupWindow extends PopupWindow {

    @Bind(R.id.et_ssid)
    EditText etSsid;
    @Bind(R.id.et_password)
    EditText etPassword;
    private MainActivity mMainActivity;

    public RouterPopupWindow(MainActivity activity) {
        this(activity, "", "");
    }

    public RouterPopupWindow(MainActivity activity, String ssid, String password) {
        mMainActivity = activity;
        View layout = View.inflate(activity, R.layout.popup_router, null);
        ButterKnife.bind(this, layout);
        etSsid.setText(ssid);
        etPassword.setText(password);
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
                if (TextUtils.isEmpty(etSsid.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写SSID");
                    return;
                }
                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    ToastUtils.showToast(mMainActivity, "请填写密码");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("800A");
                String ssid = Utils.string2HexString(etSsid.getText().toString());
                sb.append(Utils.intToHexString(ssid.length() / 2, 1));
                sb.append(ssid);
                String password = Utils.string2HexString(etPassword.getText().toString());
                sb.append(Utils.intToHexString(password.length() / 2, 1));
                sb.append(password);
                mMainActivity.getBtService().getSocketThread().send(
                        mMainActivity.getBtService().renderSendMess(sb.toString()));
                dismiss();
                break;
        }
    }
}
