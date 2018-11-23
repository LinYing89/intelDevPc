package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.linkage.timing.WeekHelper;

public interface WeekHelperRepository extends JpaRepository<WeekHelper, String> {

}
