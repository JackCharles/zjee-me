package com.zjee.task;

import com.zjee.pojo.TaskInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class CmdRunner {

    private TaskInfo taskInfo;

    private TaskManager taskManager;

    private Process process;

    private BufferedReader stdOutReader;

    private BufferedReader stdErrReader;

    private static final int SUCCESS_STATUS = 0;

    private static final int INTERRUPT_STATUS = -9999;

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CmdRunner(TaskInfo taskInfo, TaskManager taskManager) {
        if (!validate(taskInfo)) {
            throw new RuntimeException("Illegal task info");
        }
        this.taskInfo = taskInfo;
        this.taskManager = taskManager;
        this.taskInfo.setTaskId(UUID.randomUUID().toString());
    }

    public void run(int timeoutSec) throws IOException {
        taskInfo.setStartTime(LocalDateTime.now().format(DEFAULT_FORMATTER));
        process = Runtime.getRuntime().exec(taskInfo.getCmd());
        stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), getCharset()));
        stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), getCharset()));
        waitForFinish(timeoutSec);
    }

    private void waitForFinish(long timeoutSec) {
        CompletableFuture.runAsync(() -> {
            try {
                if (timeoutSec > 0) {
                    process.waitFor(timeoutSec, TimeUnit.SECONDS);
                }
                else {
                    process.waitFor();
                }
                taskInfo.setExitStatus(process.exitValue());
            } catch (InterruptedException e) {
                log.error("ERROR: ", e);
                taskInfo.setExitStatus(INTERRUPT_STATUS);
            }
        }).whenComplete((executor, exception) -> {
            taskInfo.setEndTime(LocalDateTime.now().format(DEFAULT_FORMATTER));
            taskManager.taskFinish(this, exception);
        });
    }

    public void kill() {
        if (isAlive()) {
            process.destroy();
        }
    }

    public String readStdOut(int maxCount) {
        return readOutput(maxCount, stdOutReader);
    }

    public String readStdErr(int maxCount) {
        return readOutput(maxCount, stdErrReader);
    }

    private String readOutput(int maxCount, BufferedReader reader) {
        if (null == reader) {
            return null;
        }

        if (maxCount < 0) {
            maxCount = Integer.MAX_VALUE;
        }

        String line;
        int i = 0;
        StringBuilder builder = new StringBuilder();
        try {
            while (i < maxCount && (line = reader.readLine()) != null) {
                builder.append(line).append("\n");
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    private boolean validate(TaskInfo info) {
        return null != info && !StringUtils.isEmpty(info.getCmd());
    }

    private String getCharset() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("windows") ? "GBK" : "UTF-8";
    }
}
