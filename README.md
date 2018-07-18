# PhotoSelector
Android图片选择器，仿微信的图片选择器的样式和效果。可横竖屏切换显示,
自定义配置，单选，多选，是否显示拍照，material design风格，单选裁剪，拍照裁剪，滑动翻页预览，双击放大，缩放

#kotlin版 
[kotlin版](https://github.com/wj576038874/PhotoSlectorKotlin)

效果图： [点击下载APK进行体验](https://raw.githubusercontent.com/wj576038874/PhotoSelectorDemo/master/apk/PhotoSelector.apk)

![相册](https://github.com/wj576038874/PhotoSelectorDemo/blob/master/images/selector.jpg)
![文件夹](https://github.com/wj576038874/PhotoSelectorDemo/blob/master/images/folder.jpg) 
![预览](https://github.com/wj576038874/PhotoSelectorDemo/blob/master/images/preview.jpg)
![预览列表](https://github.com/wj576038874/PhotoSelectorDemo/blob/master/images/preview_list.jpg) 
![裁剪](https://github.com/wj576038874/PhotoSelectorDemo/blob/master/images/clip.jpg)

**1、引入依赖**

在工程的build.gradle添加如下配置
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

在Module的build.gradle在添加以下代码

```
implementation 'com.winfo.photoselector:PhotoSelector:1.2.1'
```


**2、说明**

PhotoSelector的图片加载是基于glide4.7.1实现的，可以自定义加载动画，预览照片使用
**com.github.chrisbanes:PhotoView:2.1.3**
裁剪使用的是**com.github.yalantis:ucrop:2.2.2**等开源库，列表加载，翻页预览这里没有使用viewpager使用的是recycleview


**3、使用**

在你的项目中的AndroidManifest文件中添加以下配置
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<activity
    android:name="com.yalantis.ucrop.UCropActivity"
    android:screenOrientation="portrait"
    android:theme="@style/AppTheme" />
<activity
    android:name="com.winfo.photoselector.ImageSelectorActivity"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:theme="@style/AppTheme" />
<activity
    android:name="com.winfo.photoselector.PreviewActivity"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:theme="@style/AppTheme" />

<activity
    android:name="com.winfo.photoselector.RvPreviewActivity"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:theme="@style/AppTheme" />
```

**3、调起图片选择器**

调用的是很简单，只需要一句代码，其他可选择性配置
```java
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
        
 //裁剪
 //单选后剪裁 裁剪的话都是针对一张图片所以要设置setSingle(true)
PhotoSelector.builder()
       .setSingle(true)//单选，裁剪都是单选
       .setCrop(true)//是否裁剪
       .setCropMode(PhotoSelector.CROP_RECTANG)//设置裁剪模式 矩形还是圆形
       .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
       .setToolBarColor(ContextCompat.getColor(this, R.color.colorAccent))
       .setBottomBarColor(ContextCompat.getColor(this, R.color.colorAccent))
       .setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent))
       .start(MainActivity.this, CROP_CODE);

 //多选(不限数量)
PhotoSelector.builder()
       .setMaxSelectCount(-1)//-1不限制数量
       .setSelected(images)
       .start(MainActivity.this, UN_LIMITT_CODE);

 //单选
PhotoSelector.builder()
       .setSingle(true)
       .start(MainActivity.this, SINGLE_CODE);
```
REQUEST_CODE就是调用者自己定义的启动Activity时的requestCode，这个相信大家都能明白。selected可以在再次打开选择器时，把原来已经选择过的图片传入，使这些图片默认为选中状态。

**4、接收选择器返回的数据**

在Activity的onActivityResult方法中接收选择器返回的数据。
```java
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
                //获取到裁剪后的图片的Uri进行处理
                Uri resultUri = PhotoSelector.getCropImageUri(data);
                Glide.with(this).load(resultUri).into(imageView);
                break;
            case UN_LIMITT_CODE:
                images = data.getStringArrayListExtra(PhotoSelector.SELECT_RESULT);
                mAdapter.refresh(images);
                break;
        }
    }
}
```
