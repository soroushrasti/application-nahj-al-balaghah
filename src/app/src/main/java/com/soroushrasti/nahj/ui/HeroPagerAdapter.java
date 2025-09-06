package com.soroushrasti.nahj.ui;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.soroushrasti.nahj.R;

import java.util.List;

public class HeroPagerAdapter extends RecyclerView.Adapter<HeroPagerAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final Context context;
    private final List<String> data;
    private final OnItemClickListener listener;

    public HeroPagerAdapter(Context context, List<String> data) {
        this(context, data, null);
    }

    public HeroPagerAdapter(Context context, List<String> data, OnItemClickListener listener) {
        this.context = context.getApplicationContext();
        this.data = data;
        this.listener = listener;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
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
            Glide.with(iv)
                    .load(R.drawable.header_banner)
                    .placeholder(R.drawable.header_banner)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.header_banner)
                    .into(iv);
        } else {
            // Load from assets via file:///android_asset/
            Uri uri = Uri.parse("file:///android_asset/images/" + item);
            Glide.with(iv)
                    .load(uri)
                    .placeholder(R.drawable.header_banner)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .error(R.drawable.header_banner)
                    .into(iv);
        }

        if (listener != null) {
            iv.setOnClickListener(v -> listener.onItemClick(holder.getBindingAdapterPosition()));
        } else {
            iv.setOnClickListener(null);
        }
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
