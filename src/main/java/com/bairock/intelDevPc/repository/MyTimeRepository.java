package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.linkage.timing.MyTime;

public interface MyTimeRepository extends JpaRepository<MyTime, String> {

}
