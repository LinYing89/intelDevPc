package com.bairock.intelDevPc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.controller.UpDownloadDialogController;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.repository.DevGroupRepo;
import com.bairock.intelDevPc.repository.DragDeviceRepository;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.data.DragConfig;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.data.DragDeviceHelper;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.http.HttpDownloadBase;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.IntelDevHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;

@Service
public class DownloadService {

    @Autowired
    private DevGroupRepo devGroupRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private MainController mainController;
    
    @Autowired
    private DragDeviceRepository dragDeviceRepository;
    
    //缓存下载的数据, 下载完成后一起保存
    private DevGroup devGroup;
    private List<DragDevice> dragDevices;
    private DragConfig dragConfig;
    
    @Autowired
    private UpDownloadDialogController upDownloadDialogController;
    @Autowired
    private Config config;
    
    public void download() {
        upDownloadDialogController.setMessage("下载设备信息...");
        String strUrl = String.format("http://%s/group/client/groupDownload/%s/%s", config.getServerName(), config.getUserid(), config.getDevGroupName());
        HttpDownloadBase task = new HttpDownloadBase(strUrl);
        task.setOnExecutedListener(result -> {
            Platform.runLater(() -> downloadGroupResult(result));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void downloadGroupResult(Result<String> result) {
        if (result.getCode() == 0) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Result<DevGroup> loginResult = mapper.readValue(result.getData(), new TypeReference<Result<DevGroup>>(){});
                
                devGroup = loginResult.getData();
                upDownloadDialogController.setMessage("下载组态设备信息...");
            }catch(Exception e){
                e.printStackTrace();
                upDownloadDialogController.loadResult(false);
            }
            downloadDragDevice();
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
    
    /**
     * 下载组态信息
     */
    private void downloadDragDevice() {
        String strUrl = String.format("http://%s/group/client/dragDeviceDownload/%s/%s", config.getServerName(), config.getUserid(), config.getDevGroupName());
        HttpDownloadBase task = new HttpDownloadBase(strUrl);
        task.setOnExecutedListener(result -> {
            Platform.runLater(() -> downloadDragDeviceResult(result));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void downloadDragDeviceResult(Result<String> result) {
        if (result.getCode() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            Result<List<DragDevice>> loginResult;
            try {
                loginResult = mapper.readValue(result.getData(), new TypeReference<Result<List<DragDevice>>>(){});
                dragDevices = loginResult.getData();
                if(null == dragDevices) {
                    upDownloadDialogController.loadResult(false);
                }
                upDownloadDialogController.setMessage("下载组态配置信息...");
                downloadDragConfig();
            } catch (IOException e) {
                e.printStackTrace();
                upDownloadDialogController.loadResult(false);
            }
            
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
    
    /**
     * 上传组态配置
     */
    private void downloadDragConfig() {
        String strUrl = String.format("http://%s/group/client/dragConfigDownload/%s/%s", config.getServerName(), config.getUserid(), config.getDevGroupName());
        HttpDownloadBase task = new HttpDownloadBase(strUrl);
        task.setOnExecutedListener(result -> {
            Platform.runLater(() -> downloadDragConfigResult(result));
        });
        IntelDevHelper.executeThread(task);
    }
    
    private void downloadDragConfigResult(Result<String> result) {
        if (result.getCode() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            Result<DragConfig> loginResult;
            try {
                loginResult = mapper.readValue(result.getData(), new TypeReference<Result<DragConfig>>(){});
                dragConfig = loginResult.getData();
                downloadComplete();
                upDownloadDialogController.loadResult(true);
            } catch (IOException e) {
                e.printStackTrace();
                upDownloadDialogController.loadResult(false);
            }
        } else {
            upDownloadDialogController.loadResult(false);
        }
    }
    
    private void downloadComplete() {
        saveDevGroup();
        saveDragDevice();
        saveDragConfig();
        
        userService.reloadDevGroup(UserService.getDevGroup());
        mainController.reInit();
        // 关掉所有设备链接
        DevChannelBridgeHelper.getIns().closeAllBridge();

        DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
        PadClient.getIns().sendUserInfo();
    }
    
    private void saveDevGroup() {
        DevGroup groupDb = UserService.getDevGroup();
        DevGroup groupDownload = devGroup;

        DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();

        List<Device> listOldDevice = new ArrayList<>(groupDb.getListDevice());

//  groupDb.getListDevice().clear();
        for (Device dev : listOldDevice) {
            groupDb.removeDevice(dev);
        }
        for (Device dev : groupDownload.getListDevice()) {
            groupDb.addDevice(dev);
        }
//  groupDb.getListDevice().addAll(groupUpload.getListDevice());
//  groupDb.getListLinkageHolder().clear();
        List<LinkageHolder> listOldLinkageHolder = new ArrayList<>(groupDb.getListLinkageHolder());
        for (LinkageHolder h : listOldLinkageHolder) {
            h.setDevGroup(null);
            groupDb.getListLinkageHolder().remove(h);
        }
        for (LinkageHolder h : groupDownload.getListLinkageHolder()) {
            h.setDevGroup(groupDb);
            groupDb.getListLinkageHolder().add(h);
        }
//  groupDb.getListLinkageHolder().addAll(groupDownload.getListLinkageHolder());
        devGroupRepo.saveAndFlush(groupDb);
    }

    private void saveDragDevice() {
        for(DragDevice dg : DragDeviceHelper.getIns().getDragDevices()) {
            dragDeviceRepository.deleteById(dg.getId());
        }
        for(DragDevice dg : dragDevices) {
            dragDeviceRepository.saveAndFlush(dg);
        }
    }
    
    private void saveDragConfig() {
        if(null == dragConfig) {
            return;
        }
        Util.DRAG_CONFIG.setDevGroupId(dragConfig.getDevGroupId());
        Util.DRAG_CONFIG.setDragBackgroundHeight(dragConfig.getDragBackgroundHeight());
        Util.DRAG_CONFIG.setDragBackgroundWidth(dragConfig.getDragBackgroundWidth());
        Util.DRAG_CONFIG.setDragViewBackgroundImagePath(dragConfig.getDragViewBackgroundImagePath());
        Util.saveDragConfig();
    }
}
