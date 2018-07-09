package com.winfo.photoselector;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winfo.photoselector.adapter.FolderAdapter;
import com.winfo.photoselector.adapter.ImageAdapter;
import com.winfo.photoselector.entity.Folder;
import com.winfo.photoselector.entity.Image;
import com.winfo.photoselector.model.ImageModel;
import com.winfo.photoselector.utils.DateUtils;
import com.winfo.photoselector.utils.ImageCaptureManager;
import com.winfo.photoselector.utils.PermissionsUtils;
import com.winfo.photoselector.utils.StatusBarUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageSelectorActivity extends AppCompatActivity {

    private TextView tvTime;
    private TextView tvFolderName;
    private TextView tvConfirm;
    private TextView tvPreview;

    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;

    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private View masking;

    private ImageAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ImageCaptureManager captureManager;

    private ArrayList<Folder> mFolders;
    private Folder mFolder;
    private boolean isToSettings = false;
    private static final int PERMISSION_REQUEST_CODE = 0X00000011;

    //    private boolean isOpenFolder;
    private boolean isShowTime;
    private boolean isInitFolder;
    private RelativeLayout rlBottomBar;

    private int toolBarColor;
    private int bottomBarColor;
    private int statusBarColor;
    private int column;
    private boolean isSingle;
    private boolean showCamera;
    //    private boolean cutAfterPhotograph;
    private int mMaxCount;
    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private ArrayList<String> mSelectedImages;
    private boolean isCrop;//是否裁剪
    private int cropMode;//裁剪样式

    private Toolbar toolbar;

    private Handler mHideHandler = new Handler();
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hideTime();
        }
    };

    /**
     * 两种方式从底部弹出文件夹列表
     * 1、使用BottomSheetDialog交互性好
     * 2、使用recycleview控制显示和隐藏加入动画即可
     */
    private BottomSheetDialog bottomSheetDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        mMaxCount = bundle.getInt(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, PhotoSelector.DEFAULT_MAX_SELECTED_COUNT);
        column = bundle.getInt(PhotoSelector.EXTRA_GRID_COLUMN, PhotoSelector.DEFAULT_GRID_COLUMN);
        isSingle = bundle.getBoolean(PhotoSelector.EXTRA_SINGLE, false);
        cropMode = bundle.getInt(PhotoSelector.EXTRA_CROP_MODE, 1);
        showCamera = bundle.getBoolean(PhotoSelector.EXTRA_SHOW_CAMERA, true);
        isCrop = bundle.getBoolean(PhotoSelector.EXTRA_CROP, false);
        mSelectedImages = bundle.getStringArrayList(PhotoSelector.EXTRA_SELECTED_IMAGES);
        captureManager = new ImageCaptureManager(this);
        toolBarColor = bundle.getInt(PhotoSelector.EXTRA_TOOLBARCOLOR, ContextCompat.getColor(this, R.color.blue));
        bottomBarColor = bundle.getInt(PhotoSelector.EXTRA_BOTTOMBARCOLOR, ContextCompat.getColor(this, R.color.blue));
        statusBarColor = bundle.getInt(PhotoSelector.EXTRA_STATUSBARCOLOR, ContextCompat.getColor(this, R.color.blue));
        boolean materialDesign = bundle.getBoolean(PhotoSelector.EXTRA_MATERIAL_DESIGN, false);
        if (materialDesign) {
            setContentView(R.layout.activity_image_select);
        } else {
            setContentView(R.layout.activity_image_select2);
        }
        initView();
        StatusBarUtils.setColor(this, statusBarColor);
        setToolBarColor(toolBarColor);
        setBottomBarColor(bottomBarColor);
        initListener();
        initImageList();
        checkPermissionAndLoadImages();
        hideFolderList();
        if (mSelectedImages != null) {
            setSelectImageCount(mSelectedImages.size());
        } else {
            setSelectImageCount(0);
        }

    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        rlBottomBar = findViewById(R.id.rl_bottom_bar);
        rvImage = findViewById(R.id.rv_image);
        //第一种方式
        bottomSheetDialog = new BottomSheetDialog(this);
        @SuppressLint("InflateParams")
        View bsdFolderDialogView = getLayoutInflater().inflate(R.layout.bsd_folder_dialog, null);
        bottomSheetDialog.setContentView(bsdFolderDialogView);
        rvFolder = bsdFolderDialogView.findViewById(R.id.rv_folder);

        //第二种方式  保留使用recycleview布局显示和隐藏添加动画
