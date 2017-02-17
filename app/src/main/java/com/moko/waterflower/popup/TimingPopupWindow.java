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

public class TimingPopupWindow extends PopupWindow {


    @Bind(R.id.et_timing)
    EditText etTiming;
    @Bind(R.id.et_duration)
    EditText etDuration;

    public TimingPopupWindow(MainActivity activity) {
        this(activity, "", "");
    }

    public TimingPopupWindow(MainActivity activity, String timing, String duration) {
        View layout = View.inflate(activity, R.layout.popup_water_timing, null);
        ButterKnife.bind(this, layout);
        etTiming.setText(timing);
        etDuration.setText(duration);
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
