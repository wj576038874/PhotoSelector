package com.winfo.photoselector.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.winfo.photoselector.R;
import com.winfo.photoselector.entity.Image;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<Image> mImages;
    private LayoutInflater mInflater;
    private View.OnClickListener onCameraClickListener = null;
    private final static int ITEM_TYPE_CAMERA = 100;
    private final static int ITEM_TYPE_PHOTO = 101;
    //是否是显示全部图片  只有显示全部图片的时候 才会去显示牌照，否则不显示牌拍照
    private boolean showCamera;

    //保存选中的图片
    private ArrayList<Image> mSelectImages = new ArrayList<>();
    private OnImageSelectListener mSelectListener;
    private OnItemClickListener mItemClickListener;
    private int mMaxCount;
    private boolean isSingle;

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return ITEM_TYPE_CAMERA;
        } else {
            return ITEM_TYPE_PHOTO;
        }
    }

    /**
     * @param maxCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param isSingle 是否单选
     */
    public ImageAdapter(Context context, int maxCount, boolean isSingle) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        mMaxCount = maxCount;
        this.isSingle = isSingle;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CAMERA) {
            CameraHolder cameraHolder = new CameraHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false));
            cameraHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(v);
                    }
                }
            });
            return cameraHolder;
        } else {
            return new ImageHolder(mInflater.inflate(R.layout.adapter_images_item, parent, false));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //如果是照片则加载照片显示  否则的话就是拍照就不用处理，默认用布局显示样式
        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {
            final ImageHolder imageHolder = (ImageHolder) holder;
            final Image image;
            //如果是显示拍照
            if (showCamera) {
                image = mImages.get(position - 1);
                image.setPosition(position - 1);
            } else {
                image = mImages.get(position);
                image.setPosition(position);
            }
            Glide.with(mContext).load(image.getPath())
//                    .transition(new GenericTransitionOptions<>().transition(R.anim.glide_anim))
                    .transition(new GenericTransitionOptions<>().transition(android.R.anim.slide_in_left))
                    .transition(new DrawableTransitionOptions().crossFade(150))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .placeholder(R.drawable.ic_image).error(R.drawable.ic_img_load_fail))
                    .thumbnail(0.5f)
                    .into(imageHolder.ivImage);

            setItemSelect(imageHolder, mSelectImages.contains(image));
            //点击选中/取消选中图片
            imageHolder.ivSelectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(mContext , image.getPosition()+"" ,Toast.LENGTH_SHORT).show();
                    if (mSelectImages.contains(image)) {
                        //如果图片已经选中，就取消选中
                        unSelectImage(image);
                        setItemSelect(imageHolder, false);
                    } else if (isSingle) {
                        //如果是单选，就先清空已经选中的图片，再选中当前图片
                        clearImageSelect();
                        selectImage(image);
                        setItemSelect(imageHolder, true);
                    } else if (mMaxCount <= 0 || mSelectImages.size() < mMaxCount) {
                        //如果不限制图片的选中数量，或者图片的选中数量
                        // 还没有达到最大限制，就直接选中当前图片。
                        selectImage(image);
                        setItemSelect(imageHolder, true);
                    } else if (mSelectImages.size() == mMaxCount) {
                        Toast.makeText(mContext, "最多只能选" + mMaxCount + "张", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        //如果是显示拍照
                        if (showCamera) {
                            mItemClickListener.OnItemClick(image, imageHolder.itemView, imageHolder.getAdapterPosition() - 1);
                        } else {
                            mItemClickListener.OnItemClick(image, imageHolder.itemView, imageHolder.getAdapterPosition());
                        }
                    }
                }
            });
        }
    }

    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }

    /**
     * 选中图片
     *
     * @param image image
     */
    private void selectImage(Image image) {
//        image.setSelectPosition(mSelectImages.size());
        mSelectImages.add(image);
        if (mSelectListener != null) {
            mSelectListener.OnImageSelect(image, true, mSelectImages.size());
        }
    }

    /**
     * 取消选中图片
     *
     * @param image image
     */
    private void unSelectImage(Image image) {
        mSelectImages.remove(image);
        if (mSelectListener != null) {
            mSelectListener.OnImageSelect(image, false, mSelectImages.size());
        }
    }

    @Override
    public int getItemCount() {
        if (showCamera) {
            return mImages == null ? 0 : mImages.size() + 1;
        } else {
            return mImages == null ? 0 : mImages.size();
        }
    }

    public ArrayList<Image> getData() {
        return mImages;
    }

    /**
     * 刷新数据
     *
     * @param data       data
     * @param showCamera 是否显示拍照功能
     */
    public void refresh(ArrayList<Image> data, boolean showCamera) {
        this.showCamera = showCamera;
        mImages = data;
        notifyDataSetChanged();
    }


    /**
     * 设置图片选中和未选中的效果
     */
    private void setItemSelect(ImageHolder holder, boolean isSelect) {
        if (isSelect) {
            holder.ivSelectIcon.setImageResource(R.drawable.ic_image_select);
            holder.ivMasking.setAlpha(0.5f);
        } else {
            holder.ivSelectIcon.setImageResource(R.drawable.ic_image_un_select);
            holder.ivMasking.setAlpha(0.2f);
        }
    }

    private void clearImageSelect() {
        mSelectImages.clear();
        notifyDataSetChanged();
//        if (mImages != null && mSelectImages.size() == 1) {
//            int index = mImages.indexOf(mSelectImages.get(0));
//            if (index != -1) {
//                mSelectImages.clear();
//                notifyItemChanged(index);
//                notifyDataSetChanged();
//            }
//        }
    }

    public void setSelectedImages(ArrayList<String> selected) {
        mSelectImages.clear();
        if (mImages != null && selected != null) {
            for (String path : selected) {
                if (isFull()) {
                    return;
                }
                for (Image image : mImages) {
                    if (path.equals(image.getPath())) {
                        if (!mSelectImages.contains(image)) {
                            mSelectImages.add(image);
                        }
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        }
    }


    private boolean isFull() {
        return isSingle && mSelectImages.size() == 1 || mMaxCount > 0 && mSelectImages.size() == mMaxCount;
    }

    public ArrayList<Image> getSelectImages() {
        return mSelectImages;
    }

    public void setOnImageSelectListener(OnImageSelectListener listener) {
        this.mSelectListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    class ImageHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        ImageView ivSelectIcon;
        ImageView ivMasking;

        ImageHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivSelectIcon = itemView.findViewById(R.id.iv_select);
            ivMasking = itemView.findViewById(R.id.iv_masking);
        }
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        ImageView ivCamera;

        CameraHolder(View itemView) {
            super(itemView);
            ivCamera = itemView.findViewById(R.id.iv_camera);
        }
    }

    public interface OnImageSelectListener {
        void OnImageSelect(Image image, boolean isSelect, int selectCount);
    }

    public interface OnItemClickListener {
        void OnItemClick(Image image, View iteView, int position);
    }
}
