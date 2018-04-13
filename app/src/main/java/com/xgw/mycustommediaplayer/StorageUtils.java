package com.xgw.mycustommediaplayer;


import com.xgw.mybaselib.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XieGuangwei on 2017/12/24.
 * 存储相关工具类
 */

public class StorageUtils {
    /**
     * 获取那个目录下的文件路径列表
     *
     * @param dir
     * @return
     */
    public static List<String> getFilePaths(String dir) {
        ArrayList<String> fileList = new ArrayList<String>();
        File extStorage = new File(dir);
        if (!extStorage.exists()) {
            return new ArrayList<>();
        }
        File[] files = extStorage.listFiles();
        //获取路径
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileList.add(files[i].getAbsolutePath());
            }
        }
        return fileList;
    }

    public static List<String> getNames(String dir) {
        List<String> urls = getFilePaths(dir);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            if (urls.get(i).endsWith(".mp4")) {
                names.add(FileUtils.getFileNameNoExtension(urls.get(i)));
            }
        }
        return names;
    }
}
