package com.bairock.intelDevPc.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.MyOnCurrentValueChangedListener;
import com.bairock.intelDevPc.comm.MyOnGearChangedListener;
import com.bairock.intelDevPc.comm.MyOnSortIndexChangedListener;
import com.bairock.intelDevPc.comm.MyOnStateChangedListener;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.repository.DevGroupRepo;
import com.bairock.intelDevPc.repository.UserRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.FindDevHelper;
import com.bairock.iot.intelDev.communication.UdpServer;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.data.DragDeviceHelper;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.linkage.Linkage;
import com.bairock.iot.intelDev.linkage.LinkageHelper;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.linkage.LinkageTab;
import com.bairock.iot.intelDev.linkage.SubChain;
import com.bairock.iot.intelDev.linkage.guagua.GuaguaHelper;
import com.bairock.iot.intelDev.linkage.timing.Timing;
import com.bairock.iot.intelDev.linkage.timing.ZTimer;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.order.OrderType;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	@Autowired
	private Config config;
	@Autowired
	private DragDeviceService dragDeviceService;

	public static User user;
	private static DevGroup group;

	private UserRepository userRepository;
	private DevGroupRepo groupRepository;
	private DeviceHistoryService deviceHistoryService;

	public static DevGroup getDevGroup() {
		return group;
	}

	@Autowired
	public UserService(UserRepository userRepository, DevGroupRepo groupRepository,
			DeviceHistoryService deviceHistoryService) {
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.deviceHistoryService = deviceHistoryService;
		// initUser();
	}

	public void cleanUser() {
		List<User> list = userRepository.findAll();
		for (User user : list) {
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
		List<User> listUser = userRepository.findAll();
		List<DevGroup> listGroup = groupRepository.findAll();
		if (listUser.isEmpty() || listGroup.isEmpty() || null == config.getDevGroupName() || config.getDevGroupName().isEmpty()) {
			user = new User();
			user.setUserid(config.getUserid());
//			user.setPassword("a123456");
			group = new DevGroup();
			group.setName(config.getDevGroupName());
			group.setPetName(config.getDevGroupPetname());
			group.setId(UUID.randomUUID().toString());
			user.addGroup(group);
			userRepository.saveAndFlush(user);
			config.setAutoLogin(false);
		} else {
			user = listUser.get(0);
			// 从数据库读取组数据
//			List<DevGroup> listGroup = groupRepository.findAll();
			group = listGroup.get(0);
			//保持组的唯一, 删掉多余的, 正常情况下不会有多个
			if(listGroup.size() > 1) {
				for(int i=1; i<listGroup.size(); i++) {
					groupRepository.deleteById((listGroup.get(i).getId()));
				}
			}
			group.setUserid(user.getUserid());
			user.addGroup(group);
		}
		DevChannelBridgeHelper.getIns().setUser(user);
		UdpServer.getIns().setUser(user);

		reloadDevGroup(group);

	}

	public void reloadDevGroup(DevGroup group) {
		System.out.println("userServer group " + (group.getUser() == user));
		FindDevHelper.getIns().cleanAll();
		DragDeviceHelper.getIns().getDragDevices().clear();
		for (Device dev : group.getListDevice()) {
			System.out.println(dev);
			FindDevHelper.getIns().findDev(dev.getCoding());
			dev.setDevStateId(DevStateHelper.DS_UNKNOW);
			initDevice(dev);
		}
		for (LinkageHolder holder : group.getListLinkageHolder()) {
			for (Linkage linkage : holder.getListLinkage()) {
				linkage.getListEffect();
				if (linkage instanceof SubChain) {
					((SubChain) linkage).getListCondition();
				} else if (linkage instanceof Timing) {
					for (ZTimer timer : ((Timing) linkage).getListZTimer()) {
						timer.getListTimes();
					}
				}
			}
		}

		List<Device> list = group.findListIStateDev(true);
		LinkageTab.getIns().getListLinkageTabRow().clear();
		for (Device dev : list) {
			LinkageTab.getIns().addTabRow(dev);
		}

		LinkageHelper.getIns().setChain(group.getChainHolder());
		LinkageHelper.getIns().setLoop(group.getLoopHolder());
		LinkageHelper.getIns().setTiming(group.getTimingHolder());

		GuaguaHelper.getIns().setGuaguaHolder(group.getGuaguaHolder());

		if (null != config.getLoginModel() && config.getLoginModel().equals(LoginModel.LOCAL)) {
			LinkageTab.getIns().SetOnOrderSendListener((device, order, ctrlModel) -> {
				if (null != order) {
				    boolean deviceIsNormal = device.isNormal() && device.findSuperParent().isNormal();
					if (null != config.getLoginModel() && config.getLoginModel().equals(LoginModel.LOCAL) && deviceIsNormal) {
						IntelDevPcApplication.sendOrder(device, order, OrderType.CTRL_DEV, false);
					}
				}
			});

			LinkageHelper.getIns().stopCheckLinkageThread();
			LinkageHelper.getIns().startCheckLinkageThread();
			GuaguaHelper.getIns().stopCheckGuaguaThread();
			GuaguaHelper.getIns().startCheckGuaguaThread();
			GuaguaHelper.getIns().setOnOrderSendListener((guagua, s, ctrlModel) -> {
				if (null != config.getLoginModel() && config.getLoginModel().equals(LoginModel.LOCAL)) {
					IntelDevPcApplication.sendOrder(guagua.findSuperParent(), s, OrderType.CTRL_DEV, true);
				}
			});
		}
		//初始化组态界面配置数据
        Util.initDragConfig(group.getId());
	}

	private void initDevice(Device dev) {
		dev.addOnStateChangedListener(new MyOnStateChangedListener());
		dev.addOnGearChangedListener(new MyOnGearChangedListener());
//		dev.setOnCtrlModelChanged(new MyOnCtrlModelChangedListener());
		dev.setOnSortIndexChangedListener(new MyOnSortIndexChangedListener());
		deviceHistoryService.createTable(dev.getLongCoding());
		if (dev instanceof DevHaveChild) {
			for (Device dd : ((DevHaveChild) dev).getListDev()) {
				initDevice(dd);
			}
		}
		if (dev instanceof DevCollect) {
			((DevCollect) dev).getCollectProperty()
					.addOnCurrentValueChangedListener(new MyOnCurrentValueChangedListener());
			findDragDevice(dev);
		}else if(dev instanceof IStateDev) {
			findDragDevice(dev);
		}
	}
	
	private void findDragDevice(Device dev) {
//	    if(!dev.isVisibility()) {
//	        return;
//	    }
		DragDevice dragDevice = dragDeviceService.findByDeviceId(dev.getId());
		if(null == dragDevice) {
			dragDevice = new DragDevice();
			dragDevice.setId(UUID.randomUUID().toString());
			dragDevice.setDeviceId(dev.getId());
			dragDeviceService.insert(dragDevice);
		}
		dragDevice.setDevice(dev);
		DragDeviceHelper.getIns().addDragDevice(dragDevice);
	}

	public static String getUserJson(User user) {
		String json = null;
		if (null != user) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				json = mapper.writeValueAsString(user);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return json;
	}
}
