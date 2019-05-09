package com.bairock.intelDevPc;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.bairock.intelDevPc.repository.UserRepository;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbReadTest {

	@Autowired
	private UserRepository userRepository;
	
	@Test
	@Transactional
	public void testInsertUser() {
		List<User> list = userRepository.findAll();
		for(User user : list) {
			System.out.println("user:" + user.getUserid());
			for(DevGroup group : user.getListDevGroup()) {
				System.out.println("group:" + group.getName());
				for(Device dev : group.getListDevice()) {
					System.out.println("dev:" + dev.getName());
				}
			}
		}
	}
}
