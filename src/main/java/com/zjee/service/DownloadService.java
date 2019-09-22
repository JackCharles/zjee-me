package com.zjee.service;

import com.zjee.service.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

@Component
@Slf4j
public class DownloadService {

    public File getFileFromUri(String uri, String prefix) {
        if (StringUtils.isEmpty(uri))
            return null;
        return new File(uri.replaceAll(prefix, ""));
    }

    public List<Map<String, String>> getFileList(File file) {
        if (file == null) {
            return Collections.emptyList();
        }
        List<Map<String, String>> fileList = new ArrayList<>();
        Tika tika = new Tika();// detect file type
        for (File item : file.listFiles()) {
            Map<String, String> map = new HashMap<>();
            map.put("key", item.getName());
            map.put("name", item.getName());
            map.put("size", item.isDirectory() ? "-" : CommonUtil.formatByteUnit(item.length()));
            if (item.isDirectory()) {
                map.put("type", "directory");
            } else {
                try {
                    map.put("type", tika.detect(item));
                } catch (Exception e) {
                    log.error("detect file type error: ", e);
                    map.put("type", "unknown");
                }
            }
            fileList.add(map);
        }
        return fileList;
    }
}
