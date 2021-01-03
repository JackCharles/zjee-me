package com.zjee.task;

import com.zjee.dal.TaskMapper;
import com.zjee.pojo.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TaskManager {

    private final ConcurrentHashMap<String, CmdRunner> container;

    private final int maxTaskCount;

    private final TaskMapper taskMapper;

    private final Map<String, String> restLogCache;

    private static final String LOG_SUFFIX = ".log";

    private static final String LOG_BASE_DIR = "logs/task/";

    private static final String LOG_END_MARK = "====== TASK END ======";

    public TaskManager(int maxTaskCount, TaskMapper taskMapper) {
        if (maxTaskCount < 0) {
            throw new RuntimeException("Illegal maxTaskCount: " + maxTaskCount);
        }
        this.taskMapper = taskMapper;
        this.maxTaskCount = maxTaskCount;
        container = new ConcurrentHashMap<>(maxTaskCount);
        restLogCache = new ConcurrentHashMap<>();
    }

    public TaskInfo submitTask(TaskInfo taskInfo) {
        synchronized (this) {
            if (container.size() >= maxTaskCount) {
                clean();
                if (container.size() >= maxTaskCount) {
                    throw new RuntimeException("The task pool is full, total: " +
                        maxTaskCount + ", current: " + container.size());
                }
            }
            try {
                CmdRunner runner = new CmdRunner(taskInfo, this);
                container.put(runner.getTaskInfo().getTaskId(), runner);
                taskMapper.addTask(taskInfo);
                runner.run(taskInfo.getTimeOut());
                taskMapper.updateTask(taskInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return taskInfo;
    }

    public List<TaskInfo> getAllTask(int pageNo, int pageSize) {
        if (pageNo < 0 || pageSize < 0) {
            throw new RuntimeException("Illegal page number or size");
        }
        List<TaskInfo> allTask = taskMapper.getAllTask(pageSize, pageNo * pageSize);
        allTask.forEach(t -> {
            if(!container.containsKey(t.getTaskId()) &&
                t.getExitStatus() == CmdRunner.RUNNING_STATUS) {
                t.setExitStatus(1);
            }
        });
        return allTask;
    }

    public void taskFinish(CmdRunner runner, Throwable e) {
        if (null == runner || runner.isAlive()) {
            return;
        }
        TaskInfo taskInfo = runner.getTaskInfo();

        try {
            // save rest output
            StringBuilder resLogBuilder = new StringBuilder();
            String restLog = runner.readOutput();
            appendLogFile(taskInfo.getTaskId(), restLog);
            resLogBuilder.append(restLog);

            if (null != e) {
                log.error("ERROR: ", e);
                resLogBuilder.append("====== INTERNAL ERROR ======\n");
                resLogBuilder.append(e.toString()).append("\n");
            }
            resLogBuilder.append(LOG_END_MARK);
            restLogCache.put(taskInfo.getTaskId(), resLogBuilder.toString());

            runner.getOutputReader().close();
        } catch (Exception ex) {
            log.error("ERROR: ", ex);
        }

        // WriteDB
        taskMapper.updateTask(taskInfo);
        container.remove(taskInfo.getTaskId());
    }

    public String getTaskLog(String taskId) {
        Map<String, Object> map = new HashMap<>();
        CmdRunner runner = container.get(taskId);
        // read from cache
        if (null == runner) {
            String log = restLogCache.getOrDefault(taskId, "");
            restLogCache.remove(taskId);
            return log ;
        }

        // read from input stream
        String logStr = runner.readOutput();
        appendLogFile(taskId, logStr);
        return logStr;
    }

    public String getHistoryLog(String taskId) {
        if(StringUtils.isEmpty(taskId)) {
            return "";
        }
        return readLogFile(taskId);
    }

    public void stopTask(String taskId) {
        CmdRunner runner = container.get(taskId);
        if (null != runner) {
            runner.kill();
        }
    }

    public TaskInfo getAliveTask(String taskId) {
        CmdRunner cmdRunner = container.get(taskId);
        if(null != cmdRunner) {
            return cmdRunner.getTaskInfo();
        }
        return null;
    }

    private void clean() {
        container.entrySet().removeIf(e -> !e.getValue().isAlive());
    }

    private void appendLogFile(String taskId, String logStr) {
        if (StringUtils.isEmpty(logStr)) {
            return;
        }
        try (FileWriter writer = new FileWriter(LOG_BASE_DIR + taskId + LOG_SUFFIX, true)) {
            writer.append(logStr);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readLogFile(String taskId) {
        String fileName = LOG_BASE_DIR + taskId + LOG_SUFFIX;
        if (!checkFile(fileName)) {
            return "";
        }
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkFile(String fileName) {
        return Files.exists(Paths.get(fileName));
    }
}
