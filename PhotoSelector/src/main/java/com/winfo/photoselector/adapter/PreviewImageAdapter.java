package com.winfo.photoselector.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

/**
 * ProjectName：ImageSelector-master
 * PackageName：com.donkingliang.imageselector.adapter
 * Author：wenjie
 * Date：2018-06-14 18:31
 * Description：
 */
public class PreviewImageAdapter extends RecyclerView.Adapter<PreviewImageAdapter.ImageHolder> {

    private Context mContext;
    private List<Image> mImgList;

    public interface OnItemClcikLitener {
        void OnItemClcik(PreviewImageAdapter previewImageAdapter, View iteView, int position);
    }

    public OnItemClcikLitener onItemClcikLitener;

    public void setOnItemClcikLitener(OnItemClcikLitener onItemClcikLitener) {
        this.onItemClcikLitener = onItemClcikLitener;
    }

    public PreviewImageAdapter(Context mContext, List<Image> mImgList) {
        this.mContext = mContext;
        this.mImgList = mImgList;
    }

    public List<Image> getData() {
        return mImgList;
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ImageHolder imageHolder = new ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.preview_item, parent, false));
        imageHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (onItemClcikLitener != null){
                        onItemClcikLitener.OnItemClcik(PreviewImageAdapter.this , imageHolder.itemView , imageHolder.getLayoutPosition());
                    }
            }
        });
        return imageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        String path = mImgList.get(position).getPath();
        Uri uri;
        if (path.startsWith("http")) {
            uri = Uri.parse(path);
        } else {
            uri = Uri.fromFile(new File(path));
        }
        Glide.with(mContext).setDefaultRequestOptions(new RequestOptions()
                .dontTransform()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_img_load_fail)
                .override(800, 1200))
                .load(uri)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mImgList.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_itemimg);
        }
    }
}
