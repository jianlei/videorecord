package com.daren.videorecord;

import android.content.Context;
import android.os.Environment;

import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.util.DeviceUtils;

import java.io.File;

/**
 * 初始化video
 *
 * Created by daren on 16/6/29.
 */
public class VideoInit {

    /**
     * 初始化秒拍sdk，以及默认的视频存储目录，在／sdcard/DCIM/Camera/Daren_Video/下
     * @param context
     */
    public static void init(Context context){
        // 设置拍摄视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                VCamera.setVideoCachePath(dcim + "/Camera/Daren_Video/");
            } else {
                VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/Daren_Video/");
            }
        } else {
            VCamera.setVideoCachePath(dcim + "/Camera/Daren_Video/");
        }
        // 开启log输出,ffmpeg输出到logcat
        VCamera.setDebugMode(true);
        // 初始化拍摄SDK，必须
        VCamera.initialize(context);
    }
}
