package com.zjee.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfo {

    private String taskId;

    private String cmd;

    private int timeOut;

    private String startTime;

    private String endTime;

    private int exitStatus;

    private String desc;

    private int pageSize;

    private int pageNo;
}
