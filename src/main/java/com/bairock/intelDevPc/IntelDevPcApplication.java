package com.bairock.intelDevPc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.controller.LoginController;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.UILayoutConfig;
import com.bairock.intelDevPc.repository.ConfigRepository;
import com.bairock.intelDevPc.repository.UILayoutConfigRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.FindDevHelper;
import com.bairock.iot.intelDev.device.Coordinator;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.linkage.timing.WeekHelper;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//jdbc:h2:file:C:\project\workspace-sts-3.9.5.RELEASE\intelDevPc\db\demo_db2

@SpringBootApplication
@EntityScan({ "com.bairock.iot.intelDev", "com.bairock.intelDevPc.data" })
@EnableScheduling
public class IntelDevPcApplication extends AbstractJavaFxApplicationSupport {

    public static boolean SERVER_CONNECTED = false;

    public static void main(String[] args) {
        // SpringApplication.run(IntelDevPcApplication.class, args);
        System.out.println("hello");
        // launch(args);

        launch(IntelDevPcApplication.class, LoginView.class, new CustomSplash(), args);
    }

    @Override
    public Collection<Image> loadDefaultIcons() {
        return Arrays.asList(new Image(getClass().getResource("/img/icons/guagua_16x16.png").toExternalForm()),
                new Image(getClass().getResource("/img/icons/guagua_24x24.png").toExternalForm()),
                new Image(getClass().getResource("/img/icons/guagua_36x36.png").toExternalForm()),
                new Image(getClass().getResource("/img/icons/guagua_42x42.png").toExternalForm()),
                new Image(getClass().getResource("/img/icons/guagua_64x64.png").toExternalForm()));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        super.start(primaryStage);
        primaryStage.setOnCloseRequest(e -> handle(e));
        primaryStage.setOnShown(e -> handleShown(e));
        initMainCodeInfo();
        WeekHelper.ARRAY_WEEKS = new String[] { "日", "一", "二", "三", "四", "五", "六" };
    }

    public void handle(WindowEvent e) {
        EventType<WindowEvent> type = e.getEventType();
        if (type == WindowEvent.WINDOW_CLOSE_REQUEST) {
            System.exit(0);
        }
    }

    public void handleShown(WindowEvent e) {
        System.out.println("handleShown");
//		UserService userService = SpringUtil.getBean(UserService.class);
        LoginView loginView = SpringUtil.getBean(LoginView.class);
//		userService.initUser();
        ((LoginController) (loginView.getPresenter())).init();
    }

    public static void addOfflineDevCoding(Device device) {
        if (null != device) {
            if (device instanceof Coordinator) {
                FindDevHelper.getIns().findDev(device.getCoding());
            } else if (!(device.findSuperParent() instanceof Coordinator)) {
                FindDevHelper.getIns().findDev(device.findSuperParent().getCoding());
            }
        }
    }

    public static void removeOfflineDevCoding(Device device) {
        if (null != device) {
            if (null == device.getParent() || !(device.findSuperParent() instanceof Coordinator)) {
                FindDevHelper.getIns().alreadyFind(device.findSuperParent().getCoding());
            }
        }
    }

    public static void sendOrder(Device device, String order, OrderType orderType, boolean immediately) {
        switch (device.getCtrlModel()) {
        case UNKNOW:
            DevChannelBridgeHelper.getIns().sendDevOrder(device, order, immediately);
            String devOrder = createDeviceOrder(device, orderType, order);
            PadClient.getIns().send(devOrder);
            break;
        case LOCAL:
            DevChannelBridgeHelper.getIns().sendDevOrder(device, order, immediately);
            break;
        case REMOTE:
            Device superParent = device.findSuperParent();
            devOrder = createDeviceOrder(device, orderType, order);
            if(immediately) {
                PadClient.getIns().send(devOrder);
            }else if (superParent.canSend()) {
                superParent.setLastOrder(order);
                superParent.resetLastCommunicationTime();
                PadClient.getIns().send(devOrder);
            }
            break;
        }
    }

//	public static void sendOrder(Device device, String order, boolean immediately) {
//		
//		switch (device.getCtrlModel()) {
//		case UNKNOW:
//			DevChannelBridgeHelper.getIns().sendDevOrder(device, order, immediately);
//			String strOrderBase = getOrderBaseString(device, order);
//			PadClient.getIns().send(strOrderBase);
//			break;
//		case LOCAL:
//			DevChannelBridgeHelper.getIns().sendDevOrder(device, order, immediately);
//			break;
//		case REMOTE:
//			// 远程设备没判断发送间隔, 所以同一条命令没有获得响应时发送的可能比较频繁
//			String strOrderBase1 = getOrderBaseString(device, order);
//			PadClient.getIns().send(strOrderBase1);
//			break;
//		}
//	}

    public static String createDeviceOrder(Device device, OrderType orderType, String order) {
        DeviceOrder ob = new DeviceOrder();
        ob.setOrderType(orderType);
        ob.setLongCoding(device.getLongCoding());
        ob.setData(order);
        return Util.orderBaseToString(ob);
    }

