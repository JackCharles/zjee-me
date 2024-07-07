package com.zjee.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author ZhongJie
 * @date 15:23
 * @desc
 */
@Data
@Builder
public class PvStatModel {
    private List<String> dateList;

    private List<Long> pvList;
}
