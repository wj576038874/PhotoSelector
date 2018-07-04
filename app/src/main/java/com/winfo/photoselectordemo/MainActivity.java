package com.winfo.photoselectordemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.winfo.photoselector.PhotoSelector;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SINGLE_CODE = 1;//单选
    private static final int LIMIT_CODE = 2;//多选限制数量
    private static final int CROP_CODE = 3;//剪切裁剪
    private static final int UN_LIMITT_CODE = 4;//多选不限制数量

    private ImageAdapter mAdapter;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageview);
        RecyclerView rvImage = findViewById(R.id.rv_image);
        rvImage.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new ImageAdapter(this);
        rvImage.setAdapter(mAdapter);

        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_limit).setOnClickListener(this);
        findViewById(R.id.btn_unlimited).setOnClickListener(this);
        findViewById(R.id.btn_clip).setOnClickListener(this);
    }

    private ArrayList<String> images;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case SINGLE_CODE:
                    //单选的话 images就只有一条数据直接get(0)即可
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT);
                    mAdapter.refresh(images);
                    break;
                case LIMIT_CODE:
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT);
                    mAdapter.refresh(images);
                    break;
                case CROP_CODE:
                    Uri resultUri = PhotoSelector.getCropImage(data);
                    Glide.with(this).load(resultUri).into(imageView);
                    break;
                case UN_LIMITT_CODE:
                    images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT);
                    mAdapter.refresh(images);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_single:
                //单选
                PhotoSelector.builder()
                        .setSingle(true)
                        .start(MainActivity.this, SINGLE_CODE);
                break;

            case R.id.btn_limit:
                //多选(最多9张)
                PhotoSelector.builder()
                        .setShowCamera(true)//显示拍照
                        .setMaxSelectCount(9)//最大选择9 默认9，如果这里设置为-1则是不限数量
                        .setSelected(images)//已经选择的照片
                        .setGridColumnCount(3)//列数
                        .setMaterialDesign(true)//design风格
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//toolbar的颜色
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//底部bottombar的颜色
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))//状态栏的颜色
                        .start(MainActivity.this, LIMIT_CODE);//当前activity 和 requestCode，不传requestCode则默认为PhotoSelector.DEFAULT_REQUEST_CODE
                break;

            case R.id.btn_unlimited:
                //多选(不限数量)
                PhotoSelector.builder()
                        .setMaxSelectCount(-1)//-1不限制数量
                        .setSelected(images)
                        .start(MainActivity.this, UN_LIMITT_CODE);
                break;

            case R.id.btn_clip:
                //单选后剪裁 裁剪的话都是针对一张图片所以要设置setSingle(true)
                PhotoSelector.builder()
                        .setSingle(true)
                        .setCrop(true)
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .start(MainActivity.this, CROP_CODE);
                break;
        }
    }
}
