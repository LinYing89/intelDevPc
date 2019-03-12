package com.bairock.intelDevPc.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.data.DeviceValueHistory;

@Service
public class DeviceHistoryService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public void createTable(String deviceLongCoding) {
		String sql = "create table if not exists device_history_" + deviceLongCoding + "("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "device_id varchar(255) NOT NULL,"
				+ "device_long_coding varchar(25) NOT NULL,"
				+ "device_name varchar(25) NOT NULL,"
				+ "abnormal BIT NOT NULL,"
				+ "value float NOT NULL,"
				+ "history_time timestamp NOT NULL,"
				+ "PRIMARY KEY (id))";
		jdbcTemplate.execute(sql);
	}
	
	public void alartTable() {
		String sql = "select drop table table_name from hama.tables where table_name like device_history%";
		jdbcTemplate.execute(sql);
	}
	
	public void insert(DeviceValueHistory history) {
		String sql = "insert into device_history_" + history.getLongCoding() + "(device_id, device_long_coding, device_name, abnormal, value, history_time) values(?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql, history.getDeviceId(), history.getLongCoding(), history.getDeviceName(), history.isAbnormal(), history.getValue(), history.getHistoryTime());
	}
	
	public List<DeviceValueHistory> find(String deviceLongCoding, Date startTime, Date endTime){
		List<DeviceValueHistory> list = new ArrayList<>();
		String sql = "select * from device_history_" + deviceLongCoding + " where device_long_coding = ? and history_time>? and history_time<?";
		list = jdbcTemplate.query(sql, new Object[] {deviceLongCoding, startTime, endTime}, new BeanPropertyRowMapper<DeviceValueHistory>(DeviceValueHistory.class));
		return list;
	}
}
