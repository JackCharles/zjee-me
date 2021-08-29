package com.zjee.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ZhongJie
 * @date 17:54
 * @desc
 */

@Data
public class BwStat {
    private LocalDateTime dt;
    private Long usageToday;
    private Long usageTotal;
    private Long capacity;
}
