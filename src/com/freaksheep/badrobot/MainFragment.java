package com.freaksheep.badrobot;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link MainFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class MainFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	//objects from the layout 
	private View myFragmentLayout;
	private Button scanBtn;
	private TextView title_new_devices;
	private OnFragmentInteractionListener mListener;
	
	
	//vars from deviceListActivity
	static final String PAIRING = "pairing";

    // Return Intent extra
    public static final String DEVICE_NAME_AND_ADDRESS = "device_infos";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

	//end vars from deviceListActivity
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment MainFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static MainFragment newInstance(String param1, String param2) {
		MainFragment fragment = new MainFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public MainFragment() {
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//mandatory if we want to add listeners to the buttons etc... 
		View myFragmentLayout = inflater.inflate(R.layout.fragment_main, container, false);
		
		
		//load the deviceList
		
				// Initialize array adapters. One for already paired devices and
		        // one for newly discovered devices
		        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);
		        mNewDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);

		        // Find and set up the ListView for paired devices
		        ListView pairedListView = (ListView) myFragmentLayout.findViewById(R.id.paired_devices);
		        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		        pairedListView.setOnItemClickListener(mDeviceClickListener);

		        // Find and set up the ListView for newly discovered devices
		        ListView newDevicesListView = (ListView) myFragmentLayout.findViewById(R.id.new_devices);
		        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		     // Turn on sub-title for new devices
		        title_new_devices=(TextView)myFragmentLayout.findViewById(R.id.title_new_devices);
		        
		        

		        // Get the local Bluetooth adapter
		        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		        // Get a set of currently paired devices
		        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		        // If there are paired devices, add each one to the ArrayAdapter
		        boolean legoDevicesFound = false;
		        myFragmentLayout.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
		        if (pairedDevices.size() > 0) {
		        	//myFragmentLayout.findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
		            for (BluetoothDevice device : pairedDevices) {
		                // only add LEGO devices
		                if (device.getAddress().startsWith(BTCommunicator.OUI_LEGO)) {
		                    legoDevicesFound = true;
		                    mPairedDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
		                }
		            }
		        }
		        
		        if (legoDevicesFound == false) {
		            String noDevices = getResources().getText(R.string.none_paired).toString();
		            mPairedDevicesArrayAdapter.add(noDevices);
		        }
				
				
				//load the button in the fragment to handle the clicks and add a click listener to communicate with the main. this is a test.
		        ImageView mImage =(ImageView)myFragmentLayout.findViewById(R.id.imageView1);
		        mImage.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						v.setEnabled(false);
						doDiscovery();
					}
				});
				scanBtn = (Button) myFragmentLayout.findViewById(R.id.scanner);
				scanBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//onButtonPressed("Hola hola!!");
						Log.d("BLUETOOTH", "en search!");
						v.setEnabled(false);
						doDiscovery();
					}
				});

		// Inflate the layout for this fragment
		return myFragmentLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onDeviceSelected(String msg,String val) {
		if (mListener != null) {
			
			mListener.onFragmentInteraction(msg,val);
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

	
	//--TEST BLUETOOTH IN FRAGMENT
	
	//test bluetooth discovery
	private void doDiscovery() {
		// Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		// If there are paired devices, add each one to the ArrayAdapter
        boolean legoDevicesFound = false;
        
        if (pairedDevices.size() > 0) {
        	mPairedDevicesArrayAdapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                // only add LEGO devices
                if (device.getAddress().startsWith(BTCommunicator.OUI_LEGO)) {
                    legoDevicesFound = true;
                    mPairedDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                }
            }
        }
        
        if (legoDevicesFound == false) {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
		
		// If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
		mNewDevicesArrayAdapter.clear();
		// Register for broadcasts when a device is discovered
		
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
        // Turn on sub-title for new devices
        title_new_devices.setVisibility(View.VISIBLE);

        

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            // did we choose a correct name and address?
            if (info.lastIndexOf('-') != info.length()-18) 
                return;

            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, this is the text after the last '-' character
            String address = info.substring(info.lastIndexOf('-')+1);
            // Create the result Intent and include the infos
            Intent intent = new Intent();
            Bundle data = new Bundle();
            data.putString(DEVICE_NAME_AND_ADDRESS, info);
            data.putString(EXTRA_DEVICE_ADDRESS, address);
            data.putBoolean(PAIRING,av.getId()==R.id.new_devices);
            intent.putExtras(data);
            // Set result and finish this Activity
            
            onDeviceSelected(EXTRA_DEVICE_ADDRESS, address);
            
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "-" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	getActivity().unregisterReceiver(this);
            	Log.d("BLUETOOTH", "en discovery finished!");
            	scanBtn.setEnabled(true);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                    
                    
                }
            }
        }
    };
}
