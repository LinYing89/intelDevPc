package com.bairock.intelDevPc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.MainStateView;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.LoginResult;
import com.bairock.intelDevPc.service.LoginTask;
import com.bairock.intelDevPc.service.UserService;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
	private Label labelWaring;
	
	@Autowired
	MainStateView mainStateView;
	@Autowired
	private Config config;
	@Autowired
	private UserService userService;
	
	public void init() {
		labelAppName.setText(config.getAppTitle());
		txtUserName.setText(UserService.user.getName());
		txtGroupName.setText(UserService.user.getListDevGroup().get(0).getName());
		txtGroupPsd.setText(UserService.user.getListDevGroup().get(0).getPsd());
	}
	
//	@Autowired
//	private UserService userService;
	
//	private String serverIp = "192.168.1.112";
	//登录
	public void login() {
		System.out.println("login");
		
		labelWaring.setText("正在登录...");
		gridPane.setDisable(true);
		
		String userName = txtUserName.getText();
		String groupName = txtGroupName.getText();
		String groupPsd = txtGroupPsd.getText();
		String url = String.format("http://%s/hamaSer/ClientLoginServlet?name=%s&group=%s&psd=%s", config.getServerName(), userName, groupName, groupPsd);
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
	
	
	private void loginResult(LoginResult result){
		gridPane.setDisable(false);
		if(result.getCode() != 0) {
			logger.error("登录失败");
			labelWaring.setText(result.getMsg());
		}else {
			logger.error("登录成功");
			labelWaring.setText("登录成功");
			logger.info("upDownloadPort: " + result.getData().getUpDownloadPort());
			logger.info("padPort: " + result.getData().getPadPort());
			config.setUpDownloadPort(result.getData().getUpDownloadPort());
			config.setPadPort(result.getData().getPadPort());
			UserService.user.setName(txtUserName.getText());
			UserService.getDevGroup().setName(txtGroupName.getText());
			UserService.getDevGroup().setPsd(txtGroupPsd.getText());
			userService.update(UserService.user);
//			userService.initUser();
			PadClient.getIns().closeHandler();
			
			//显示主窗口
			IntelDevPcApplication.showView(MainStateView.class);
			//窗口最大化
//			IntelDevPcApplication.getStage().setMaximized(true);

			((MainController)mainStateView.getPresenter()).init();
		}
	}
	
	public void loginLocal() {
		System.out.println("loginLocal");
		IntelDevPcApplication.showView(MainStateView.class);
		//窗口最大化
//		IntelDevPcApplication.getStage().setMaximized(true);

		((MainController)mainStateView.getPresenter()).init();
	}
	
	public void cbAutoLogin() {
		System.out.println("cbAutoLogin");
	}
}
