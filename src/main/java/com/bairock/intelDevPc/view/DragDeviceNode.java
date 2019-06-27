package com.bairock.intelDevPc.view;

import java.io.IOException;

import com.bairock.iot.intelDev.data.DragDevice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public abstract class DragDeviceNode extends VBox{

    @FXML
    protected Label labelValue;
    @FXML
    protected Label labelName;
    @FXML
    protected ImageView image;

    protected DragDevice dragDevice;

    public DragDeviceNode(DragDevice dragDevice) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dragDeviceNode.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setLayoutX(dragDevice.getLayoutx());
        this.setLayoutY(dragDevice.getLayouty());
        this.dragDevice = dragDevice;
        labelName.setText(dragDevice.getDevice().getName());
        setImageWidthAndHeight(dragDevice.getImageWidth(), dragDevice.getImageHeight());
        setImageRotate(dragDevice.getRotate());
//        image.setRotate(10);
    }
    
    public DragDevice getDragDevice() {
        return dragDevice;
    }

    public void setDragDevice(DragDevice dragDevice) {
        this.dragDevice = dragDevice;
    }
    
    public void showName(boolean showable) {
        labelName.setVisible(showable);
//        labelName.setManaged(showable);
    }
    
    public void showIcon(boolean showable) {
        image.setVisible(showable);
    }
    
    public void setImageRotate(double rotate) {
        image.setRotate(rotate);
    }
    
    public void setImageWidthAndHeight(int width, int height) {
        setImageWidth(width);
        setImageHeight(height);
    }
    
    public void setImageWidth(int width) {
        image.setFitWidth(width);
    }
    
    public void setImageHeight(int height) {
        image.setFitHeight(height);
    }
    
    public void refreshImage() {
        try {
            if(dragDevice.getImageType().equals(DragDevice.IMG_PICTURE)) {
                image.setImage(new Image("file:///" + dragDevice.getImageName()));
            }else {
                image.setImage(new Image(dragDevice.getImageName()));
            }
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public abstract void destory();
}
