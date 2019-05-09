package com.bairock.intelDevPc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.repository.DragDeviceRepository;
import com.bairock.iot.intelDev.data.DragDevice;

@Service
public class DragDeviceService {

	@Autowired
	private DragDeviceRepository dragDeviceRepository;
	
	public DragDevice findByDeviceId(String deviceId) {
		return dragDeviceRepository.findByDeviceId(deviceId);
	}
	
	public DragDevice insert(DragDevice dragDevice) {
		return dragDeviceRepository.saveAndFlush(dragDevice);
	}
}
