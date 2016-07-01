package com.daren.videorecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daren on 16/6/29.
 */
public class VideoDownloadUtils {
    public static String getFilename(String filepath) {
        if (filepath == null)
            return null;

        final String[] filepathParts = filepath.split("/");

        return filepathParts[filepathParts.length - 1];
    }


    public static void downloadVideo(final String remoteUrl, final String localPath, final DownloadCallback callback) {
        new Thread() {
            @Override
            public void run() {
                //下载到的本地路径
                InputStream in = null;
                FileOutputStream out = null;
                try {
                    URL url = new URL(remoteUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("Charset", "UTF-8");
                    urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

                    urlConnection.connect();
                    long bytetotal = urlConnection.getContentLength();
                    long bytesum = 0;
                    int byteread = 0;
                    in = urlConnection.getInputStream();
//			File dir = StorageUtils.getCacheDirectory(this);
//			String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
                    String key = String.valueOf(System.currentTimeMillis());

                    File apkFile = new File(localPath);
                    out = new FileOutputStream(apkFile);
                    byte[] buffer = new byte[2048];

                    int oldProgress = 0;

                    while ((byteread = in.read(buffer)) != -1) {
                        bytesum += byteread;
                        out.write(buffer, 0, byteread);

                        int progress = (int) (bytesum * 100L / bytetotal);
                        callback.onProgress(progress);
                        oldProgress = progress;
                    }
                    callback.onComplete(localPath);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError();

                }
            }
        }.start();

    }


    public static interface DownloadCallback {
        void onProgress(int progress);

        void onComplete(String localPath);

        void onError();

    }
}
