package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.ExternalStorageStats;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;

import com.example.ibrhm.instagramclone.Share.ShareToActivity;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FileOperations {

    public static ArrayList<String> getFolderFile(String path){
        ArrayList<String> allFiles = new ArrayList<>();

        File file = new File(path);
        File[] folderAllFiles = file.listFiles();

        if(folderAllFiles != null){

            if(folderAllFiles.length > 1){
                Arrays.sort(folderAllFiles, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.lastModified() > o2.lastModified() ? -1 : 1;
                    }
                });
            }

            for (File folderAllFile : folderAllFiles) {
                if (folderAllFile.isFile()) {

                    String readFilePath = folderAllFile.getAbsolutePath();

                    int count = readFilePath.lastIndexOf(".");

                    if (count > 0) {
                        String fileType = readFilePath.substring(readFilePath.lastIndexOf("."));

                        if (fileType.equals(".jpg") || fileType.equals(".jpeg") || fileType.equals(".png") || fileType.equals(".mp4") || fileType.equals(".3gp")) {
                            allFiles.add(readFilePath);
                        }
                    }
                }
            }
        }

        return allFiles;
    }

    public static void compressToPhoto(Activity activity, String filePath){
        new PhotoCompressAsyncTask(activity).execute(filePath);
    }

    public static void compressToVideo(Activity activity, String filePath){
        new VideoCompressAsyncTask(activity).execute(filePath);
    }

    public static boolean doYouHaveFile(String path){
        File file = new File(path);

        File[] fileList = file.listFiles();

        return fileList != null && fileList.length > 0;
    }

    public static ArrayList<String> getAllGallery(Context context){
        ArrayList<String> galleyImageUrls;
        String[] columns = (String[]) Arrays.asList(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID).toArray();
        String orderBy = MediaStore.Images.Media.DATE_TAKEN;

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, orderBy + " DESC"
        );

        galleyImageUrls = new ArrayList<>();

        for(int i = 0; i < imageCursor.getCount(); i++){
            imageCursor.moveToPosition(i);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            galleyImageUrls.add((imageCursor.getString(dataColumnIndex)));
        }

        return galleyImageUrls;
    }

    public static String cropImage(Bitmap bitmap){
        Date d = new Date();
        CharSequence s  = DateFormat.format("yyyyMMdd", d.getTime());
        String newImageName = "IMG_"+s+"_"+System.currentTimeMillis()+".jpg";

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

        String ExternalStorageDirectory = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Instagram Clone";
        File fileInfo = new File(ExternalStorageDirectory);
        File file = new File(ExternalStorageDirectory + File.separator, newImageName);

        FileOutputStream fileOutputStream = null;
        try {
            if(fileInfo.isDirectory() || fileInfo.mkdirs()){
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes.toByteArray());
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                    return file.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static class PhotoCompressAsyncTask extends AsyncTask<String,String,String> {

        @SuppressLint("StaticFieldLeak")
        private Activity activity;
        private AlertDialog dialog;

        PhotoCompressAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {

            dialog = DialogHelper.dialogLoading(activity, "Compressing...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Instagram Clone");
            String newPath = SiliCompressor.with(activity.getApplicationContext()).compress(strings[0], newFile);

            return newPath;
        }

        @Override
        protected void onPostExecute(String filePath) {
            dialog.dismiss();
            ((ShareToActivity) activity).uploadPhotoPost(filePath);
            super.onPostExecute(filePath);
        }
    }

    public static class VideoCompressAsyncTask extends AsyncTask<String,String,String>{

        @SuppressLint("StaticFieldLeak")
        private Activity activity;
        private AlertDialog dialog;

        VideoCompressAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            dialog = DialogHelper.dialogLoading(activity, "Compressing...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Instagram Clone/Video");

            if(newFile.isDirectory() || newFile.mkdirs()){
                String newPath = null;
                try {
                    newPath = SiliCompressor.with(activity.getApplicationContext()).compressVideo(strings[0],newFile.getPath());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return newPath;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String filePath) {

            if(!filePath.isEmpty()){
                dialog.dismiss();
                ((ShareToActivity) activity).uploadVideoPost(filePath);
            }

            super.onPostExecute(filePath);
        }
    }
}
