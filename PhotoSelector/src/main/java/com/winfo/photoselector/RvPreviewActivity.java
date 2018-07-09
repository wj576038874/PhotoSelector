package com.winfo.photoselector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winfo.photoselector.adapter.BottomPreviewAdapter;
import com.winfo.photoselector.adapter.PreviewImageAdapter;
import com.winfo.photoselector.entity.Image;
import com.winfo.photoselector.utils.ImageUtil;
import com.winfo.photoselector.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

import static android.animation.ObjectAnimator.ofFloat;

public class RvPreviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    //    private TextView tvIndicator;
    private TextView tvConfirm;
    private FrameLayout btnConfirm;
    private TextView tvSelect;
    private RelativeLayout rlBottomBar;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    //tempImages和tempSelectImages用于图片列表数据的页面传输。
    //之所以不要Intent传输这两个图片列表，因为要保证两位页面操作的是同一个列表数据，同时可以避免数据量大时，
    // 用Intent传输发生的错误问题。
    private static ArrayList<Image> tempImages;
    private static ArrayList<Image> tempSelectImages;

    private ArrayList<Image> mImages;
    private ArrayList<Image> mSelectImages;
    private boolean isShowBar = true;
    private boolean isConfirm = false;
    private boolean isSingle;
    private int mMaxCount;

    private BitmapDrawable mSelectDrawable;
    private BitmapDrawable mUnSelectDrawable;

    /*-----------------------------------*/
    private RecyclerView bottomRecycleview;
    private BottomPreviewAdapter bottomPreviewAdapter;
    private View line;
    private boolean isPreview;//是否点击预览按钮进入此页面

    /**
     * @param activity       activity
     * @param images         images
     * @param selectImages   选中的图片
     * @param isSingle       是否单选
     * @param maxSelectCount 最大选择数
     * @param position       posttion
     * @param toolBarColor   toolBarColor颜色值
     * @param bottomBarColor bottomBarColor颜色值
     * @param statusBarColor statusBarColor颜色值
     */
    public static void openActivity(boolean isPreview, Activity activity, ArrayList<Image> images,
                                    ArrayList<Image> selectImages,
                                    boolean isSingle,
                                    int maxSelectCount,
                                    int position,
                                    @ColorInt int toolBarColor,
                                    @ColorInt int bottomBarColor,
                                    @ColorInt int statusBarColor) {
        tempImages = images;
        tempSelectImages = selectImages;
        Intent intent = new Intent(activity, RvPreviewActivity.class);
        intent.putExtra(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, maxSelectCount);
        intent.putExtra(PhotoSelector.EXTRA_SINGLE, isSingle);
        intent.putExtra(PhotoSelector.EXTRA_POSITION, position);
        intent.putExtra(PhotoSelector.EXTRA_ISPREVIEW, isPreview);
        intent.putExtra(PhotoSelector.EXTRA_TOOLBARCOLOR, toolBarColor);
        intent.putExtra(PhotoSelector.EXTRA_BOTTOMBARCOLOR, bottomBarColor);
        intent.putExtra(PhotoSelector.EXTRA_STATUSBARCOLOR, statusBarColor);
        activity.startActivityForResult(intent, PhotoSelector.RESULT_CODE);
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, "aa");
//        //与xml文件对应
//        ActivityCompat.startActivityForResult(activity, intent, Constants.RESULT_CODE, options.toBundle());
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_preview);
        appBarLayout = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setStatusBarVisible(true);
        mImages = tempImages;
        tempImages = null;
        mSelectImages = tempSelectImages;
        tempSelectImages = null;

        Intent intent = getIntent();
        mMaxCount = intent.getIntExtra(PhotoSelector.EXTRA_MAX_SELECTED_COUNT, 0);
        isSingle = intent.getBooleanExtra(PhotoSelector.EXTRA_SINGLE, false);
        isPreview = intent.getBooleanExtra(PhotoSelector.EXTRA_ISPREVIEW, false);
        Resources resources = getResources();
        Bitmap selectBitmap = ImageUtil.getBitmap(this, R.drawable.ic_image_select);
        mSelectDrawable = new BitmapDrawable(resources, selectBitmap);
        mSelectDrawable.setBounds(0, 0, selectBitmap.getWidth(), selectBitmap.getHeight());

        Bitmap unSelectBitmap = ImageUtil.getBitmap(this, R.drawable.ic_image_un_select);
        mUnSelectDrawable = new BitmapDrawable(resources, unSelectBitmap);
        mUnSelectDrawable.setBounds(0, 0, unSelectBitmap.getWidth(), unSelectBitmap.getHeight());

        int toolBarColor = intent.getIntExtra(PhotoSelector.EXTRA_TOOLBARCOLOR, ContextCompat.getColor(this, R.color.blue));
        int bottomBarColor = intent.getIntExtra(PhotoSelector.EXTRA_BOTTOMBARCOLOR, ContextCompat.getColor(this, R.color.blue));
        int statusBarColor = intent.getIntExtra(PhotoSelector.EXTRA_STATUSBARCOLOR, ContextCompat.getColor(this, R.color.blue));

        initView();

        StatusBarUtils.setBarColor(this, statusBarColor);
        setToolBarColor(toolBarColor);
        setBottomBarColor(bottomBarColor);
        initListener();
        initViewPager();


        changeSelect(mImages.get(intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0)));
