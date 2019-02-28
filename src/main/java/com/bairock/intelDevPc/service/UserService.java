package com.bairock.intelDevPc.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.comm.MyOnCurrentValueChangedListener;
import com.bairock.intelDevPc.comm.MyOnGearChangedListener;
import com.bairock.intelDevPc.comm.MyOnSortIndexChangedListener;
import com.bairock.intelDevPc.comm.MyOnStateChangedListener;
import com.bairock.intelDevPc.repository.UserRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.FindDevHelper;
import com.bairock.iot.intelDev.communication.UdpServer;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.linkage.Linkage;
import com.bairock.iot.intelDev.linkage.LinkageHelper;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.linkage.LinkageTab;
import com.bairock.iot.intelDev.linkage.SubChain;
import com.bairock.iot.intelDev.linkage.guagua.GuaguaHelper;
import com.bairock.iot.intelDev.linkage.timing.Timing;
import com.bairock.iot.intelDev.linkage.timing.ZTimer;
import com.bairock.iot.intelDev.order.OrderType;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	public static User user;

	private UserRepository userRepository;
	private DeviceHistoryService deviceHistoryService;
	
	public static DevGroup getDevGroup() {
		return user.getListDevGroup().get(0);
	}
	
	@Autowired
	public UserService(UserRepository userRepository, DeviceHistoryService deviceHistoryService) {
		this.userRepository = userRepository;
		this.deviceHistoryService = deviceHistoryService;
		//initUser();
	}
	
	public void cleanUser() {
		List<User> list = userRepository.findAll();
		for(User user : list) {
			userRepository.deleteById(user.getId());
			userRepository.flush();
		}
	}
	
	public void addUser(User user) {
		userRepository.saveAndFlush(user);
	}
	
	public void update(User user) {
		userRepository.saveAndFlush(user);
	}
	
	@Transactional
	public void initUser() {
		
		DevGroup group = null;
		
		List<User> listUser = userRepository.findAll();
		if(listUser.isEmpty()) {
			user = new User("test123", "a123456", "", "", "admin", new Date());
			group = new DevGroup("1", "a123", "g1");
			group.setId(UUID.randomUUID().toString());
			user.addGroup(group);
			userRepository.saveAndFlush(user);
		}else {
			user = listUser.get(0);
			group = user.getListDevGroup().get(0);
		}
		DevChannelBridgeHelper.getIns().setUser(user);
		UdpServer.getIns().setUser(user);
		
		reloadDevGroup(group);
		
	}
	
	public void reloadDevGroup(DevGroup group) {
		System.out.println("userServer group " + (group.getUser() == user));
		FindDevHelper.getIns().cleanAll();
		for(Device dev : group.getListDevice()) {
			System.out.println(dev);
			FindDevHelper.getIns().findDev(dev.getCoding());
			dev.setDevStateId(DevStateHelper.DS_YI_CHANG);
			initDevice(dev);
		}
		for(LinkageHolder holder : group.getListLinkageHolder()) {
			for(Linkage linkage : holder.getListLinkage()) {
				linkage.getListEffect();
				if(linkage instanceof SubChain) {
					((SubChain) linkage).getListCondition();
				}else if(linkage instanceof Timing) {
					for(ZTimer timer : ((Timing) linkage).getListZTimer()) {
						timer.getListTimes();
					}
				}
			}
		}
		
		List<Device> list = group.findListIStateDev(true);
		LinkageTab.getIns().getListLinkageTabRow().clear();
		for(Device dev : list) {
			LinkageTab.getIns().addTabRow(dev);
		}
		
		LinkageHelper.getIns().setChain(group.getChainHolder());
		LinkageHelper.getIns().setLoop(group.getLoopHolder());
		LinkageHelper.getIns().setTiming(group.getTimingHolder());
		
		GuaguaHelper.getIns().setGuaguaHolder(group.getGuaguaHolder());
		
		LinkageTab.getIns().SetOnOrderSendListener((device, order, ctrlModel) ->{
			if(null != order) {
				IntelDevPcApplication.sendOrder(device, order, OrderType.CTRL_DEV, false);
			}
		});
		
		LinkageHelper.getIns().stopCheckLinkageThread();
		LinkageHelper.getIns().startCheckLinkageThread();
		GuaguaHelper.getIns().stopCheckGuaguaThread();
		GuaguaHelper.getIns().startCheckGuaguaThread();
		GuaguaHelper.getIns().setOnOrderSendListener((guagua, s, ctrlModel) ->{
			IntelDevPcApplication.sendOrder(guagua.findSuperParent(), s, OrderType.CTRL_DEV, true);
		}); 
	}
	
	private void initDevice(Device dev) {
		dev.addOnStateChangedListener(new MyOnStateChangedListener());
		dev.addOnGearChangedListener(new MyOnGearChangedListener());
//		dev.setOnCtrlModelChanged(new MyOnCtrlModelChangedListener());
		dev.setOnSortIndexChangedListener(new MyOnSortIndexChangedListener());
		deviceHistoryService.createTable(dev.getLongCoding());
		if(dev instanceof DevHaveChild) {
			for(Device dd : ((DevHaveChild) dev).getListDev()) {
				initDevice(dd);
			}
		}
		if(dev instanceof DevCollect) {
			((DevCollect) dev).getCollectProperty().addOnCurrentValueChangedListener(new MyOnCurrentValueChangedListener());
		}
	}
	
	public static String getUserJson(User user){
        String json = null;
        if(null != user){
            ObjectMapper mapper = new ObjectMapper();
            try {
                json = mapper.writeValueAsString(user);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return json;
    }
}
