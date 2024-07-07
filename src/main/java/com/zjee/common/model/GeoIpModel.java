package com.zjee.common.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author ZhongJie
 * @date 15:37
 * @desc
 */
@Data
@Builder
public class GeoIpModel {
    private String ip;

    private String location;

    private Long visitCount;
}
