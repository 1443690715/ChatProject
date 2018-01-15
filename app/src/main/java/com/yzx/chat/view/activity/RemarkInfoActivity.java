package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

/**
 * Created by YZX on 2018年01月15日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RemarkInfoActivity extends BaseCompatActivity {

    private Toolbar mToolbar;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_remark_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setView();
    }

    private void init() {
        mToolbar = findViewById(R.id.RemarkInfoActivity_mToolbar);
    }

    private void setView(){
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
