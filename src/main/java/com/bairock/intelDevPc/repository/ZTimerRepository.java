package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.linkage.timing.ZTimer;

public interface ZTimerRepository extends JpaRepository<ZTimer, String> {

}
