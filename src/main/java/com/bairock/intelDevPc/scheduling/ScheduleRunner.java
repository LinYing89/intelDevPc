package com.bairock.intelDevPc.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.PadClient;

@Component
public class ScheduleRunner {

//	Logger logger = LoggerFactory.getLogger(getClass());

	@Scheduled(fixedDelay = 10000)
	public void checkServerConnect() {
//		logger.info("checkServerConnect");
		if (Util.CAN_CONNECT_SERVER) {
			if (!PadClient.getIns().isLinked()) {
				PadClient.getIns().link();
			}
		}
	}
}