//        rvFolder = findViewById(R.id.rv_folder);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvPreview = findViewById(R.id.tv_preview);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnPreview = findViewById(R.id.btn_preview);
        tvFolderName = findViewById(R.id.tv_folder_name);
        tvTime = findViewById(R.id.tv_time);
        masking = findViewById(R.id.masking);
    }


    private void initListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Image> images = new ArrayList<>(mAdapter.getSelectImages());
                toPreviewActivity(true, images, 0);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCrop && isSingle) {
                    //选择之后
                    crop(mAdapter.getSelectImages().get(0).getPath(), UCrop.REQUEST_CROP);
                } else {
                    confirm();
                }
            }
        });

        findViewById(R.id.btn_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInitFolder) {
                    openFolder();
//                    if (isOpenFolder) {
//                        closeFolder();
//                    } else {
//                        openFolder();
//                    }
                }
            }
        });

        masking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFolder();
            }
        });

        rvImage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                changeTime();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changeTime();
            }
        });
    }

    /**
     * 裁剪
     *
     * @param imagePath   照片的路径
     * @param requestCode 请求code 分选择之后  和 拍照之后
     */
    private void crop(@NonNull String imagePath, int requestCode) {
        //选择之后剪切
        Uri selectUri = Uri.fromFile(new File(imagePath));
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        UCrop uCrop = UCrop.of(selectUri, Uri.fromFile(new File(getCacheDir(), imageName + ".jpg")));
        UCrop.Options options = new UCrop.Options();
        if (cropMode == 2) {
            options.setCircleDimmedLayer(true);//是否显示圆形裁剪的layer
            options.setShowCropGrid(false);//是否显示分割线
            options.setShowCropFrame(false);//是否显示矩形边框
        }
//        options.setCircleDimmedLayer(true);//是否显示圆形裁剪的layer
//        options.setDimmedLayerColor();//设置圆形的背景色
//        options.setShowCropGrid(false);//是否显示分割线
//        options.setCropGridColor();//设置分割线的颜色
//        options.setCropGridStrokeWidth();
//        options.setCropGridColumnCount();//设置分割线的列数
//        options.setCropGridRowCount();//设置分割线的行数
//        options.setShowCropFrame(false);//是否显示矩形边框
//        options.setCropFrameStrokeWidth();//设置矩形边框的宽度
//        options.setCropFrameColor();//设置矩形边框的颜色
//        options.setFreeStyleCropEnabled(false);//设置裁剪框可移动，具体可以设置为true运行看效果
//        options.setMaxScaleMultiplier();//设置图片放大的倍数，必须大于1
//        options.setHideBottomControls();//是否显示底部控制菜单
//        options.setToolbarCropDrawable();//设置裁剪确定按钮的背景图片
//        options.setToolbarCancelDrawable();//设置裁剪取消按钮的背景图片
//        options.setImageToCropBoundsAnimDuration();//设置图片移动到矩形框的动画时间单位毫秒数
//        options.setToolbarWidgetColor();//设置toobar的view的颜色为透明颜色
//        options.setCompressionFormat();//设置裁剪之后的图片的格式
//        options.setRootViewBackgroundColor();//设置裁剪页面的根布局的颜色
//        options.withAspectRatio();//设置图片左右拉伸的长度
//        options.withMaxResultSize();//值小了会模糊
        options.setToolbarColor(toolBarColor);
        options.setStatusBarColor(statusBarColor);
        options.setActiveWidgetColor(bottomBarColor);
        options.setCompressionQuality(100);
        uCrop.withOptions(options);
        uCrop.start(ImageSelectorActivity.this, requestCode);
    }


    /**
     * 修改topbar的颜色
     *
     * @param color 颜色值
     */
    private void setToolBarColor(@ColorInt int color) {
        toolbar.setBackgroundColor(color);
    }

    /**
     * 修改bottombar的颜色
     *
     * @param color 颜色值
     */
    private void setBottomBarColor(@ColorInt int color) {
        rlBottomBar.setBackgroundColor(color);
    }


    /**
     * 初始化图片列表
     */
    private void initImageList() {
        // 判断屏幕方向
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, column);
        } else {
            mLayoutManager = new GridLayoutManager(this, 5);
        }

        rvImage.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this, mMaxCount, isSingle);
        rvImage.setAdapter(mAdapter);
        ((SimpleItemAnimator) rvImage.getItemAnimator()).setSupportsChangeAnimations(false);
        if (mFolders != null && !mFolders.isEmpty()) {
            setFolder(mFolders.get(0));
        }
        mAdapter.setOnImageSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                setSelectImageCount(selectCount);
            }
        });
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Image image, View itemView, int position) {
                toPreviewActivity(false, mAdapter.getData(), position);
            }
        });

        mAdapter.setOnCameraClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (!PermissionsUtils.checkCameraPermission(ImageSelectorActivity.this)) return;
                if (!PermissionsUtils.checkWriteStoragePermission(ImageSelectorActivity.this))
                    return;
                openCamera();
            }
        });
    }

    private String filePath;

    private void openCamera() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            //如果设置了裁剪 拍照成功之后直接进行剪切界面，则传递 TAKE_PHOTO_CROP_REQUESTCODE 然后再onActivityResult中进行判断
            if (isCrop && isSingle) {
                //获取拍照保存的照片的路径
                filePath = intent.getStringExtra(ImageCaptureManager.PHOTO_PATH);
                startActivityForResult(intent, PhotoSelector.TAKE_PHOTO_CROP_REQUESTCODE);
            } else {
                startActivityForResult(intent, PhotoSelector.TAKE_PHOTO_REQUESTCODE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            Log.e("PhotoPickerFragment", "No Activity Found to handle Intent", e);
        }
    }

    /**
     * 初始化图片文件夹列表
     */
    private void initFolderList() {
        if (mFolders != null && !mFolders.isEmpty()) {
            isInitFolder = true;
            rvFolder.setLayoutManager(new LinearLayoutManager(ImageSelectorActivity.this));
            FolderAdapter adapter = new FolderAdapter(ImageSelectorActivity.this, mFolders);
            adapter.setOnFolderSelectListener(new FolderAdapter.OnFolderSelectListener() {
                @Override
                public void OnFolderSelect(Folder folder) {
                    setFolder(folder);
                    closeFolder();
                }
            });
            rvFolder.setAdapter(adapter);
        }
    }

    /**
     * 刚开始的时候文件夹列表默认是隐藏的
     */
    private void hideFolderList() {
//        rvFolder.post(new Runnable() {
//            @Override
//            public void run() {
//                rvFolder.setTranslationY(rvFolder.getHeight());
//                rvFolder.setVisibility(View.GONE);
//            }
//        });
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder 文件夹
     */
    private void setFolder(Folder folder) {
        if (folder != null && mAdapter != null && !folder.equals(mFolder)) {
            mFolder = folder;
            tvFolderName.setText(folder.getName());
            rvImage.scrollToPosition(0);
            //如果不是文件夹不是全部图片那么不需要显示牌照
            mAdapter.refresh(folder.getImages(), folder.isUseCamera());
//            if (!folder.getName().equals("全部图片")) {
//                mAdapter.refresh(folder.getImages(), false);
//            } else {
//                //否则是全部图片则需要显示拍照按钮，传递true
//                mAdapter.refresh(folder.getImages(), showCamera);
//            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setSelectImageCount(int count) {
        if (count == 0) {
            btnConfirm.setEnabled(false);
            btnPreview.setEnabled(false);
            tvConfirm.setText(getString(R.string.confirm));
            tvPreview.setText(getString(R.string.preview));
        } else {
            btnConfirm.setEnabled(true);
            btnPreview.setEnabled(true);
            tvPreview.setText(getString(R.string.preview_count, count));
            if (isSingle) {
                tvConfirm.setText(getString(R.string.confirm));
            } else if (mMaxCount > 0) {
                tvConfirm.setText(getString(R.string.confirm_maxcount, count, mMaxCount));
            } else {
                tvConfirm.setText(getString(R.string.confirm_count, count));
            }
        }
    }

    /**
     * 弹出文件夹列表
     */
    private void openFolder() {
//        if (!isOpenFolder) {
//            masking.setVisibility(View.VISIBLE);
//            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
//                    rvFolder.getHeight(), 0).setDuration(300);
//            animator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    super.onAnimationStart(animation);
//                    rvFolder.setVisibility(View.VISIBLE);
//                }
//            });
//            animator.start();
//            isOpenFolder = true;
//        }
        bottomSheetDialog.show();
    }

    /**
     * 收起文件夹列表
     */
    private void closeFolder() {
//        if (isOpenFolder) {
//            masking.setVisibility(View.GONE);
//            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
//                    0, rvFolder.getHeight()).setDuration(300);
//            animator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    rvFolder.setVisibility(View.GONE);
//                }
//            });
//            animator.start();
//            isOpenFolder = false;
//        }
        bottomSheetDialog.dismiss();
    }

    /**
     * 隐藏时间条
     */
    private void hideTime() {
        if (isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 1, 0).setDuration(300).start();
            isShowTime = false;
        }
    }

    /**
     * 显示时间条
     */
    private void showTime() {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 0, 1).setDuration(300).start();
            isShowTime = true;
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private void changeTime() {
        int firstVisibleItem = getFirstVisibleItem();
        if (firstVisibleItem >= 0 && firstVisibleItem < mAdapter.getData().size()) {
            Image image = mAdapter.getData().get(firstVisibleItem);
            String time = DateUtils.getImageTime(image.getTime() * 1000);
            tvTime.setText(time);
            showTime();
            mHideHandler.removeCallbacks(mHide);
            mHideHandler.postDelayed(mHide, 1500);
        }
    }

    private int getFirstVisibleItem() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private void confirm() {
        if (mAdapter == null) {
            return;
        }
        //因为图片的实体类是Image，而我们返回的是String数组，所以要进行转换。
        ArrayList<Image> selectImages = mAdapter.getSelectImages();
        ArrayList<String> images = new ArrayList<>();
        for (Image image : selectImages) {
            images.add(image.getPath());
        }

        //点击确定，把选中的图片通过Intent传给上一个Activity。
        Intent intent = new Intent();
        intent.putStringArrayListExtra(PhotoSelector.SELECT_RESULT, images);
        setResult(RESULT_OK, intent);

        finish();
    }

    private void toPreviewActivity(boolean isPreview, ArrayList<Image> images, int position) {
        if (images != null && !images.isEmpty()) {
            RvPreviewActivity.openActivity(isPreview, this, images,
                    mAdapter.getSelectImages(), isSingle, mMaxCount, position
                    , toolBarColor, bottomBarColor, statusBarColor);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isToSettings) {
            isToSettings = false;
            checkPermissionAndLoadImages();
        }
    }

    /**
     * 处理图片预览页返回的结果
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhotoSelector.RESULT_CODE: //预览
                if (data != null && data.getBooleanExtra(PhotoSelector.IS_CONFIRM, false)) {
                    //如果用户在预览界面点击了确定，并且是单选裁剪模式那么就进行裁剪
                    if (isSingle && isCrop) {
                        crop(mAdapter.getSelectImages().get(0).getPath(), UCrop.REQUEST_CROP);
                    } else {
                        //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                        confirm();
                    }
                } else {
                    //否则，就刷新当前页面。
                    mAdapter.notifyDataSetChanged();
                    setSelectImageCount(mAdapter.getSelectImages().size());
                }
                break;
            case PhotoSelector.TAKE_PHOTO_REQUESTCODE://拍照不裁剪
                //拍照完成了，重新加载照片的列表，不进入剪切界面
                loadImageForSDCard();
                setSelectImageCount(mAdapter.getSelectImages().size());
                mSelectedImages = new ArrayList<>();
                for (Image image : mAdapter.getSelectImages()) {
                    mSelectedImages.add(image.getPath());
                }
                mAdapter.setSelectedImages(mSelectedImages);
                mAdapter.notifyDataSetChanged();
                break;
            case PhotoSelector.TAKE_PHOTO_CROP_REQUESTCODE://拍照裁剪，进入裁剪界面传递requestcode
                //拍照完成了，获取到照片之后直接进入剪切界面,用户可能没有确定剪切
                crop(filePath, PhotoSelector.CROP_REQUESTCODE);
                break;
            case UCrop.REQUEST_CROP:
                //选择之后裁剪，获取到裁剪的数据直接返回
                if (data != null) {
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    //如果选择之后没有裁剪 用户按返回键的话，那么data就是null做下判断，就刷新当前页面。
                    mAdapter.notifyDataSetChanged();
                    setSelectImageCount(mAdapter.getSelectImages().size());
                }
                break;
            case PhotoSelector.CROP_REQUESTCODE://拍照成功之后去裁剪，裁剪返回的结果
                //拍照之后裁剪，如果拍照成功之后没有裁剪 用户按返回键的话，那么data就是null的做下判断，就刷新列表加载出用户拍照的照片
                // 如果data不为null就说明他裁剪了
                if (data != null) {
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    loadImageForSDCard();
                    setSelectImageCount(mAdapter.getSelectImages().size());
                    mSelectedImages = new ArrayList<>();
                    for (Image image : mAdapter.getSelectImages()) {
                        mSelectedImages.add(image.getPath());
                    }
                    mAdapter.setSelectedImages(mSelectedImages);
                    mAdapter.notifyDataSetChanged();
                }
                break;

        }
    }

    /**
     * 横竖屏切换处理
     *
     * @param newConfig newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mLayoutManager != null && mAdapter != null) {
            //切换为竖屏
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLayoutManager.setSpanCount(3);
            }
            //切换为横屏
            else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mLayoutManager.setSpanCount(5);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private void checkPermissionAndLoadImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(this, "没有图片", Toast.LENGTH_LONG).show();
            return;
        }
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，加载图片。
            loadImageForSDCard();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 处理权限申请的回调。
     *
     * @param requestCode  requestCode
     * @param permissions  permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，加载图片。
                loadImageForSDCard();
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog();
            }
        }
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private void showExceptionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage("该相册需要赋予访问存储的权限，请到“设置”>“应用”>“权限”中配置权限。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startAppSettings();
                isToSettings = true;
            }
        }).show();
    }

    /**
     * 从SDCard加载图片。
     */
    private void loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this, new ImageModel.DataCallback() {
            @Override
            public void onSuccess(ArrayList<Folder> folders) {
                mFolders = folders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFolders != null && !mFolders.isEmpty()) {
                            initFolderList();
                            mFolders.get(0).setUseCamera(showCamera);
                            setFolder(mFolders.get(0));
                            if (mSelectedImages != null && mAdapter != null) {
                                mAdapter.setSelectedImages(mSelectedImages);
                                mSelectedImages = null;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 启动应用的设置
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

}
