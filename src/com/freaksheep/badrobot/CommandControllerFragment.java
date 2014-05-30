package com.freaksheep.badrobot;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link CommandControllerFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link CommandControllerFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class CommandControllerFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;


	//common buttons from layout
	private Button btnAccelerometer,btnUP,btnDown,btnLeft,btnRight;
	private SeekBar velBar;
	public static TextView touch_number_collision;
	public static TextView sonar_number_collision;
	private View myFragmentLayout;
	private String defaultLayout;

	//Messages for communication with the host activity
	public static final String BTN_ACCELEROMETER = "btn_accelerometer";
	public static final String ACC_ENABLE = "enable";
	public static final String ACC_DISABLE = "disable";
	public static final String VEL_SEEKBAR = "vel_seekbar";
	public static final String BTN_RIGHT = "btn_right";
	public static final String BTN_LEFT = "btn_left";
	public static final String BTN_UP = "btn_up";
	public static final String BTN_DOWN = "btn_down";


	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment BlankFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static CommandControllerFragment newInstance(String param1, String param2) {
		CommandControllerFragment fragment = new CommandControllerFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}
	public CommandControllerFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		LoadLayout(mParam2);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		myFragmentLayout = inflater.inflate(R.layout.fragment_controller, container, false);

		touch_number_collision = (TextView) myFragmentLayout.findViewById(R.id.touch_number_collision);
		touch_number_collision.setText("0");
		sonar_number_collision = (TextView) myFragmentLayout.findViewById(R.id.sonar_number_collision);
		sonar_number_collision.setText("0");

		btnAccelerometer= (Button) myFragmentLayout.findViewById(R.id.btnAcc);
		btnAccelerometer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (btnAccelerometer.getText().equals((String)getResources().getString(R.string.action_enableAc))){

					onButtonPressed(BTN_ACCELEROMETER, ACC_DISABLE);
					btnAccelerometer.setText(R.string.action_disableAc);
				}else{

					onButtonPressed(BTN_ACCELEROMETER, ACC_ENABLE);
					btnAccelerometer.setText(R.string.action_enableAc);
				}
			}
		});

		velBar=(SeekBar)myFragmentLayout.findViewById(R.id.velBar);
		velBar.setMax(100);
		velBar.setProgress(50);
		velBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				onButtonPressed(VEL_SEEKBAR, progress);
			}
		});
		btnUP=(Button) myFragmentLayout.findViewById(R.id.btnUp);
		btnUP.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.d("BLUETOOTH", ""+event.getAction());
				switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					onButtonPressed(BTN_UP, "");
					break;
				case MotionEvent.ACTION_UP:
					onButtonPressed(BTN_UP, "stop");
					break;
				case MotionEvent.ACTION_MOVE:
					onButtonPressed(BTN_UP, "");
					break;
				}

				return false;
			}
		}); 
		btnDown=(Button) myFragmentLayout.findViewById(R.id.btnDown);
		btnDown.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.d("BLUETOOTH", ""+event.getAction());
				switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					onButtonPressed(BTN_DOWN, "");
					break;
				case MotionEvent.ACTION_UP:
					onButtonPressed(BTN_DOWN, "stop");
					break;
				case MotionEvent.ACTION_MOVE:
					onButtonPressed(BTN_DOWN, "");
					break;
				}

				return false;
			}
		}); 
		btnLeft=(Button) myFragmentLayout.findViewById(R.id.btnLeft);
		btnLeft.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.d("BLUETOOTH", ""+event.getAction());
				switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					onButtonPressed(BTN_LEFT, "");
					break;
				case MotionEvent.ACTION_UP:
					onButtonPressed(BTN_LEFT, "stop");
					break;
				case MotionEvent.ACTION_MOVE:
					onButtonPressed(BTN_LEFT, "");
					break;
				}

				return false;
			}
		}); 
		btnRight=(Button) myFragmentLayout.findViewById(R.id.btnRight);
		btnRight.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.d("BLUETOOTH", ""+event.getAction());
				switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					onButtonPressed(BTN_RIGHT, "");
					break;
				case MotionEvent.ACTION_UP:
					onButtonPressed(BTN_RIGHT, "stop");
					break;
				case MotionEvent.ACTION_MOVE:
					onButtonPressed(BTN_RIGHT, "");
					break;
				}

				return false;
			}
		}); 


		return myFragmentLayout;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(String msg , String val) {
		if (mListener != null) {
			mListener.onFragmentInteraction(msg , val);
		}
	}
	public void onButtonPressed(String msg , int val) {
		if (mListener != null) {
			mListener.onFragmentInteraction(msg , String.valueOf(val));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(String msg , String val);
	}

	private void LoadLayout(String type) {
		// TODO Auto-generated method stub

		Log.d("BLUETOOTH","en loadlayout "+type);  
		switch (type) {
		case "0":
			btnDown.setVisibility(View.INVISIBLE);
			btnUP.setVisibility(View.INVISIBLE);
			btnLeft.setVisibility(View.INVISIBLE);
			btnRight.setVisibility(View.INVISIBLE);
			btnAccelerometer.setVisibility(View.VISIBLE);
			myFragmentLayout.invalidate();
			/*myFragmentLayout.findViewById(R.layout.fragment_controller_buttons).setVisibility(View.INVISIBLE);
			myFragmentLayout.findViewById(R.id.btnAcc).setVisibility(View.VISIBLE);
			myFragmentLayout.invalidate();*/
			break;
		case "1":
			btnDown.setVisibility(View.VISIBLE);
			btnUP.setVisibility(View.VISIBLE);
			btnLeft.setVisibility(View.VISIBLE);
			btnRight.setVisibility(View.VISIBLE);
			btnAccelerometer.setVisibility(View.INVISIBLE);
			myFragmentLayout.invalidate();


			break;
		case "2":

			btnDown.setVisibility(View.GONE);
			btnUP.setVisibility(View.GONE);
			btnLeft.setVisibility(View.VISIBLE);
			btnRight.setVisibility(View.VISIBLE);
			btnAccelerometer.setVisibility(View.VISIBLE);
			myFragmentLayout.invalidate();
			break;

		default:
			//myFragmentLayout.findViewById(R.layout.fragment_controller_buttons).setVisibility(View.INVISIBLE);
			//myFragmentLayout.findViewById(R.layout.fragment_controller_accelerometer).setVisibility(View.VISIBLE);
			//myFragmentLayout.findViewById(R.id.btnDown).setVisibility(View.VISIBLE);
			//myFragmentLayout.findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
			//myFragmentLayout.findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
			//myFragmentLayout.findViewById(R.id.btnUp).setVisibility(View.VISIBLE);
			//myFragmentLayout.findViewById(R.id.btnAcc).setVisibility(View.VISIBLE);

			myFragmentLayout.invalidate();
			break;
		}

	}

}
