package com.bairock.intelDevPc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bairock.intelDevPc.data.DeviceValueHistory;

public interface DeviceValueHistoryRepo extends JpaRepository<DeviceValueHistory, Long> {

	@Query(value="select * from device_value_history e where device_id=:deviceId order by history_time", nativeQuery = true)
	List<DeviceValueHistory> findByDeviceId(@Param("deviceId") String deviceId);
	
	@Query(value="select * from device_value_history e where device_id=:deviceId order by history_time limit :mylimit", nativeQuery = true)
	List<DeviceValueHistory> findByDeviceIdAndLimit(@Param("deviceId") long deviceId, @Param("mylimit") int mylimit);
}
