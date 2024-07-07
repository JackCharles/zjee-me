package com.zjee.common.model;

import lombok.Data;

/**
 * @author ZhongJie
 * @date 17:54
 * @desc
 */

@Data
public class TrafficStatModel {
    private String date;

    private long inBound;

    private long outBound;

    private long totalInBound;

    private long totalOutBound;
}
