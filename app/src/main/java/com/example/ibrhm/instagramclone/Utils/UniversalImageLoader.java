package com.example.ibrhm.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ibrhm.instagramclone.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoader {

    private Context mContext;

    public UniversalImageLoader(Context context){
        mContext = context;
    }

    public ImageLoaderConfiguration getConfig() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.color.colorLightGray)
                .showImageForEmptyUri(R.drawable.ic_default_avatar)
                .showImageOnFail(R.color.colorLightGray)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(400))
                .build();

        return new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100*1024*1024)
                .build();
    }

    public static void setImage(String imgUrl, ImageView imageView, final ProgressBar progressBar, String title){

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(title + imgUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progressBar != null){
                    progressBar.setIndeterminate(true);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(progressBar != null){
                    progressBar.setIndeterminate(false);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(progressBar != null){
                    progressBar.setIndeterminate(false);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(progressBar != null){
                    progressBar.setIndeterminate(false);
                }
            }
        });

    }
}
