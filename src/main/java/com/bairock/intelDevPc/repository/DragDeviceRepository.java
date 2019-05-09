package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.data.DragDevice;

public interface DragDeviceRepository extends JpaRepository<DragDevice, String> {

	DragDevice findByDeviceId(String deviceId);
}
