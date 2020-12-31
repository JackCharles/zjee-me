package com.zjee.config;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.sql.ResultSet;

@Configuration
@MapperScan(basePackages = "com.zjee.dal")
public class DbConfig implements InitializingBean {

    @Autowired
    SqlSessionFactory sessionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        ResultSet resultSet = sessionFactory.openSession().getConnection().prepareStatement("select * from cmd_task limit 0, 3").executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString(2));
        }
    }
}
