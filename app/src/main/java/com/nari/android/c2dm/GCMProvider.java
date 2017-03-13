package com.nari.android.c2dm;

import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;

import android.util.Log;

import com.nari.util.AllConfig;

public class GCMProvider extends Thread {

	/* 이건 내가 등록한 Token 인디 */
	String deviceToken = null;
	private String upgbn = null;
	private String mesg = null;
	private String iDate = null ;
	private String iTime = null ;
	
	String TAG = "GCMProvider";
	
	private int badge = 0;
	private String phone_no = AllConfig.PHONE_NUMBER;
 
	 public GCMProvider(String deviceToken, String upgbn, String mesg, int badge, String phone_no, String iDate, String iTime) {
	  this.upgbn = upgbn ; 
	  this.deviceToken = deviceToken;
	  this.mesg = mesg;
	  this.badge = badge;
	  this.phone_no = phone_no;
	  this.iDate = iDate ;
	  this.iTime = iTime ;

	 }

	 public void run() {
		  try {
			  sender(AllConfig.ServerKey);
		   
		  }catch (Exception e) {
			  Log.d(TAG,"Error:" + e.toString());
		  }
	 }

	 public void sender(String ServerKey) throws Exception{
	  
		  String rdata = "" ;
		  
		  rdata += URLEncoder.encode("registration_id", "UTF-8") + "=" + URLEncoder.encode(this.deviceToken, "UTF-8");
		  rdata += "&" + URLEncoder.encode("data.upgbn", "UTF-8") + "=" + URLEncoder.encode(this.upgbn, "UTF-8");
		  rdata += "&" + URLEncoder.encode("data.msg", "UTF-8") + "=" + URLEncoder.encode(this.mesg, "UTF-8"); 
		  rdata += "&" + URLEncoder.encode("data.phone", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.phone_no), "UTF-8"); 
		  rdata += "&" + URLEncoder.encode("data.date", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.iDate), "UTF-8"); 
		  rdata += "&" + URLEncoder.encode("data.time", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.iTime), "UTF-8"); 
		  
		  URL url = new URL(AllConfig.GCM_SEND_URL);
		  
		  HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		  conn.setDoOutput(true);
		  conn.setUseCaches(false);
		  conn.setRequestMethod("POST");
		  conn.setRequestProperty("Authorization", "key=" + ServerKey);
		  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		  OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		  
		  wr.write(rdata);
		  wr.flush();
		  wr.close();
		  
		  int responseCode = conn.getResponseCode() ;
		
		//if (responseCode == HttpServletResponse.SC_UNAUTHORIZED)
		Log.d(TAG,"responseCode ===>>>[" + responseCode + "]" ) ;
		
		String updateAuthToken = conn.getHeaderField("Update-Client-Auth");
		if (updateAuthToken != null && !ServerKey.equals(updateAuthToken)) {
			Log.d(TAG,"Got updated auth token from datamessaging servers :" + updateAuthToken) ;
		}
		
		String responseLine = new BufferedReader(new InputStreamReader(conn.getInputStream()))
				.readLine() ;
		Log.d(TAG,"responseCode ===>>>[" + responseCode + "][" + responseLine + "]") ;
		
		if (responseLine == null || responseLine.equals("")) {
			Log.d(TAG,"responeCode : " + responseCode + "]" ) ;
			throw new IOException ("Got empty response from google AC2DM endpoint.") ;
		}
		
		String[] responseParts = responseLine.split("=", 2) ;
		if (responseParts.length != 2) {
			Log.d(TAG,"Invalid message from google : " + responseCode + "][" + responseLine + "][" + String.valueOf( responseParts.length ) + "]") ;
			throw new IOException ("Invalid message from google : " + responseCode + "][" + responseLine) ;
		}
		
		if (responseParts[0].equals("id")) {
			Log.d(TAG,"Successfully sent data messages to device : " + responseLine ) ;
		}
		
		if (responseParts[0].equals("Error")) {
			String err = responseParts[1] ;
			Log.d(TAG,"Got error response from google datamessaging endpoint : " + err ) ;
			throw new IOException ("Server Error : " + responseCode + "][" + err) ;
		}
		
	 }
	 
}

