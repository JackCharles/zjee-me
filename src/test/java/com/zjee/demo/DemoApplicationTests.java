package com.zjee.demo;

import com.zjee.demo.service.util.IpInfoGenerator;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    private IpInfoGenerator ipInfoGenerator;

    @Test
    public void contextLoads() throws SigarException {
        Sigar sigar = new Sigar();
        System.out.println(OperatingSystem.getInstance().toMap());
    }

    @Test
    public void test(){
        System.out.println(ipInfoGenerator.getIpLocationInfo("49.140.86.146"));
    }

}
