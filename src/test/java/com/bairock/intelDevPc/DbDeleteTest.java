package com.bairock.intelDevPc;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.junit4.SpringRunner;

import com.bairock.intelDevPc.repository.UserRepository;
import com.bairock.iot.intelDev.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbDeleteTest {

	@Autowired
	private UserRepository userRepository;
	
	@Test
	@Modifying
	@Transactional
	public void testInsertUser() {
		List<User> list = userRepository.findAll();
//		userRepository.deleteAll();
//		userRepository.deleteAll(list);
		for(User user : list) {
			userRepository.deleteById(user.getId());
			userRepository.flush();
		}
	}
}