//        vpImage.setCurrentItem(intent.getIntExtra(Constants.POSITION, 0));
        recyclerView.scrollToPosition(intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0));
        toolbar.setTitle(intent.getIntExtra(PhotoSelector.EXTRA_POSITION, 0) + 1 + "/" + mImages.size());
        if (isPreview) {
            bottomRecycleview.smoothScrollToPosition(0);
        }
//        tvIndicator.setText();

    }

    private void initView() {
        recyclerView = findViewById(R.id.rv_preview);
//        tvIndicator = findViewById(R.id.tv_indicator);
        tvConfirm = findViewById(R.id.tv_confirm);
        btnConfirm = findViewById(R.id.btn_confirm);
        tvSelect = findViewById(R.id.tv_select);
        rlBottomBar = findViewById(R.id.rl_bottom_bar);
        bottomRecycleview = findViewById(R.id.bottom_recycleview);
        line = findViewById(R.id.line);
        bottomRecycleview.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));
        if (mSelectImages.size() == 0) {
            bottomRecycleview.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }
        bottomPreviewAdapter = new BottomPreviewAdapter(this, mSelectImages);
        bottomRecycleview.setAdapter(bottomPreviewAdapter);
//        LinearSnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(bottomRecycleview);
    }

    private void initListener() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirm = true;
                finish();
            }
        });

        tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSelect();
            }
        });

        bottomPreviewAdapter.setOnItemClcikLitener(new BottomPreviewAdapter.OnItemClcikLitener() {
            @Override
            public void OnItemClcik(int position, Image image) {
                if (isPreview) {
                    List<Image> imageList = previewImageAdapter.getData();
                    for (int i = 0; i < imageList.size(); i++) {
                        if (imageList.get(i).equals(image)) {
                            recyclerView.smoothScrollToPosition(i);
                        }
                    }
                } else {
                    recyclerView.smoothScrollToPosition(mSelectImages.get(position).getPosition());
                }
                bottomPreviewAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 初始化ViewPager
     */
    private PreviewImageAdapter previewImageAdapter;

    private void initViewPager() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        previewImageAdapter = new PreviewImageAdapter(this, mImages);
        recyclerView.setAdapter(previewImageAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
//        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        previewImageAdapter.setOnItemClcikLitener(new PreviewImageAdapter.OnItemClcikLitener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void OnItemClcik(PreviewImageAdapter previewImageAdapter, View iteView, int position) {
                if (isShowBar) {
                    hideBar();
                } else {
                    showBar();
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = linearLayoutManager.findLastVisibleItemPosition();
                    mImages.get(position).setPosition(position);
                    toolbar.setTitle((position + 1) + "/" + mImages.size());
                    changeSelect(mImages.get(position));
                }
            }
        });

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
     * 显示和隐藏状态栏
     *
     * @param show 是否显示
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setStatusBarVisible(boolean show) {
        if (show) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /**
     * 显示头部和尾部栏
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showBar() {
        isShowBar = true;
        setStatusBarVisible(true);
        //添加延时，保证StatusBar完全显示后再进行动画。
        appBarLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (appBarLayout != null) {
                    ObjectAnimator animator = ofFloat(appBarLayout, "translationY",
                            appBarLayout.getTranslationY(), 0).setDuration(300);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            if (appBarLayout != null) {
                                appBarLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    animator.start();
                    ofFloat(rlBottomBar, "translationY", rlBottomBar.getTranslationY(), 0)
                            .setDuration(300).start();
                }
            }
        }, 100);
    }

    /**
     * 隐藏头部和尾部栏
     */
    private void hideBar() {
        isShowBar = false;
        ObjectAnimator animator = ObjectAnimator.ofFloat(appBarLayout, "translationY",
                0, -appBarLayout.getHeight()).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (appBarLayout != null) {
                    appBarLayout.setVisibility(View.GONE);
                    //添加延时，保证rlTopBar完全隐藏后再隐藏StatusBar。
                    appBarLayout.postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void run() {
                            setStatusBarVisible(false);
                        }
                    }, 5);
                }
            }
        });
        animator.start();
        ofFloat(rlBottomBar, "translationY", 0, rlBottomBar.getHeight())
                .setDuration(300).start();
    }

    private void clickSelect() {
        final int position = linearLayoutManager.findFirstVisibleItemPosition();
        if (mImages != null && mImages.size() > position) {
            Image image = mImages.get(position);
            if (mSelectImages.contains(image)) {
                mSelectImages.remove(image);
            } else if (isSingle) {
                mSelectImages.clear();
                mSelectImages.add(image);
            } else if (mMaxCount <= 0 || mSelectImages.size() < mMaxCount) {
//                image.setSelectPosition(mSelectImages.size());
                mSelectImages.add(image);
            } else {
                Toast.makeText(RvPreviewActivity.this, "最多只能选" + mMaxCount + "张", Toast.LENGTH_SHORT).show();
            }
            bottomPreviewAdapter.referesh(mSelectImages);
            bottomPreviewAdapter.notifyDataSetChanged();
            changeSelect(image);
        }
        if (mSelectImages.size() > 0) {
            bottomRecycleview.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);
        } else {
            bottomRecycleview.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }
    }

    private void changeSelect(Image image) {
        tvSelect.setCompoundDrawables(mSelectImages.contains(image) ?
                mSelectDrawable : mUnSelectDrawable, null, null, null);
        setSelectImageCount(mSelectImages.size());
        //清空所有选择的照片的边框背景
        for (Image image1 : mSelectImages) {
            image1.setChecked(false);
        }
        //设置当前选中打的照片的背景
        image.setChecked(true);
        bottomPreviewAdapter.referesh(mSelectImages);
        bottomPreviewAdapter.notifyDataSetChanged();
        if (mSelectImages.contains(image)) {
            bottomRecycleview.smoothScrollToPosition(image.getSelectPosition());
        }
    }

    private void setSelectImageCount(int count) {
        if (count == 0) {
            btnConfirm.setEnabled(false);
            tvConfirm.setText(getString(R.string.confirm));
        } else {
            btnConfirm.setEnabled(true);
            if (isSingle) {
                tvConfirm.setText(getString(R.string.confirm));
            } else if (mMaxCount > 0) {
                tvConfirm.setText(getString(R.string.confirm_maxcount, count, mMaxCount));
            } else {
                tvConfirm.setText(getString(R.string.confirm_count, count));
            }
        }
    }

    @Override
    public void finish() {
        //Activity关闭时，通过Intent把用户的操作(确定/返回)传给ImageSelectActivity。
        Intent intent = new Intent();
        intent.putExtra(PhotoSelector.IS_CONFIRM, isConfirm);
        setResult(PhotoSelector.RESULT_CODE, intent);
        super.finish();
    }
}
