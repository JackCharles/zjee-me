package com.zjee.task;

import com.zjee.pojo.TaskInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@Slf4j
public class CmdRunner {

    private TaskInfo taskInfo;

    private TaskManager taskManager;

    private Process process;

    private BufferedReader outputReader;

    private static final int SUCCESS_STATUS = 0;

    private static final int INTERRUPT_STATUS = -9999;

    public static final int RUNNING_STATUS = -8888;

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
        taskInfo.setExitStatus(RUNNING_STATUS);
        List<String> cmdList = Arrays.stream(taskInfo.getCmd().split(" "))
            .map(StringUtils::trimAllWhitespace)
            .filter(s -> !StringUtils.isEmpty(s))
            .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(cmdList)) {
            throw new RuntimeException("Invalid command: " + taskInfo.getCmd());
        }
        log.info("Process starting: {}", String.join(" ", cmdList));
        this.process = new ProcessBuilder(cmdList)
            .redirectErrorStream(true)
            .start();
        log.info("Process started: {}", String.join(" ", cmdList));
        outputReader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), getCharset()));
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

    public String readOutput() {
        if (null == outputReader) {
            return null;
        }

        String line;
        StringBuilder builder = new StringBuilder();
        try {
            //Note: readLine在缓冲区未满、流未关闭、没有遇见换行符的情况下会阻塞掉，因此读之前都需要判断数据是否就绪
            while (outputReader.ready() && (line = outputReader.readLine()) != null) {
                builder.append(line).append("\n");
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
