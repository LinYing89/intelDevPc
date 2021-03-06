package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.user.DevGroup;

public interface DevGroupRepo extends JpaRepository<DevGroup, String> {

	DevGroup findByName(String name);
	DevGroup findByNameAndUserid(String name, String userid);
}
