package com.zjee.service;

import com.zjee.dal.TaskMapper;
import com.zjee.pojo.TaskInfo;
import com.zjee.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class TaskService implements InitializingBean {

    @Value("${max-task-size}")
    private int maxTaskSize;

    private TaskManager taskManager;

    @Autowired
    private TaskMapper taskMapper;

    private final static String STDOUT = "stdout";

    @Override
    public void afterPropertiesSet() throws Exception {
        taskManager = new TaskManager(maxTaskSize, taskMapper);
    }

    public TaskInfo submitTask(TaskInfo taskInfo) {
        return taskManager.submitTask(taskInfo);
    }

    public List<TaskInfo> getAllTask(int pageNo, int pageSize) {
        return taskManager.getAllTask(pageNo, pageSize);
    }

    public String getLog(String taskId, int pageNo, int pageSize) {
        String log = "";
        String stdOutLog = taskManager.getTaskStdOut(taskId, pageNo, pageSize);
        String stdErrLog = taskManager.getTaskStdErr(taskId, pageNo, pageSize);

        if(!StringUtils.isEmpty(stdOutLog)) {
            log += stdOutLog + "\n";
        }

        if(!StringUtils.isEmpty(stdErrLog)) {
            log += stdErrLog + "\n";
        }

        return log;
    }

    public void stopTask(String taskId) {
        taskManager.stopTask(taskId);
    }
}
