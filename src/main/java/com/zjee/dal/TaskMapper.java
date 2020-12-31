package com.zjee.dal;

import com.zjee.pojo.TaskInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("select * from cmd_task order by id desc limit #{offset}, #{limit}")
    List<TaskInfo> getAllTask(@Param("limit") int limit, @Param("offset") int offset);

    @Insert("insert into cmd_task (task_id, cmd, time_out, start_time, end_time, exit_status, desc) " +
        "values (#{taskId}, #{cmd}, #{timeOut}, " +
        "#{startTime}, #{endTime}, #{exitStatus}, #{desc})")
    int addTask(TaskInfo taskInfo);

    @Update("update cmd_task set start_time=#{startTime}, end_time=#{endTime}, " +
        "exit_status=#{exitStatus} where task_id=#{taskId}")
    int updateTask(TaskInfo taskInfo);
}
