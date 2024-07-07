package com.zjee.service;

import com.zjee.dal.TaskMapper;
import com.zjee.common.model.TaskInfo;
import com.zjee.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TaskService implements InitializingBean {

    @Value("${max-task-size}")
    private int maxTaskSize;

    private TaskManager taskManager;

    @Autowired
    private TaskMapper taskMapper;

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

    public Map<String, String> getLog(String taskId, boolean hisLog) {
        String log = hisLog ? taskManager.getHistoryLog(taskId) : taskManager.getTaskLog(taskId);
        Map<String, String> map = new HashMap<>();
        map.put("log", log);
        String status = "STOPPED";
        if(taskManager.getAliveTask(taskId) != null) {
            status = "RUNNING";
        }
        map.put("status", status);
        return map;
    }

    public void stopTask(String taskId) {
        taskManager.stopTask(taskId);
    }
}
