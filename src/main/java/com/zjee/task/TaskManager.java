package com.zjee.task;

import com.zjee.dal.TaskMapper;
import com.zjee.pojo.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TaskManager {

    private final ConcurrentHashMap<String, CmdRunner> container;

    private final int maxTaskCount;

    final private TaskMapper taskMapper;

    private static final String STDOUT_SUFFIX = "-stdout.log";

    private static final String STDERR_SUFFIX = "-stderr.log";

    private static final String LOG_BASE_DIR = "logs/task/";

    public TaskManager(int maxTaskCount, TaskMapper taskMapper) {
        if (maxTaskCount < 0) {
            throw new RuntimeException("Illegal maxTaskCount: " + maxTaskCount);
        }
        this.taskMapper = taskMapper;
        this.maxTaskCount = maxTaskCount;
        container = new ConcurrentHashMap<>(maxTaskCount);
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
        if(pageNo < 0 || pageSize < 0) {
            throw new RuntimeException("Illegal page number or size");
        }
        return taskMapper.getAllTask(pageSize, pageNo*pageSize);
    }

    public void taskFinish(CmdRunner runner, Throwable e) {
        if (null == runner || runner.isAlive()) {
            return;
        }

        TaskInfo taskInfo = runner.getTaskInfo();
        try {
            // save std out
            String restLog = runner.readStdOut(Integer.MAX_VALUE);
            while (!StringUtils.isEmpty(restLog)) {
                appendLogFile(taskInfo.getTaskId(), restLog, STDOUT_SUFFIX);
                restLog = runner.readStdOut(Integer.MAX_VALUE);
            }
            runner.getStdOutReader().close();

            // save std err
            String restErr = runner.readStdErr(Integer.MAX_VALUE);
            while (!StringUtils.isEmpty(restErr)) {
                appendLogFile(taskInfo.getTaskId(), restErr, STDERR_SUFFIX);
                restErr = runner.readStdErr(Integer.MAX_VALUE);
            }
            runner.getStdErrReader().close();
        } catch (Exception ex) {
            log.error("ERROR: ", ex);
        }

        // WriteDB
        taskMapper.updateTask(taskInfo);
        container.remove(taskInfo.getTaskId());
    }

    public String getTaskStdOut(String taskId, int pageNo, int pageSize) {
        CmdRunner runner = container.get(taskId);
        if (null != runner) {
            String partLog = runner.readStdOut(pageSize);
            appendLogFile(taskId, partLog, STDOUT_SUFFIX);
            return partLog;
        }
        else {
            return readLogFile(taskId, pageNo * pageSize, pageSize, STDOUT_SUFFIX);
        }
    }

    public String getTaskStdErr(String taskId, int pageNo, int pageSize) {
        CmdRunner runner = container.get(taskId);
        if (null != runner) {
            String partLog = runner.readStdErr(pageSize);
            appendLogFile(taskId, partLog, STDERR_SUFFIX);
            return partLog;
        }
        else {
            return readLogFile(taskId, pageNo * pageSize, pageSize, STDERR_SUFFIX);
        }
    }

    public void stopTask(String taskId) {
        CmdRunner runner = container.get(taskId);
        if(null != runner) {
            runner.kill();
        }
    }

    private void clean() {
        container.entrySet().removeIf(e -> !e.getValue().isAlive());
    }

    private void appendLogFile(String taskId, String logStr, String logType) {
        if (StringUtils.isEmpty(logStr)) {
            return;
        }
        try (FileWriter writer = new FileWriter(LOG_BASE_DIR + taskId + logType, true)) {
            writer.append(logStr);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readLogFile(String taskId, int lineNum, int readCount, String logType) {
        String fileName = LOG_BASE_DIR + taskId + logType;
        if(!checkFile(fileName)) {
            return "";
        }
        try(Stream<String> lines = Files.lines(Paths.get(fileName))) {
            return lines.skip(lineNum).limit(readCount).collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

     private boolean checkFile(String fileName) {
        return Files.exists(Paths.get(fileName));
     }
}
