package com.zjee.controller;

import com.zjee.pojo.TaskInfo;
import com.zjee.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("submit")
    public ResponseEntity submitTask(@RequestBody TaskInfo taskInfo) {
        if (taskInfo == null) {
            return ResponseEntity.badRequest().body("Empty task info");
        }

        try {
            TaskInfo retInfo = taskService.submitTask(taskInfo);
            return ResponseEntity.ok(retInfo);
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping("list")
    public ResponseEntity getAllTask(@RequestBody TaskInfo taskInfo) {
        if (taskInfo == null) {
            return ResponseEntity.badRequest().body("Empty task info");
        }

        try {
            List<TaskInfo> tasks = taskService.getAllTask(taskInfo.getPageNo(), taskInfo.getPageSize());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("log")
    public ResponseEntity getLog(@RequestBody TaskInfo taskInfo) {
        if (taskInfo == null) {
            return ResponseEntity.badRequest().body("Empty task info");
        }

        try {
            return ResponseEntity.ok(taskService.getLog(taskInfo.getTaskId(), false));
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("hisLog")
    public ResponseEntity getHistoryLog(@RequestBody TaskInfo taskInfo) {
        if (taskInfo == null) {
            return ResponseEntity.badRequest().body("Empty task info");
        }

        try {
            return ResponseEntity.ok(taskService.getLog(taskInfo.getTaskId(), true));
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("stop")
    public ResponseEntity stopTask(@RequestBody TaskInfo taskInfo) {
        if (taskInfo == null) {
            return ResponseEntity.badRequest().body("Empty task info");
        }

        try {
            taskService.stopTask(taskInfo.getTaskId());
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
