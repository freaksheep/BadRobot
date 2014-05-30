package com.freaksheep.badrobot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class TouchSensor extends Thread {

	//Handler uiHandler=null;
	private Handler  btCommHandler = null;

	private int port = -1;
	public int cont = 0;
	private boolean connected = false;	
	
	public TouchSensor(Handler btCommHandler, int port) {
		super();
		this.btCommHandler = btCommHandler;
		this.port = port;
	}



	public TouchSensor(Handler btCommHandler, int port, boolean connected) {
		super();
		this.btCommHandler = btCommHandler;
		this.port = port;
		this.connected = connected;
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.TOUCH_CONNECTION, port ,0);
		connected=true;
		while (connected ) {
			
			//System.out.println(" TouchSensor durmiendo 1 seg");
			try {
				Thread.sleep(250);
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.TOUCH_STATUS, port,0);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			//System.out.println("despertando..");
		}
		
	}
	
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		this.connected=false;
		this.interrupt();
		
	}
	
	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param String a String parameter
	 */       
	void sendBTCmessage(int delay, int message, String name) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putString("name", name);
		Message myMessage = btCommHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btCommHandler.sendMessage(myMessage);
		else
			btCommHandler.sendMessageDelayed(myMessage, delay);
	}
	
	/**
	 * Sends the message via the BTCommuncator to the robot.
	 * @param delay time to wait before sending the message.
	 * @param message the message type (as defined in BTCommucator)
	 * @param value1 first parameter
	 * @param value2 second parameter
	 */   
	void sendBTCmessage(int delay, int message, int value1, int value2) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		myBundle.putInt("value1", value1);
		myBundle.putInt("value2", value2);
		Message myMessage =  btCommHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			 btCommHandler.sendMessage(myMessage);

		else
			 btCommHandler.sendMessageDelayed(myMessage, delay);
	}

}
