package com.zjee.demo.service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class DownloadService {

    public File getFileFromUri(String uri){
        if(uri == null)
            return null;
        return new File(".." + uri);
    }

    public Map<String, String> getFileList(File file){
        if(file == null)
            return Collections.emptyMap();
        Map<String, String> fileList = new HashMap<>();
        for (File item : file.listFiles()) {
            if (item.isFile())
                fileList.put(item.getName(), "文件");
            else if (item.isDirectory())
                fileList.put(item.getName(), "目录");
        }
        return fileList;
    }
}
