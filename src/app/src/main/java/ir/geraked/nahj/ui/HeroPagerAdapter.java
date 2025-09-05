package com.soroushrasti.nahj.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soroushrasti.nahj.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Simple pager adapter that shows images either from app resources (res/) or from assets/images.
 * Each item in data is either:
 *  - "res:header_banner" style resource key (resolved to R.drawable.header_banner)
 *  - or a plain asset filename under assets/images (e.g., "my_pic.jpg").
 */
public class HeroPagerAdapter extends RecyclerView.Adapter<HeroPagerAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final Context context;
    private final List<String> data;
    private final OnItemClickListener listener;
    private final int targetW;
    private final int targetH;

    public HeroPagerAdapter(Context context, List<String> data) {
        this(context, data, null);
    }

    public HeroPagerAdapter(Context context, List<String> data, OnItemClickListener listener) {
        this.context = context.getApplicationContext();
        this.data = data;
        this.listener = listener;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        // Aim for screen width and ~screen height for safe decode
        this.targetW = dm.widthPixels;
        this.targetH = dm.heightPixels;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hero_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String item = data.get(position);
        ImageView iv = holder.imageView;
        if (item.startsWith("res:")) {
            iv.setImageResource(R.drawable.header_banner);
        } else {
            AssetManager am = context.getAssets();
            try (InputStream boundsIs = am.open("images/" + item)) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(boundsIs, null, opts);
                opts.inSampleSize = calculateInSampleSize(opts, targetW, targetH);
                opts.inJustDecodeBounds = false;
                try (InputStream is = am.open("images/" + item)) {
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    if (bmp != null) {
                        iv.setImageBitmap(bmp);
                    } else {
                        iv.setImageResource(R.drawable.header_banner);
                    }
                }
            } catch (IOException e) {
                iv.setImageResource(R.drawable.header_banner);
            }
        }
        if (listener != null) {
            iv.setOnClickListener(v -> listener.onItemClick(holder.getBindingAdapterPosition()));
        } else {
            iv.setOnClickListener(null);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_hero);
        }
    }
}
