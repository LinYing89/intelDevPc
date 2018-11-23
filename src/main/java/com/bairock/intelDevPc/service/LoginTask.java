package com.bairock.intelDevPc.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.bairock.intelDevPc.data.DevGroupLoginResult;
import com.bairock.intelDevPc.data.LoginResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginTask extends Thread {

	private String strUrl;
	private OnExecutedListener onExecutedListener;
	
	public LoginTask(String strUrl) {
		this.strUrl = strUrl;
	}
	
	public OnExecutedListener getOnExecutedListener() {
		return onExecutedListener;
	}

	public void setOnExecutedListener(OnExecutedListener onExecutedListener) {
		this.onExecutedListener = onExecutedListener;
	}

	@Override
	public void run() {
		LoginResult loginResult = new LoginResult();
		InputStream inputStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestProperty("Charset", "UTF-8");
			urlConnection.setRequestProperty("Accept-Language", "zh-CN");
			
			urlConnection.setConnectTimeout(30000);
			urlConnection.setReadTimeout(30000);
			
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			
			int statusCode = urlConnection.getResponseCode();
			if(statusCode == 200) {
				inputStream = new BufferedInputStream(urlConnection.getInputStream());
				String response = convertStreamToString(inputStream);
				ObjectMapper mapper = new ObjectMapper();
				DevGroupLoginResult resultData = mapper.readValue(response, DevGroupLoginResult.class);
				loginResult.setData(resultData);
				if(resultData.getStateCode() != 200) {
					loginResult.setCode(404);
					loginResult.setMsg("登录失败, 用户名或密码错误");
				}
			}else {
				loginResult.setCode(statusCode);
				loginResult.setMsg("登录失败");
			}
		}catch(Exception e) {
			e.printStackTrace();
			loginResult.setCode(-1);
			loginResult.setMsg(e.getMessage());
		}finally {
			if(null != inputStream) {
				try {
					inputStream.close();
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
			if(null != urlConnection) {
				urlConnection.disconnect();
			}
		}
		if(null != onExecutedListener) {
			onExecutedListener.onExecuted(loginResult);
		}
	}
	
	private String convertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder();

        try {
            String line = reader.readLine();
            while (null != line){
                sb.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
	
	public interface OnExecutedListener{
		void onExecuted(LoginResult loginResult);
	}

	
}
