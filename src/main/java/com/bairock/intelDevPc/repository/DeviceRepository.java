package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.device.Device;

public interface DeviceRepository extends JpaRepository<Device, String> {

}
