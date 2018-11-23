package com.bairock.intelDevPc;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.bairock.intelDevPc.repository.UserRepository;
import com.bairock.iot.intelDev.device.DeviceAssistent;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.device.devcollect.Pressure;
import com.bairock.iot.intelDev.device.devswitch.DevSwitchOneRoad;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTest {
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	public void testInsertUser() {
		
		List<User> list = userRepository.findAll();
		for(User user : list) {
			userRepository.deleteById(user.getId());
			userRepository.flush();
		}
		
		User user = new User("jack", "a123", "444@qq.com", "171", "admin", new Date());
		DevGroup group = new DevGroup("group", "a123", "g1");
		user.addGroup(group);
		DevSwitchOneRoad dsor = new DevSwitchOneRoad(MainCodeHelper.KG_1LU_2TAI, "0001");
		group.addDevice(dsor);
		Pressure pressure = (Pressure) DeviceAssistent.createDeviceByMcId(MainCodeHelper.YE_WEI, "0001");
		group.addDevice(pressure);
		userRepository.saveAndFlush(user);
	}
}
