package com.zjee;

import cn.leancloud.AVLogger;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.core.AVOSCloud;
import com.zjee.service.WeatherService;
import com.zjee.service.WebPicService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

/**
 * Date: 2019-05-29 13:25
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

public class NoSpringTest {

    private WeatherService weatherService = new WeatherService();

    private WebPicService webPicService = new WebPicService();

    @Test
    public void apiTest() throws Exception {
        System.out.println(weatherService.getForecastBywoeId(2151330));
    }

    @Test
    public void imageUrlTest() throws Exception {
        webPicService.batchGetPhotoUrl("内华达", LocalDate.now().toString()).forEach(System.out::println);
    }

    @Test
    public void LeanCloudTest() throws Exception {
        AVOSCloud.initialize("hiuyEvHNGJv0HBzIgke1FOa2-MdYXbMMI", "W2WW9ah2D4zfyhW3aYtKw3xD");
        AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        AVQuery<AVObject> query = new AVQuery<>("zjeeAccount");
        query.whereEqualTo("user_name", "zhongjie");
        query.findInBackground().blockingSubscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> students) {
                System.out.println(students);
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
}
