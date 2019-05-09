package com.bairock.intelDevPc.view;

import com.bairock.intelDevPc.data.MyColor;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;
import javafx.scene.Cursor;

public class DragDevSwitchNode extends DragDeviceNode {

    private Device device;

    public DragDevSwitchNode(DragDevice dragDevice) {
        super(dragDevice);
        device = dragDevice.getDevice();
        setGear(dragDevice.getDevice().getGear());
        refreshState();
        device.addOnStateChangedListener(onStateChangedListener);
        device.addOnGearChangedListener(onGearChangedListener);
        device.addOnNameChangedListener(onNameChangedListener);
        
        if(null == dragDevice.getImageName() || dragDevice.getImageName().isEmpty()) {
            dragDevice.setImageName("../img/switch.png");
        }
        refreshImage();
        this.setCursor(Cursor.HAND);
    }

    public void setGear(Gear gear) {
        String strGear;
        switch (gear) {
        case GUAN:
            strGear = "S";
            break;
        case KAI:
            strGear = "O";
            break;
        default:
            strGear = "A";
            break;
        }
        Platform.runLater(() -> labelValue.setText(strGear));
    }

    public void refreshGear() {
        setGear(device.getGear());
    }

    public void refreshState() {
        if (device.getDevStateId().equals(DevStateHelper.DS_KAI)) {
            paneState.setStyle("-fx-background-color : " + MyColor.SUCCESS);
        } else if (device.getDevStateId().equals(DevStateHelper.DS_GUAN)) {
            paneState.setStyle("-fx-background-color : " + MyColor.SECONDARY);
        } else if (!device.isNormal()) {
            paneState.setStyle("-fx-background-color : " + MyColor.DANGER);
        }
    }

    private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

        @Override
        public void onNameChanged(MyHome myHome, String name) {
            labelName.setText(name);
        }

    };

    private OnGearChangedListener onGearChangedListener = new OnGearChangedListener() {
        @Override
        public void onGearChanged(Device dev, Gear gear, boolean touchDev) {
            refreshGear();
        }
    };

    private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

        @Override
        public void onStateChanged(Device dev, String stateId) {
            if (stateId.equals(DevStateHelper.DS_UNKNOW)) {
                return;
            }
            refreshState();
        }

        @Override
        public void onNormalToAbnormal(Device dev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAbnormalToNormal(Device dev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onNoResponse(Device dev) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    public void destory() {
        device.removeOnNameChangedListener(onNameChangedListener);
        device.removeOnStateChangedListener(onStateChangedListener);
    }
}
