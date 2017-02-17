package com.moko.waterflower.popup;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.moko.waterflower.R;
import com.moko.waterflower.activity.MainActivity;

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

    public RouterPopupWindow(MainActivity activity) {
        this(activity, "", "");
    }

    public RouterPopupWindow(MainActivity activity, String ssid, String password) {
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
                // TODO: 2017/2/14 保存
                dismiss();
                break;
        }
    }
}
