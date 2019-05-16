package com.bairock.intelDevPc.view;

import com.bairock.intelDevPc.data.MyColor;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;

public class DragDevCollectorNode extends DragDeviceNode {

    private DevCollect devCollector;
    
    public DragDevCollectorNode(DragDevice dragDevice) {
        super(dragDevice);
        devCollector = (DevCollect) dragDevice.getDevice();
        refreshState();
        refreshValue();
        devCollector.addOnStateChangedListener(onStateChangedListener);
        devCollector.addOnNameChangedListener(onNameChangedListener);
        devCollector.getCollectProperty().addOnCurrentValueChangedListener(onCurrentValueChangedListener);
        
        if(null == dragDevice.getImageName() || dragDevice.getImageName().isEmpty()) {
            dragDevice.setImageName("../img/yeweiji.png");
        }
        refreshImage();
    }

    public void refreshState() {
        if(!devCollector.isNormal()) {
            labelValue.setStyle("-fx-background-color : " + MyColor.DANGER);
        }else {
            labelValue.setStyle("-fx-background-color : " + MyColor.INFO);
        }
    }
    
    public void refreshValue() {
        Platform.runLater(()->labelValue.setText(devCollector.getCollectProperty().getValueWithSymbol()));
    }
    
    private OnCurrentValueChangedListener onCurrentValueChangedListener = new OnCurrentValueChangedListener() {

        @Override
        public void onCurrentValueChanged(DevCollect dev, Float value) {
            Platform.runLater(()->labelValue.setText(String.valueOf(value)));
        }
        
    };
    
    private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

        @Override
        public void onNameChanged(MyHome myHome, String name) {
            labelName.setText(name);
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
        devCollector.removeOnStateChangedListener(onStateChangedListener);
        devCollector.removeOnNameChangedListener(onNameChangedListener);
        devCollector.getCollectProperty().removeOnCurrentValueChangedListener(onCurrentValueChangedListener);
    }

}