    @Bean
    @Autowired
    public Config initConfig(ConfigRepository configRepository) {
        List<Config> list = configRepository.findAll();
        if (list.isEmpty()) {
            Config config = new Config();
            configRepository.saveAndFlush(config);
            return config;
        } else {
            return list.get(0);
        }
    }

    @Bean
    @Autowired
    public UILayoutConfig initUILayoutConfig(UILayoutConfigRepository configRepository) {
        List<UILayoutConfig> list = configRepository.findAll();
        if (list.isEmpty()) {
            UILayoutConfig config = new UILayoutConfig();
            configRepository.saveAndFlush(config);
            return config;
        } else {
            return list.get(0);
        }
    }

    private void initMainCodeInfo() {
        Map<String, String> map = new HashMap<>();
        map.put(MainCodeHelper.XIE_TIAO_QI, "协调器");
        map.put(MainCodeHelper.GUAGUA_MOUTH, "呱呱嘴");
        map.put(MainCodeHelper.MEN_JIN, "门禁");
        map.put(MainCodeHelper.YE_WEI, "液位计");
        map.put(MainCodeHelper.COLLECTOR_SIGNAL, "信号采集器");
        map.put(MainCodeHelper.COLLECTOR_SIGNAL_CONTAINER, "多功能信号采集器");
        map.put(MainCodeHelper.COLLECTOR_CLIMATE_CONTAINER, "多功能气候采集器");
        map.put(MainCodeHelper.YAN_WU, "烟雾探测器");
        map.put(MainCodeHelper.WEN_DU, "温度");
        map.put(MainCodeHelper.SHI_DU, "湿度");
        map.put(MainCodeHelper.JIA_QUAN, "甲醛");
        map.put(MainCodeHelper.KG_1LU_2TAI, "一路开关");
        map.put(MainCodeHelper.KG_2LU_2TAI, "两路开关");
        map.put(MainCodeHelper.KG_3LU_2TAI, "三路开关");
        map.put(MainCodeHelper.KG_XLU_2TAI, "多路开关");
        map.put(MainCodeHelper.KG_3TAI, "三态开关");
        map.put(MainCodeHelper.YAO_KONG, "遥控器");
        map.put(MainCodeHelper.CHA_ZUO, "插座");
        map.put(MainCodeHelper.SMC_WU, "未知");
        map.put(MainCodeHelper.SMC_REMOTER_CHUANG_LIAN, "窗帘");
        map.put(MainCodeHelper.SMC_REMOTER_DIAN_SHI, "电视");
        map.put(MainCodeHelper.SMC_REMOTER_KONG_TIAO, "空调");
        map.put(MainCodeHelper.SMC_REMOTER_TOU_YING, "投影仪");
        map.put(MainCodeHelper.SMC_REMOTER_MU_BU, "投影幕布");
        map.put(MainCodeHelper.SMC_REMOTER_SHENG_JIANG_JIA, "升降架");
        map.put(MainCodeHelper.SMC_REMOTER_ZI_DING_YI, "自定义");
        map.put(MainCodeHelper.SMC_DENG, "灯");
        map.put(MainCodeHelper.SMC_CHUANG_HU, "窗帘");
        map.put(MainCodeHelper.SMC_FA_MEN, "阀门");
        map.put(MainCodeHelper.SMC_BING_XIANG, "冰箱");
        map.put(MainCodeHelper.SMC_XI_YI_JI, "洗衣机");
        map.put(MainCodeHelper.SMC_WEI_BO_LU, "微波炉");
        map.put(MainCodeHelper.SMC_YIN_XIANG, "音箱");
        map.put(MainCodeHelper.SMC_SHUI_LONG_TOU, "水龙头");

        MainCodeHelper.getIns().setManCodeInfo(map);
    }
//	@Bean
//	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//		return args -> {
//			System.out.println("Let's inspect the beans provided by Spring Boot:");
//			String[] beanNames = ctx.getBeanDefinitionNames();
//			Arrays.sort(beanNames);
//			for (String beanName : beanNames) {
//				System.out.println(beanName);
//			}
//		};
//	}

    public static void showView(final Class<? extends AbstractFxmlView> window, final Modality mode,
            OnStageCreatedListener onStageCreatedListener) {
        final AbstractFxmlView view = SpringUtil.getBean(window);
        Stage newStage = new Stage();

        Scene newScene;
        if (view.getView().getScene() != null) {
            // This view was already shown so
            // we have a scene for it and use this one.
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setScene(newScene);
        newStage.initModality(mode);
        newStage.initOwner(getStage());

        if (null != onStageCreatedListener) {
            onStageCreatedListener.onStageCreated(newStage);
        }
        newStage.showAndWait();
    }
    
    public static void showViewMax(final Class<? extends AbstractFxmlView> window, String title) {
        final AbstractFxmlView view = SpringUtil.getBean(window);
        Stage newStage = new Stage();

        Scene newScene;
        if (view.getView().getScene() != null) {
            // This view was already shown so
            // we have a scene for it and use this one.
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setTitle(title);
        newStage.setScene(newScene);
        newStage.initOwner(getStage());
        newStage.setMaximized(true);
        newStage.showAndWait();
    }

    public interface OnStageCreatedListener {
        void onStageCreated(Stage newStage);
    }
}
