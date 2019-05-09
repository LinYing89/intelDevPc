package com.bairock.intelDevPc.controller;

import java.io.File;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.repository.DragDeviceRepository;
import com.bairock.intelDevPc.view.ChoiceIconView;
import com.bairock.intelDevPc.view.DragDevCollectorNode;
import com.bairock.intelDevPc.view.DragDevSwitchNode;
import com.bairock.intelDevPc.view.DragDeviceNode;
import com.bairock.intelDevPc.view.DragDeviceView;
import com.bairock.intelDevPc.view.NumberTextField;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.data.DragDeviceHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.order.OrderType;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class DragDeviceCtrler {

    @Autowired
    private DragDeviceRepository dragDeviceRepository;
    @Autowired
    private DragDeviceView dragDeviceView;

    private double dragBeginLayoutX;
    private double dragBeginLayoutY;
    //右键点击的图标, 保存以备更改图标
    private DragDeviceNode clickedDragDeviceNode;

    // 是否拖拽了, 是的话鼠标离开控件后保存位置
    private boolean dragged = false;
    @FXML
    private ToggleButton toggleBtnReplace;
    @FXML
    private Pane paneBackground;
    @FXML
    private ImageView imgBackground;

    private boolean inited = false;

    private void init1() {
        if (!inited) {
            inited = true;
            toggleBtnReplace.selectedProperty().addListener((p0, p1, p2) -> {
                if (p2) {
                    toggleBtnReplace.setText("结束调整");
                } else {
                    toggleBtnReplace.setText("调整位置");
                }
            });
            imgBackground.setImage(new Image("file:///" + Util.DRAG_CONFIG.getDragViewBackgroundImagePath()));
            imgBackground.setFitWidth(Util.DRAG_CONFIG.getDragBackgroundWidth());
            imgBackground.setFitHeight(Util.DRAG_CONFIG.getDragBackgroundHeight());
        }
    }

    public void init() {
        init1();

        Iterator<Node> it = paneBackground.getChildren().iterator();
        while (it.hasNext()) {
            Node n = it.next();
            if (n instanceof DragDeviceNode) {
                ((DragDeviceNode) n).destory();
                it.remove();
            }
        }
        for (DragDevice dg : DragDeviceHelper.getIns().getDragDevices()) {
            DragDeviceNode ds = null;
            if (dg.getDevice() instanceof IStateDev) {
                ds = new DragDevSwitchNode(dg);
            } else {
                ds = new DragDevCollectorNode(dg);
            }
            ds.setDragDevice(dg);
            paneBackground.getChildren().add(ds);
            setDragListener(ds);
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuChangeIcon = new MenuItem("选择系统图标");
            MenuItem menuChangePic = new MenuItem("选择本地图片");
            menuChangeIcon.setOnAction(e -> {
                clickedDragDeviceNode.getDragDevice().setImageType(DragDevice.IMG_ICON);
                IntelDevPcApplication.showView(ChoiceIconView.class, Modality.WINDOW_MODAL);
                if(null != ChoiceIconController.iconPath) {
                    clickedDragDeviceNode.getDragDevice().setImageName(ChoiceIconController.iconPath);
                    clickedDragDeviceNode.refreshImage();
                    dragDeviceRepository.saveAndFlush(clickedDragDeviceNode.getDragDevice());
                    ChoiceIconController.iconPath = null;
                }
            });
            menuChangePic.setOnAction(e -> {
                clickedDragDeviceNode.getDragDevice().setImageType(DragDevice.IMG_PICTURE);
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择图片");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
                        new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
                File file = fileChooser.showOpenDialog(dragDeviceView.getView().getScene().getWindow());
                if (file != null) {
                    long length = file.length();
                    //文件不得大于200KB
                    if(length > 1024 * 200) {
                        Alert warning = new Alert(Alert.AlertType.WARNING, "文件不得大于200KB!");
                        warning.showAndWait();
                        return;
                    }
                    String path = file.getAbsolutePath();
                    clickedDragDeviceNode.getDragDevice().setImageName(path);
                    clickedDragDeviceNode.refreshImage();
                    dragDeviceRepository.saveAndFlush(clickedDragDeviceNode.getDragDevice());
                }
            });
            contextMenu.getItems().add(menuChangeIcon);
            contextMenu.getItems().add(menuChangePic);
            ds.setOnContextMenuRequested(v -> {
                contextMenu.show(imgBackground, v.getScreenX(), v.getScreenY());
            });
        }
    }

    private void setDragListener(DragDeviceNode ds) {
        ds.setOnMousePressed(mouseEvent -> {
            dragBeginLayoutX = mouseEvent.getSceneX() - ds.getLayoutX();
            dragBeginLayoutY = mouseEvent.getSceneY() - ds.getLayoutY();
        });
        ds.setOnMouseDragged(mouseEvent -> {
            if (!toggleBtnReplace.isSelected()) {
                return;
            }
            dragged = true;
            double x = mouseEvent.getSceneX() - dragBeginLayoutX;
            double y = mouseEvent.getSceneY() - dragBeginLayoutY;
            x = x < 0 ? 0 : x;
            y = y < 0 ? 0 : y;
            ds.getDragDevice().setLayoutx((int) x);
            ds.getDragDevice().setLayouty((int) y);
            ds.setLayoutX(x);
            ds.setLayoutY(y);
        });
        ds.setOnMouseClicked(e -> {
            if(dragged) {
                return;
            }
            clickedDragDeviceNode = ds;
            System.out.println("setOnMouseClicked" + ds.getDragDevice().getDeviceId());
            if(ds.getDragDevice().getDevice() instanceof IStateDev) {
                Device devSwitch = ds.getDragDevice().getDevice();
                IStateDev iStateDev = (IStateDev)devSwitch;
                
                if(devSwitch.isKaiState()) {
                    IntelDevPcApplication.sendOrder(devSwitch, iStateDev.getTurnOffOrder(), OrderType.CTRL_DEV, true);
                    devSwitch.setGear(Gear.GUAN);
                }else {
                    IntelDevPcApplication.sendOrder(devSwitch, iStateDev.getTurnOnOrder(), OrderType.CTRL_DEV, true);
                    devSwitch.setGear(Gear.KAI);
                }
                //向服务器发送档位, 不能在监听器中发送, 因为如果是远程登录, 当收到服务器档位改变后不可以再向服务器发送
                //宫格中, 列表中, 属性界面中三个地方一致处理
                String gearOrder = IntelDevPcApplication.createDeviceOrder(devSwitch, OrderType.GEAR, devSwitch.getGear().toString());
                PadClient.getIns().send(gearOrder);
            }
        });
        ds.setOnMouseExited(e -> {
            if (toggleBtnReplace.isSelected()) {
                if (dragged) {
                    dragged = false;
                    System.out.println("setOnMouseExited");
                    dragDeviceRepository.saveAndFlush(ds.getDragDevice());
                }
            }
        });
    }

    // 更换背景
    @FXML
    public void onChangeBackgroundAction() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = fileChooser.showOpenDialog(dragDeviceView.getView().getScene().getWindow());
        if (file != null) {
            String path = file.getAbsolutePath();
            System.out.println(path);
            Platform.runLater(() -> imgBackground.setImage(new Image("file:///" + path)));
            Util.DRAG_CONFIG.setDragViewBackgroundImagePath(path);
            Util.saveDragConfig();
        }
    }

    @FXML
    public void onBackgroundSize() {
        Stage state = new Stage();
        state.setTitle("背景图尺寸");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // 创建Label对象，放到第0列，第0行
        Label labelWidth = new Label("宽度:");
        grid.add(labelWidth, 0, 0);
        // 创建文本输入框，放到第1列，第0行
        NumberTextField txtWidth = new NumberTextField();
        txtWidth.setText(String.valueOf(Util.DRAG_CONFIG.getDragBackgroundWidth()));
        grid.add(txtWidth, 1, 0);

        Label labelHight = new Label("高度:");
        grid.add(labelHight, 0, 1);
        NumberTextField txtHight = new NumberTextField();
        txtHight.setText(String.valueOf(Util.DRAG_CONFIG.getDragBackgroundHeight()));
        grid.add(txtHight, 1, 1);

        Label labelWarning = new Label();
        grid.add(labelWarning, 1, 2);

        Button btn = new Button("保存");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);// 将按钮控件作为子节点
        btn.setOnAction(e -> {
            String strWidth = txtWidth.getText();
            String strHight = txtHight.getText();
            if (strWidth.isEmpty() || strHight.isEmpty()) {
                labelWarning.setText("输入不可为空!");
                return;
            }
            try {
                Util.DRAG_CONFIG.setDragBackgroundWidth(Integer.parseInt(strWidth));
                Util.DRAG_CONFIG.setDragBackgroundHeight(Integer.parseInt(strHight));
            } catch (Exception ex) {
                labelWarning.setText("输入只能为数字!");
                return;
            }
            imgBackground.setFitWidth(Util.DRAG_CONFIG.getDragBackgroundWidth());
            imgBackground.setFitHeight(Util.DRAG_CONFIG.getDragBackgroundHeight());
            Util.saveDragConfig();
            state.close();
        });
        grid.add(hbBtn, 1, 4);// 将HBox pane放到grid中的第1列，第4行

        Scene scene = new Scene(grid, 300, 275);
        state.setScene(scene);

        state.showAndWait();
    }

}
