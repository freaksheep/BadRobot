package com.freaksheep.badrobot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LightSensor extends Thread {
	//Handler uiHandler=null;
		private Handler  btCommHandler = null;

		private int port=-1;
		private boolean connected=false,enabled=false;
				
				
		public LightSensor(Handler btCommHandler, int port) {
			super();
			this.btCommHandler = btCommHandler;
			this.port = port;
			this.connected=false;
			this.enabled=false;
		}


		public LightSensor(Handler btCommHandler, int port, int cont,
				boolean connected,boolean enabled) {
			super();
			this.btCommHandler = btCommHandler;
			this.port = port;
			this.connected = connected;
			this.enabled=enabled;
		}
		
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			connected=true;
			while (connected ){
				System.out.println(" lightsensor durmiendo 1 seg");
				try {
					Thread.sleep(2000);
					if (enabled){
						enabled=false;
						System.out.println(" lightsensor apagando");
						//sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.LIGHT_OFF, port,0);
					}else{
						enabled=true;
						System.out.println(" lightsensor encendiendo");
						sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.LIGHT_ON, port,0);
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				System.out.println("despertando..");
			}
			
		}
		

		public boolean isEnabled() {
			return enabled;
		}


		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}


		@Override
		public void destroy() {
			// TODO Auto-generated method stub
			
			this.connected=false;
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.LIGHT_OFF, port,0);
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
