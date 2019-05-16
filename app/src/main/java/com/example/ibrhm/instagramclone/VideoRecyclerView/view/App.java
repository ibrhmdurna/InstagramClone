package com.example.ibrhm.instagramclone.VideoRecyclerView.view;

import android.app.Application;
import android.content.Context;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.ibrhm.instagramclone.Background.Background;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.one.EmojiOneProvider;

/**
 * Created by HoangAnhTuan on 1/21/2018.
 */

public class App extends Application {
    private HttpProxyCacheServer proxy;

    public static Background background = new Background();
    public static Background.Profile backgroundProfile = new Background.Profile();
    public static Background.News backgroundNews = new Background.News();

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 5)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);

        EmojiManager.install(new EmojiOneProvider());

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
