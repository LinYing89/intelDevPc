package com.bairock.intelDevPc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.bairock.intelDevPc.repository.LoopDurationRepository;
import com.bairock.iot.intelDev.linkage.loop.LoopDuration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbLoopDurationTest {

	@Autowired
	private LoopDurationRepository repository;
	
	@Test
	public void testInsertUser() {
		
		LoopDuration duration = new LoopDuration();
		repository.saveAndFlush(duration);
	}
}
