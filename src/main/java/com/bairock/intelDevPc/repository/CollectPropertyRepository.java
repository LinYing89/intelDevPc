package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.device.devcollect.CollectProperty;

public interface CollectPropertyRepository extends JpaRepository<CollectProperty, String> {

}
