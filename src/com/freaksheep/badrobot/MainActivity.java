package com.freaksheep.badrobot;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements MainFragment.OnFragmentInteractionListener,CommandControllerFragment.OnFragmentInteractionListener, OnSharedPreferenceChangeListener {

	// TAG DEBUG
	private static final String TAG_BT = "BLUETOOTH";

	//intent tags
	private static final int BT_REQUEST_ENABLE = 0;
	private static final int DEVICE_SELECTED = 1;
	private static  int MOTOR_AUX = 2;
	private static  int MOTOR_LEFT = 1;
	private static  int MOTOR_RIGHT = 0;


	// Setting vars
	private static int INIT_MODE = 0; //0 = ask user to enable , 1 = enable without ask .
	private static String DEFAULT_LAYOUT = "1";
	private static int MAXSPEED=50;
	// Bluetooth
	private boolean isBtOnOutside; 
	private boolean isBtOnByUs; 
	private boolean isBtActive;
	private BluetoothAdapter btAdapter;
	private boolean firstTime ;

	//Conection vars

	private BTCommunicator myBTCommunicator;
	private String targetMAC;
	private boolean connected;
	private Handler btcHandler;
	private MenuItem disconnectBtnM;
	Vibrator mVibrator;
	//Sensors movement, etc, 
	private acController mAcController;
	private SensorManager mSensorManager;
	private boolean sensorActive = false;
	int motorLeft = 0;
	int motorRight = 0;
	int leftPrev = 0;
	int rightPrev = 0;
	int frontPrev = 0;
	int backPrev = 0;
	private boolean isAccActive=false;



	TouchSensor mtouchSensor1,mtouchSensor2;
	UltraSonicSensor mUltraSonicSensor;
	int previousTouchState = 0;
	private boolean wasConnected=false;

	LightSensor mLightSensor1;
	//Messages to the user
	private Toast reusableToast;

	// Layout objects

	private SharedPreferences sPreferences;
	private Fragment reusableFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mVibrator.vibrate(250);

		sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sPreferences.registerOnSharedPreferenceChangeListener(this);
		INIT_MODE=(sPreferences.getBoolean("bluetooth", true))?0:1;
		DEFAULT_LAYOUT = sPreferences.getString("layouts", DEFAULT_LAYOUT);
		Log.d(TAG_BT, DEFAULT_LAYOUT);
		firstTime=true;
		registerReceivers();
		//KEEP SCREEN ON
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//init the toast
		reusableToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);



		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container,MainFragment.newInstance("", "")).commit();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		// no bluetooth available
		if (BluetoothAdapter.getDefaultAdapter()==null) {
			showToast(R.string.bt_initialization_failure, Toast.LENGTH_LONG);
			//destroyBTCommunicator();
			finish();
			return;
		}else{
			if (firstTime){
				firstTime=false;
				bluetoothInit(INIT_MODE);
			}

		}  	       
	}


	//1.5.1
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d(TAG_BT,"en onRestart");
		//showVarsLog();
		if (wasConnected){
			startBTCommunicator(targetMAC);
		}
	}

	//2
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG_BT,"en onRestoreInstanceState");
		//showVarsLog();


	}

	//3
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG_BT,"en onResume");
		//		showVarsLog();

	}

	//4
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.d(TAG_BT,"en onSaveInstanceState");
		//showVarsLog();

	}

	//5
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG_BT,"en onPause");
		//showVarsLog();
		if (mtouchSensor1!=null){
			mtouchSensor1.destroy();
			mtouchSensor1=null;
		}
		
		if (mtouchSensor2!=null){
			mtouchSensor1.destroy();
			mtouchSensor2=null;
		}
		if (mUltraSonicSensor!=null){
			mUltraSonicSensor.destroy();
			mUltraSonicSensor=null;
		}
		if (mAcController!=null){
			mSensorManager.unregisterListener(acControllerListener);
			mAcController=null;
		}
		//mSensorManager.unregisterListener(acControllerListener);
		destroyBTCommunicator();

	}

	//5.5
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG_BT,"en onStop");
		//showVarsLog();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG_BT, "en ondestroy");

		
		unRegisterReceivers();
		stopBluetooth();
		btAdapter=null;
		sPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	/**
	 * Displays a message as a toast
	 * @param textToShow the message
	 * @param length the length of the toast to display
	 */
	private void showToast(String textToShow, int length) {
		reusableToast.setText(textToShow);
		reusableToast.setDuration(length);
		reusableToast.show();
	}

	/**
	 * Displays a message as a toast
	 * @param resID the ressource ID to display
	 * @param length the length of the toast to display
	 */
	private void showToast(int resID, int length) {
		reusableToast.setText(resID);
		reusableToast.setDuration(length);
		reusableToast.show();
	}


	private void bluetoothInit(int mode){
		Log.d(TAG_BT,"en bluetoothInit Func");

		if (btAdapter==null){
			btAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		Log.d(TAG_BT,"the init mode actually is "+mode);
		//miro si esta activo o no al principio
		//showToast(""+firstTime, Toast.LENGTH_SHORT);

		isBtActive=( isBtOnOutside=checkEnabledOut())?true:false;


		//showVarsLog();
		if (!isBtOnOutside && !isBtActive){
			switch (mode) {

			case 0:
				Log.d(TAG_BT,"I enable BT by intent");
				startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_ENABLE);
				break;
			case 1:
				try {
					Log.d(TAG_BT,"I enable without intent");
					btAdapter.enable();
					isBtOnByUs=true;
					isBtActive= true;

				} catch (Exception e) {
					Log.d(TAG_BT," "+e.getMessage());

				}

				break;

			default:
				break;
			}
		}else{
			Log.d(TAG_BT,"en bluetoothInit Func in Else, it's enabled");
		}
		//updateButtons();
	}

	private void stopBluetooth(){
		//si no esta habilitado de fuera, lo finalizo yo.
		Log.d(TAG_BT,"en stopBluetooth Func");
		if (!isBtOnOutside ){

			if (isBtActive){
				Log.d(TAG_BT,"not enabled outside, i kill it...");
				try {
					btAdapter.disable();

				} catch (Exception e) {
					Log.d(TAG_BT,e.getMessage());
				}finally{
					isBtActive=btAdapter.isEnabled();
				}

			}


		}else{
			Log.d(TAG_BT,"Enabled outside.. let's keep it opened.. ");
		}
		//updateButtons();
	}

	private boolean checkEnabledOut(){
		if (btAdapter.isEnabled()){
			//Log.d(TAG_BT,"isBtOnOutside "+isBtOnOutside);
			return true;
		}else{
			//Log.d(TAG_BT,"isBtOnOutside "+isBtOnOutside);
			return false;
		}
	}

	public void registerReceivers(){
		//broadcastreceiver para controlar los distintos estados del bluetooth. 

		if (!bluetoothState.isOrderedBroadcast()){
			Log.d(TAG_BT,"registering bluetooth receiver");
			registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}

		//registerReceiver(discoveryResult,new IntentFilter(BluetoothDevice.ACTION_FOUND));

		//registerReceiver (discoveryFinished,new IntentFilter (BluetoothAdapter.ACTION_DISCOVERY_FINISHED));


	}

	private BroadcastReceiver bluetoothState= new BroadcastReceiver() {
		//this receiver manages all the states 
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
			String stateExtra= BluetoothAdapter.EXTRA_STATE;
			String res="";
			int state = intent.getIntExtra(stateExtra,-1);
			int previousState=intent.getIntExtra(prevStateExtra, -1);

			switch (state) {
			case BluetoothAdapter.STATE_TURNING_ON:
				res="bt is turning on";
				break;
			case BluetoothAdapter.STATE_ON:
				res="bt is on now!";
				isBtActive=true;
				break;
			case BluetoothAdapter.STATE_DISCONNECTING:
				res="bt is disconecting";
				break;
			case BluetoothAdapter.STATE_DISCONNECTED:
				res="bt is disconected";
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				res="bt is turning off";
				isBtActive=false;
				break;
			case BluetoothAdapter.STATE_OFF:
				res="bt is off now!";
				break;
			case BluetoothAdapter.STATE_CONNECTED:
				res="bt is connected!";
				break;
			case BluetoothAdapter.STATE_CONNECTING:
				res="bt is connecting!";
				break;
			default:
				break;
			}
			Log.d(TAG_BT,res);
			showToast( res, Toast.LENGTH_SHORT);
			//updateButtons();
		}

	};

	public void unRegisterReceivers(){
		if (bluetoothState!=null){
			Log.d(TAG_BT, "unregistering btreceiver");
			unregisterReceiver(bluetoothState);
		}
		if (mAcController!=null){
			mSensorManager.unregisterListener(acControllerListener);
			mAcController=null;
		}

		//unregisterReceiver(discoveryFinished);
		//unregisterReceiver(discoveryResult);

	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG_BT,"en onActivityResult");
		switch (requestCode){

		case BT_REQUEST_ENABLE:
			switch (resultCode){
			case RESULT_OK: 
				Log.d(TAG_BT,"bt enabled with Intent");
				isBtOnByUs=true;
				isBtActive=true;
				break;
			case RESULT_CANCELED:
				Log.d(TAG_BT, "bt disabled by user");
				isBtOnByUs=false;
				isBtActive=false;
				Toast.makeText(this, "Exiting, enabling bluetooth denied", Toast.LENGTH_LONG).show();
				finish();
				break;
			}
		}
		//updateButtons();
	}

	/**
	 * Creates a new object for communication to the NXT robot via bluetooth and fetches the corresponding handler.
	 */
	private void createBTCommunicator() {
		// interestingly BT adapter needs to be obtained by the UI thread - so we pass it in in the constructor
		myBTCommunicator = new BTCommunicator( myHandler, BluetoothAdapter.getDefaultAdapter(), getResources());
		btcHandler = myBTCommunicator.getHandler();
	}

	/**
	 * Creates and starts the a thread for communication via bluetooth to the NXT robot.
	 * @param mac_address The MAC address of the NXT robot.
	 */
	private void startBTCommunicator(String mac_address) {
		connected = false;        
		//connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);

		if (myBTCommunicator != null) {
			try {
				myBTCommunicator.destroyNXTconnection();
			}
			catch (IOException e) { }
		}
		createBTCommunicator();
		myBTCommunicator.setMACAddress(mac_address);
		myBTCommunicator.start();
		//updateButtons();
	}

	/**
	 * Sends a message for disconnecting to the communcation thread.
	 */
	public void destroyBTCommunicator() {

		
		if (myBTCommunicator != null) {
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DISCONNECT, 0, 0);
			myBTCommunicator = null;
		}
		
		connected = false;
		//updateButtons();
		
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
		Message myMessage = myHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);

		else
			btcHandler.sendMessageDelayed(myMessage, delay);
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
		Message myMessage = myHandler.obtainMessage();
		myMessage.setData(myBundle);

		if (delay == 0)
			btcHandler.sendMessage(myMessage);
		else
			btcHandler.sendMessageDelayed(myMessage, delay);
	}

	private void createAcController(){

		if (mAcController == null){
			mSensorManager= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			mAcController= new acController(mSensorManager);
			mSensorManager.registerListener(acControllerListener, mAcController.getmSensor(), 0);
		}else{
			mSensorManager.registerListener(acControllerListener, mAcController.getmSensor(), 0);
		}


	}

	private SensorEventListener acControllerListener =new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			//Log.d(TAG_BT, "Accelerometer :onSensorChanged : ");
			// TODO Auto-generated method stub

			int[] vals= mAcController.calcVelocity(event.values[0], event.values[1]);

			//			Log.d(TAG_BT,"front: "+vals[0]);
			//			Log.d(TAG_BT,"back : "+vals[1]); 
			//			Log.d(TAG_BT,"left : "+vals[2]);
			//			Log.d(TAG_BT,"right: "+vals[3]);

			int frontBack = vals[0] - vals[1];
			int leftRight = (!DEFAULT_LAYOUT.equals("2"))? (vals[2] - vals[3]): 0;

			//Log.d(TAG_BT,"frontBack: "+frontBack);
			//Log.d(TAG_BT,"leftRight : "+leftRight);

			motorLeft = frontBack;
			motorRight = frontBack;

			boolean quiet = (frontBack==0&&leftRight==0)?true:false;
			//Log.d(TAG_BT,DEFAULT_LAYOUT);
			if (DEFAULT_LAYOUT == "2"){
				leftRight=0;
				quiet = (frontBack==0)?true:false;
				if(!quiet){
					if(vals[0] != frontPrev || vals[1] != backPrev ){
						Log.d(TAG_BT,DEFAULT_LAYOUT);

						if (!isAccActive){
							updateMotorControl(motorLeft, motorRight);
						}

					}else{
						//como se esta mandando la misma señal que antes no mando nada.. 
					}

					frontPrev = vals[0];
					backPrev= vals[1];
				}else{

				}
			}else{
				if(!quiet){
					if(vals[0] != frontPrev || vals[1] != backPrev || vals[2] != leftPrev || vals[3] != rightPrev){
						if (leftRight > 0) {
							motorLeft = ( frontBack > 0 ) ? Math.round(motorLeft*0.7F) - Math.round(vals[2]*0.3F) : Math.round(motorLeft*0.7F) + Math.round(vals[2]*0.3F);
						} else {
							motorRight = ( frontBack > 0 ) ? Math.round(motorRight*0.7F) - Math.round(vals[3]*0.3F) : Math.round(motorRight*0.7F) + Math.round(vals[3]*0.3F);
						}
						if (frontBack==0){
							motorLeft = vals[3];
							motorRight = vals[2];
						}
						if (leftRight==0){
							motorLeft = frontBack;
							motorRight = frontBack;
						}
						motorLeft=Math.round((motorLeft*MAXSPEED)/100);
						motorRight=Math.round((motorRight*MAXSPEED)/100);
						updateMotorControl(motorLeft, motorRight);
					}else{
						//como se esta mandando la misma señal que antes no mando nada.. 
					}

					frontPrev = vals[0];
					backPrev= vals[1];
					leftPrev =vals[2];
					rightPrev=vals[3];
				}else{
					updateMotorControl(0,0);
				}
			}

			//Log.d(TAG_BT,"motorRight: "+motorRight);
			//Log.d(TAG_BT,"motorLeft : "+motorLeft);


		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};	

	private void checkTouchStatus(int port, int val){
		//Log.d(TAG_BT, "en checktouchstatus ");

		if (val==1 && previousTouchState == 0) {
			mVibrator.vibrate(250);
			sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_BEEP, 400,200);
			switch (port){
			case BTCommunicator.PORT_1:
				break;
			case BTCommunicator.PORT_2:
				break;
			case BTCommunicator.PORT_3:

				mtouchSensor1.cont+=1;
				CommandControllerFragment.touch_number_collision.setText("" + mtouchSensor1.cont +" cm");
				break;
			case BTCommunicator.PORT_4:
				mtouchSensor2.cont+=1;
				break;
			}
		}
		previousTouchState = val;
	}
	
	private void checkSonarStatus(int port, int val){
		//Log.d(TAG_BT, "en checktouchstatus ");

		
			//mVibrator.vibrate(250);
			//sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_BEEP, 400,200);
			switch (port){
			case BTCommunicator.PORT_1:
				break;
			case BTCommunicator.PORT_2:
				mUltraSonicSensor.cont=val;
				CommandControllerFragment.sonar_number_collision.setText("" + mUltraSonicSensor.cont);
				
				break;
			case BTCommunicator.PORT_3:

				
				
				break;
			case BTCommunicator.PORT_4:

				break;
			}
		
		
	}




	/**
	 * Sends the motor control values to the communcation thread.
	 * @param left The power of the left motor from 0 to 100.
	 * @param rigth The power of the right motor from 0 to 100.
	 */   
	public void updateMotorControl(int left, int right) {

		if (myBTCommunicator != null) {
			// don't send motor stop twice                        
			// send messages via the handler
			sendBTCmessage(BTCommunicator.NO_DELAY, MOTOR_LEFT,left,0 );
			sendBTCmessage(BTCommunicator.NO_DELAY, MOTOR_RIGHT,right,0 );
		}
	}

	final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			switch (myMessage.getData().getInt("message")) {
			case BTCommunicator.DISPLAY_TOAST:
				//showToast(myMessage.getData().getString("toastText"), Toast.LENGTH_SHORT);
				break;
			case BTCommunicator.STATE_CONNECTED:
				connected = true;
				wasConnected = true;
				Log.d(TAG_BT,"Connected!!!!!");
				disconnectBtnM.setVisible(true);
				reusableFragment = CommandControllerFragment.newInstance(targetMAC, DEFAULT_LAYOUT);
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.container,reusableFragment).commit();

				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.DO_BEEP, 400,200);

				mtouchSensor1= new TouchSensor(btcHandler, BTCommunicator.PORT_3);
				mtouchSensor1.start();
				mUltraSonicSensor = new UltraSonicSensor(btcHandler, BTCommunicator.PORT_2);
				mUltraSonicSensor.start();

				//mLightSensor1=new LightSensor(btcHandler, BTCommunicator.PORT_1);
				//mLightSensor1.start();
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.ULTRASONIC_ACTIVATION, 0,0);
				sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.ULTRASONIC_SET  , 0,0);
				//sendBTCmessage(BTCommunicator.NO_DELAY, BTCommunicator.ULTRASONIC_CHECK  , 0,0);


				break;

			case BTCommunicator.STATE_CONNECTERROR:
				//connectingProgressDialog.dismiss();
				showToast(R.string.problem_at_connecting, Toast.LENGTH_SHORT);
				break;
			case BTCommunicator.ULTRASONIC_ACTIVATION:

				if (myBTCommunicator != null) {
					byte[] distanceMessage = myBTCommunicator.getReturnMessage();
					int dist= (int)distanceMessage[2];
					Log.d(TAG_BT, "distance activation ,status byte = "+distanceMessage[2]);
					Toast.makeText(getApplicationContext(), "ultrasonic activation  : "+dist, Toast.LENGTH_SHORT ).show();

				}
				break;

			case BTCommunicator.ULTRASONIC_CHECK:

				if (myBTCommunicator != null) {
					byte[] distanceMessage = myBTCommunicator.getReturnMessage();
					//Log.d(TAG_BT, "distance check ,status byte = "+distanceMessage[2]+" bytes ready "+distanceMessage[3]);
					Log.d(TAG_BT, "distance check ,status byte = "+distanceMessage[2]);
					//Toast.makeText(getApplicationContext(),  "ultrasonic check ,status byte = "+distanceMessage[2]+" bytes ready "+distanceMessage[3], Toast.LENGTH_SHORT ).show();
				}
				break;
			case BTCommunicator.ULTRASONIC_GET:

				if (myBTCommunicator != null) {
					byte[] distanceMessage = myBTCommunicator.getReturnMessage();
					if (distanceMessage.length>5){
					Log.d(TAG_BT, "distance get ,status byte = "+distanceMessage[2]+" bytes ready "+distanceMessage[3]+"vals: "+distanceMessage[4]+" "+distanceMessage[5]);
					checkSonarStatus(BTCommunicator.PORT_2, distanceMessage[4]);
					//Toast.makeText(getApplicationContext(),  "ultrasonic get ,status byte = "+distanceMessage[4] , Toast.LENGTH_SHORT ).show();
					}
				}
				break;

			case BTCommunicator.ULTRASONIC_SET:

				if (myBTCommunicator != null) {
					byte[] distanceMessage = myBTCommunicator.getReturnMessage();
					int dist= (int) distanceMessage[2];
					
					Log.d(TAG_BT, "ultrasonic set ,status byte = "+distanceMessage[2]+" bytes ready ");
				}
				break;

				//touch test	
			case BTCommunicator.TOUCH_STATUS:
				if (myBTCommunicator != null) {
					byte[] distanceMessage = myBTCommunicator.getReturnMessage();

					if (distanceMessage.length>10){
						checkTouchStatus(distanceMessage[3], distanceMessage[12]);
					}
				}

				break;
			case BTCommunicator.VIBRATE_PHONE:
				if (myBTCommunicator != null) {
					byte[] vibrateMessage = myBTCommunicator.getReturnMessage();
					if (vibrateMessage!= null){
						mVibrator.vibrate(250);
					}
				}

				break;
			}
		}
	};

	//--MENU OPTIONS
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		disconnectBtnM = menu.findItem(R.id.action_disconnect);
		disconnectBtnM.setVisible(false);
		//menu.findItem(R.id.action_aboutus).setIcon(R.drawable.ic_action_about);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id){
		case R.id.action_settings:

			startActivity(new Intent(this,SettingsActivity.class));
			break;
		case R.id.action_disconnect:
			wasConnected=false;
			destroyBTCommunicator();
			disconnectBtnM.setVisible(false);
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.container,MainFragment.newInstance("", "")).commit();
			break;
		case R.id.action_aboutus:
			startActivity(new Intent(this,AboutActivity.class));
			break;
		default:

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFragmentInteraction(String msg,String val) {
		// TODO Auto-generated method stub
		Log.d("BLUETOOTH",msg+" "+val);
		switch (msg){
		case MainFragment.EXTRA_DEVICE_ADDRESS :
			targetMAC=val;
			startBTCommunicator(targetMAC);
			break;
		case CommandControllerFragment.BTN_ACCELEROMETER:
			if (val.equals(CommandControllerFragment.ACC_DISABLE)){
				createAcController();
				isAccActive=true;
			}else{
				isAccActive=false;

				mSensorManager.unregisterListener(acControllerListener);
				updateMotorControl(0, 0);
			}
			break;
		case CommandControllerFragment.VEL_SEEKBAR:
			int res=Integer.parseInt(val);

			MAXSPEED=res;
			//sendBTCmessage(BTCommunicator.NO_DELAY , BTCommunicator.ULTRASONIC_SET , 0,0);
			//sendBTCmessage(BTCommunicator.NO_DELAY , BTCommunicator.ULTRASONIC_GET , 0,0);
			//res=Math.round((20*maxSpeed)/100);
			break;

		case CommandControllerFragment.BTN_UP:
			motorLeft=MAXSPEED;
			motorRight=MAXSPEED;
			if (val=="stop"){

				updateMotorControl(0, 0);
			}
			else{

				updateMotorControl(motorLeft, motorRight);	
			}

			break;
		case CommandControllerFragment.BTN_RIGHT:
			motorLeft=MAXSPEED;
			motorRight=-MAXSPEED;
			if (val=="stop"){
				if (isAccActive){
					createAcController();	
				}else{
					updateMotorControl(0, 0);
				}
			}
			else{
				if (isAccActive){
					mSensorManager.unregisterListener(acControllerListener);
				}
				updateMotorControl(motorLeft, motorRight);	
			}

			break;
		case CommandControllerFragment.BTN_LEFT:
			motorLeft=-MAXSPEED;
			motorRight=MAXSPEED;
			if (val=="stop"){
				if (isAccActive){
					createAcController();	
				}else{
					updateMotorControl(0, 0);
				}

			}
			else{
				if (isAccActive){
					mSensorManager.unregisterListener(acControllerListener);
				}
				updateMotorControl(motorLeft, motorRight);	
			}

			break;
		case CommandControllerFragment.BTN_DOWN: 
			motorLeft=-MAXSPEED;
			motorRight=-MAXSPEED;
			if (val=="stop"){

				updateMotorControl(0, 0);
			}
			else{

				updateMotorControl(motorLeft, motorRight);	
			}

			break;
		default:


			break;

		}

	}




	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		Log.d("BLUETOOTH", "En shared cc "+key);
		switch (key){

		case "layouts":
			DEFAULT_LAYOUT=sharedPreferences.getString(key, "-1");
			break;
		case "motorAux":
			MOTOR_AUX = Integer.parseInt(sharedPreferences.getString(key, "-1"));
			break;
		case "motorLeft":
			MOTOR_LEFT = Integer.parseInt(sharedPreferences.getString(key, "-1"));
			break;
		case "motorRight":
			MOTOR_RIGHT = Integer.parseInt(sharedPreferences.getString(key, "-1"));
			break;
		default:

			break;
		}
	}


}
