package com.zjee.dal;

import com.zjee.pojo.BwStat;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZhongJie
 * @date 17:54
 * @desc
 */
@Mapper
public interface BandwidthMapper {

    @Insert("INSERT INTO bandwidth_stat VALUES(#{dt}, #{usageToday}, #{usageTotal}, #{capacity})")
    int insertOne(BwStat bwStat);

    @Select("SELECT * FROM bandwidth_stat WHERE dt BETWEEN #{startTime} AND #{endTime}")
    List<BwStat> getBwStat(@Param("startTime") LocalDateTime startTime,
                           @Param("endTime")   LocalDateTime endTime);
}
