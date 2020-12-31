package com.zjee.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
