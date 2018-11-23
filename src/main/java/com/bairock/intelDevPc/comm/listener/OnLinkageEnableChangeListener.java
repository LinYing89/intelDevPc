package com.bairock.intelDevPc.comm.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.service.LinkageService;
import com.bairock.iot.intelDev.linkage.Linkage;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * 连锁使能监听器
 * @author 44489
 *
 */
public class OnLinkageEnableChangeListener implements ChangeListener<Boolean>{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private LinkageService linkageService = SpringUtil.getBean(LinkageService.class);
	
	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) observable;
		Linkage linkage = (Linkage) boolProp.getBean();
		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
		linkageService.updateLinkage(linkage);
	}

}
