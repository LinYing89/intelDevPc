package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.data.DeviceImg;

public interface DeviceImgRepo extends JpaRepository<DeviceImg, Long> {

	DeviceImg findByCode(String code);
}
