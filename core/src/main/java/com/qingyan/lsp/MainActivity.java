package com.qingyan.lsp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.lsposed.lspd.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setTitle(getTitleX());
        Log.e("BugHook", "我是原始onCreate: " );

    }
    private static String getTitleX(){
        return "原标题";
    }
}