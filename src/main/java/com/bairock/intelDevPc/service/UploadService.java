package com.bairock.intelDevPc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.controller.UpDownloadDialogController;
import com.bairock.intelDevPc.data.Config;
import com.bairock.iot.intelDev.data.DragDeviceHelper;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.http.HttpUploadBase;
import com.bairock.iot.intelDev.user.IntelDevHelper;

import javafx.application.Platform;

@Service
public class UploadService {

    @Autowired
    private UpDownloadDialogController upDownloadDialogController;
    @Autowired
    private Config config;
    
    public void upload() {
        upDownloadDialogController.setMessage("上传设备信息...");
        String strUrl = "http://" + config.getServerName() + "/group/client/groupUpload";
        HttpUploadBase task = new HttpUploadBase(strUrl, UserService.getDevGroup());
        task.setOnExecutedListener(loginResult -> {
            Platform.runLater(() -> uploadGroupResult(loginResult));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void uploadGroupResult(Result<Object> loginResult) {
        if (loginResult.getCode() == 0) {
            upDownloadDialogController.setMessage("上传组态设备信息...");
            uploadDragDevice();
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
    
    /**
     * 上传组态信息
     */
    private void uploadDragDevice() {
        String strUrl = "http://" + config.getServerName() + "/group/client/dragDeviceUpload";
        HttpUploadBase task = new HttpUploadBase(strUrl, DragDeviceHelper.getIns().getDragDevices());
        task.setOnExecutedListener(loginResult -> {
            Platform.runLater(() -> uploadDragDeviceResult(loginResult));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void uploadDragDeviceResult(Result<Object> loginResult) {
        if (loginResult.getCode() == 0) {
            upDownloadDialogController.setMessage("上传组态配置信息...");
            uploadDragConfig();
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
    
    /**
     * 上传组态配置
     */
    private void uploadDragConfig() {
        String strUrl = "http://" + config.getServerName() + "/group/client/dragConfigUpload";
        HttpUploadBase task = new HttpUploadBase(strUrl, Util.DRAG_CONFIG);
        task.setOnExecutedListener(loginResult -> {
            Platform.runLater(() -> uploadDragConfigResult(loginResult));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void uploadDragConfigResult(Result<Object> loginResult) {
        if (loginResult.getCode() == 0) {
            upDownloadDialogController.loadResult(true);
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
}
