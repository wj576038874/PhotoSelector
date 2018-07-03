package com.winfo.photoselector.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.winfo.photoselector.R;
import com.winfo.photoselector.entity.Image;

import java.io.File;
import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;
    //    private List<PhotoView> viewList = new ArrayList<>(4);
    private List<Image> mImgList;
    private OnItemClickListener mListener;

    public ImagePagerAdapter(Context context, List<Image> imgList) {
        this.mContext = context;
//        createImageViews();
        mImgList = imgList;
    }

//    private void createImageViews() {
//        for (int i = 0; i < 4; i++) {
//            PhotoView imageView = new PhotoView(mContext);
//            imageView.setAdjustViewBounds(true);
//            viewList.add(imageView);
//        }
//    }

    @Override
    public int getCount() {
        return mImgList == null ? 0 : mImgList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        if (object instanceof PhotoView) {
//            PhotoView view = (PhotoView) object;
//            view.setImageDrawable(null);
//            viewList.add(view);
//            container.removeView(view);
//        }
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_view_pager, container, false);
        final ImageView imageView = itemView.findViewById(R.id.iv_pager);

        final String path = mImgList.get(position).getPath();

        final Uri uri;
        if (path.startsWith("http")) {
            uri = Uri.parse(path);
        } else {
            uri = Uri.fromFile(new File(path));
        }
        final Image image = mImgList.get(position);
        Glide.with(mContext).setDefaultRequestOptions(new RequestOptions()
                .dontTransform()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_img_load_fail)
                .override(800, 1200))
                .load(uri)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position, image);
                }
            }
        });
        container.addView(itemView);

//        final PhotoView currentView = viewList.remove(0);
//        final Image image = mImgList.get(position);
//        container.addView(currentView);
//        Glide.with(mContext).asBitmap()
//                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                .load(new File(image.getPath())).into(new SimpleTarget<Bitmap>() {
//            @Override
//            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                currentView.setImageBitmap(resource);
////                int bw = resource.getWidth();
////                int bh = resource.getHeight();
////                if (bw > 8192 || bh > 8192) {
////                    Bitmap bitmap = ImageUtil.zoomBitmap(resource, 8192, 8192);
////                    setBitmap(currentView, bitmap);
////                } else {
////                    setBitmap(currentView, resource);
////                }
//            }
//        });
//        currentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListener != null) {
//                    mListener.onItemClick(position, image);
//                }
//            }
//        });
        return itemView;
    }

//    private void setBitmap(PhotoView imageView, Bitmap bitmap) {
//        imageView.setImageBitmap(bitmap);
//        if (bitmap != null) {
//            int bw = bitmap.getWidth();
//            int bh = bitmap.getHeight();
//            int vw = imageView.getWidth();
//            int vh = imageView.getHeight();
//            if (bw != 0 && bh != 0 && vw != 0 && vh != 0) {
//                if (1.0f * bh / bw > 1.0f * vh / vw) {
//                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    float offset = (1.0f * bh * vw / bw - vh) / 2;
//                    adjustOffset(imageView, offset);
//                } else {
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                }
//            }
//        }
//    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Image image);
    }

//    private void adjustOffset(PhotoView view, float offset) {
//        PhotoViewAttacher attacher = view.getAttacher();
//        try {
//            Field field = PhotoViewAttacher.class.getDeclaredField("mBaseMatrix");
//            field.setAccessible(true);
//            Matrix matrix = (Matrix) field.get(attacher);
//            matrix.postTranslate(0, offset);
//            Method method = PhotoViewAttacher.class.getDeclaredMethod("resetMatrix");
//            method.setAccessible(true);
//            method.invoke(attacher);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
