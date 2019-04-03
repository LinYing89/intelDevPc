package com.bairock.intelDevPc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.MainStateView;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.repository.ConfigRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.data.DevGroupLoginResult;
import com.bairock.iot.intelDev.data.Result;
import com.bairock.iot.intelDev.http.LoginTask;
import com.bairock.iot.intelDev.order.LoginModel;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@FXMLController
public class LoginController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@FXML
	private Label labelAppName;
	@FXML
	private GridPane gridPane;
	@FXML
	private TextField txtUserName;
	@FXML
	private TextField txtGroupName;
	@FXML
	private TextField txtGroupPsd;
	@FXML
	private CheckBox cbAutoLogin;
	@FXML
	private Label labelWaring;
	
	@Autowired
	private MainStateView mainStateView;
	@Autowired
	private Config config;
	@Autowired
	private ConfigRepository configRepository;
	@Autowired
	private UserService userService;
	
	public void init() {
		if(config.isAutoLogin()) {
			Util.GUEST = false;
			showMain();
		}
		
		cbAutoLogin.setSelected(false);
		
		labelAppName.setText(config.getAppTitle());
		txtUserName.setText(UserService.user.getName());
		txtGroupName.setText(UserService.user.getListDevGroup().get(0).getName());
		txtGroupPsd.setText(UserService.user.getListDevGroup().get(0).getPsd());
//		mainStateView.getView().getScene().getWindow().setOnCloseRequest(e -> handlerExit());
	}
	
	private void login(String loginModel) {

		labelWaring.setText("正在登录...");
		gridPane.setDisable(true);
		
		config.setLoginModel(loginModel);
		configRepository.saveAndFlush(config);
		
		String userName = txtUserName.getText();
		String groupName = txtGroupName.getText();
		String groupPsd = txtGroupPsd.getText();
//		String url = String.format("http://%s/hamaSer/ClientLoginServlet?name=%s&group=%s&psd=%s", config.getServerName(), userName, groupName, groupPsd);
		String url = String.format("http://%s/group/client/devGroupLogin/%s/%s/%s/%s", config.getServerName(), loginModel, userName, groupName, groupPsd);
		//开启线程登录
		LoginTask loginTask = new LoginTask(url);
		loginTask.setOnExecutedListener(loginResult ->{
			Platform.runLater(()->loginResult(loginResult));
		});
		loginTask.start();
		
//		userService.initUser();
//		IntelDevPcApplication.showView(MainStateView.class);
//		((MainController)mainStateView.getPresenter()).init();
	
	}
	
	//远程登录
	@FXML
	public void onLoginRemoteAction() {
		login(LoginModel.REMOTE);
	}

	//本地登录
	@FXML
	public void onLoginLocalAction() {
		login(LoginModel.LOCAL);
	}
	
	
	private void loginResult(Result<DevGroupLoginResult> result){
		gridPane.setDisable(false);
		if(result.getCode() != 0) {
			logger.error("登录失败");
			labelWaring.setText(result.getMsg());
		}else {
			Util.GUEST = false;
			logger.error("登录成功");
			labelWaring.setText("登录成功");
			logger.info("padPort: " + result.getData().getPadPort());
			config.setPadPort(result.getData().getPadPort());
			config.setDevPort(result.getData().getDevPort());
			UserService.user.setName(txtUserName.getText());
			UserService.getDevGroup().setName(txtGroupName.getText());
			UserService.getDevGroup().setPetName(result.getData().getDevGroupPetName());
			UserService.getDevGroup().setPsd(txtGroupPsd.getText());
			userService.update(UserService.user);
//			userService.initUser();
			PadClient.getIns().closeHandler();
			
			//显示主窗口
			showMain();
		}
	}
	
	public void loginLocal() {
		Util.GUEST = true;
		showMain();
	}
	
	private void showMain() {
		IntelDevPcApplication.showView(MainStateView.class);
		//窗口最大化
		IntelDevPcApplication.getStage().setMaximized(true);

		((MainController)mainStateView.getPresenter()).init();
	}
	
	public void cbAutoLoginAction() {
		config.setAutoLogin(true);
		configRepository.saveAndFlush(config);
	}
}
