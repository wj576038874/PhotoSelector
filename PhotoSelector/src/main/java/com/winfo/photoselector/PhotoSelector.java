package com.winfo.photoselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.winfo.photoselector.utils.PermissionsUtils;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;

public class PhotoSelector {

    /**
     * 默认最大选择数
     */
    public static final int DEFAULT_MAX_SELECTED_COUNT = 9;

    /**
     * 默认显示的列数
     */
    public static final int DEFAULT_GRID_COLUMN = 3;

    /**
     * 默认的requesrCode
     */
    public static final int DEFAULT_REQUEST_CODE = 999;

    /**
     * 拍照裁剪
     */
    public static final int TAKE_PHOTO_CROP_REQUESTCODE = 1001;

    /**
     * 拍照 不裁剪
     */
    public static final int TAKE_PHOTO_REQUESTCODE = 1002;

    public static final int CROP_REQUESTCODE = 1003;

    /**
     * 图片选择的结果
     */
    public static final String SELECT_RESULT = "select_result";

    /**
     * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     */
    public static final String EXTRA_MAX_SELECTED_COUNT = "max_selected_count";

    /**
     * 显示列数
     */
    public static final String EXTRA_GRID_COLUMN = "column";

    /**
     * 是否显示拍照
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";

    /**
     * 已经选择的照片集合
     */
    public static final String EXTRA_SELECTED_IMAGES = "selected_images";

    /**
     * 是否单选
     */
    public static final String EXTRA_SINGLE = "single";

    /**
     * 是否裁剪
     */
    public static final String EXTRA_CROP = "is_crop";

    /**
     * toolbar和bottombar是否为material design风格
     */
    public static final String EXTRA_MATERIAL_DESIGN = "material_design";

//    /**
//     * 拍照完成之后是否直接进行裁剪
//     */
//    public static final String EXTRA_CUTAFTERPHOTOGRAPH = "cutAfterPhotograph";

    /**
     * toolBar的颜色值
     */
    public static final String EXTRA_TOOLBARCOLOR = "toolBarColor";

    /**
     * bottomBar的颜色值
     */
    public static final String EXTRA_BOTTOMBARCOLOR = "bottomBarColor";

    /**
     * 状态栏的颜色值
     */
    public static final String EXTRA_STATUSBARCOLOR = "statusBarColor";

    /**
     * 初始位置
     */
    public static final String EXTRA_POSITION = "position";

    /**
     * true是点击预览按钮进入到的预览界面
     * false是点击item进入到的预览界面
     */
    public static final String EXTRA_ISPREVIEW = "isPreview";

    public static final int RESULT_CODE = 0x00000012;

    public static final String IS_CONFIRM = "is_confirm";


    /**
     * 获取裁剪之后的图片的uri
     *
     * @param intent data
     * @return uri
     */
    public static Uri getCropImageUri(@NonNull Intent intent) {
        return UCrop.getOutput(intent);
    }


    public static PhotoSelectorBuilder builder() {
        return new PhotoSelectorBuilder();
    }

    public static class PhotoSelectorBuilder {
        private Bundle mPickerOptionsBundle;
        private Intent mPickerIntent;

        PhotoSelectorBuilder() {
            mPickerOptionsBundle = new Bundle();
            mPickerIntent = new Intent();
        }

        /**
         * Send the Intent from an Activity with a custom request code
         *
         * @param activity    Activity to receive result
         * @param requestCode requestCode for result
         */
        public void start(@NonNull Activity activity, int requestCode) {
            if (PermissionsUtils.checkReadStoragePermission(activity)) {
                activity.startActivityForResult(getIntent(activity), requestCode);
            }
        }

        /**
         * Get Intent to start {@link ImageSelectorActivity}
         *
         * @return Intent for {@link ImageSelectorActivity}
         */
        private Intent getIntent(@NonNull Context context) {
            mPickerIntent.setClass(context, ImageSelectorActivity.class);
            mPickerIntent.putExtras(mPickerOptionsBundle);
            return mPickerIntent;
        }

        /**
         * @param activity Activity to receive result
         */
        public void start(@NonNull Activity activity) {
            start(activity, DEFAULT_REQUEST_CODE);
        }

        /**
         * 设置最大选择数量
         *
         * @param maxSelectCount 数量
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setMaxSelectCount(int maxSelectCount) {
            mPickerOptionsBundle.putInt(EXTRA_MAX_SELECTED_COUNT, maxSelectCount);
            return this;
        }

//        /**
//         * 拍照之后是否科技进行裁剪
//         *
//         * @param cutAfterPhotograph 拍照之后是否科技进行裁剪
//         * @return PhotoSelectorBuilder
//         */
//        public PhotoSelectorBuilder setCutAfterPhotograph(boolean cutAfterPhotograph) {
//            mPickerOptionsBundle.putBoolean(EXTRA_CUTAFTERPHOTOGRAPH, cutAfterPhotograph);
//            return this;
//        }

        /**
         * 是否是单选
         *
         * @param isSingle 是否是单选
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setSingle(boolean isSingle) {
            mPickerOptionsBundle.putBoolean(EXTRA_SINGLE, isSingle);
            return this;
        }

        /**
         * 设置列数
         *
         * @param columnCount 列数
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setGridColumnCount(int columnCount) {
            mPickerOptionsBundle.putInt(EXTRA_GRID_COLUMN, columnCount);
            return this;
        }

        /**
         * 是否显示拍照
         *
         * @param showCamera 是否显示拍照
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setShowCamera(boolean showCamera) {
            mPickerOptionsBundle.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
            return this;
        }

        /**
         * 已经选择的照片集合
         *
         * @param selected 已经选择的照片集合
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setSelected(ArrayList<String> selected) {
            mPickerOptionsBundle.putStringArrayList(EXTRA_SELECTED_IMAGES, selected);
            return this;
        }

        /**
         * toolBar的颜色
         *
         * @param toolBarColor toolBar的颜色
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setToolBarColor(@ColorInt int toolBarColor) {
            mPickerOptionsBundle.putInt(EXTRA_TOOLBARCOLOR, toolBarColor);
            return this;
        }

        /**
         * bottomBar的颜色
         *
         * @param bottomBarColor bottomBar的颜色
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setBottomBarColor(@ColorInt int bottomBarColor) {
            mPickerOptionsBundle.putInt(EXTRA_BOTTOMBARCOLOR, bottomBarColor);
            return this;
        }

        /**
         * 状态栏的颜色
         *
         * @param statusBarColor 状态栏的颜色
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setStatusBarColor(@ColorInt int statusBarColor) {
            mPickerOptionsBundle.putInt(EXTRA_STATUSBARCOLOR, statusBarColor);
            return this;
        }

        /**
         * oolbar和bototmbar是否显示materialDesign风格
         *
         * @param materialDesign toolbar和bototmbar是否显示materialDesign风格
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setMaterialDesign(boolean materialDesign) {
            mPickerOptionsBundle.putBoolean(EXTRA_MATERIAL_DESIGN, materialDesign);
            return this;
        }

        /**
         * 是否裁剪，剪切，修剪
         *
         * @return PhotoSelectorBuilder
         */
        public PhotoSelectorBuilder setCrop(boolean isCrop) {
            mPickerOptionsBundle.putBoolean(EXTRA_CROP, isCrop);
            return this;
        }
    }
}
