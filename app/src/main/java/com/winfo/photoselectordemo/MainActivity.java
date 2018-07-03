package com.winfo.photoselectordemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private final static int duoxuan = 10001;
    private final static int danxuan = 1002;
    private final static int buxian = 1003;
    private final static int clip = 1004;

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
        findViewById(R.id.btn_take_clip).setOnClickListener(this);
    }

    private ArrayList<String> images;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT);
            switch (requestCode) {
                case clip:
                    //拍照直接剪切
                    if (images == null) {
                        Uri resultUri = UCrop.getOutput(data);
                        Glide.with(this).load(resultUri).into(imageView);
                        return;
                    }
                    //选择之后剪切
                    Uri selectUri = Uri.fromFile(new File(images.get(0)));
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
                    long time = System.currentTimeMillis();
                    String imageName = timeFormatter.format(new Date(time));
                    UCrop uCrop = UCrop.of(selectUri, Uri.fromFile(new File(getCacheDir(), imageName + ".jpg")));
                    UCrop.Options options = new UCrop.Options();
                    options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    options.setCompressionQuality(100);
                    options.setFreeStyleCropEnabled(false);
                    uCrop.withOptions(options);
                    uCrop.start(this);
                    break;
                case 1005:
                    Uri resultUri2 = UCrop.getOutput(data);
                    Glide.with(this).load(resultUri2).into(imageView);
                    break;
                case UCrop.REQUEST_CROP:
                    Uri resultUri = UCrop.getOutput(data);
                    Glide.with(this).load(resultUri).into(imageView);
                    break;
                default:
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
                        .start(MainActivity.this, danxuan);
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
                        .start(MainActivity.this, duoxuan);//当前activity 和 requestCode，不传requestCode则默认为PhotoSelector.DEFAULT_REQUEST_CODE
                break;

            case R.id.btn_unlimited:
                //多选(不限数量)
                PhotoSelector.builder()
                        .setMaxSelectCount(-1)//-1不限制数量
                        .setSelected(images)
                        .start(MainActivity.this, buxian);

                break;

            case R.id.btn_clip:
                //单选后剪裁 裁剪的话都是针对一张图片所以要设置setSingle(true)
                PhotoSelector.builder()
                        .setSingle(true)
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .start(MainActivity.this, clip);
                break;

            case R.id.btn_take_clip:
                //拍照和单选之后都可以裁剪  裁剪的话都是针对一张图片所以要设置setSingle(true)
                PhotoSelector.builder()
                        .setSingle(true)
                        .setCutAfterPhotograph(true)
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setToolBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setBottomBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .start(MainActivity.this, clip);
                break;

        }
    }
}
