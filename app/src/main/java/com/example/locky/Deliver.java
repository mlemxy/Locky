package com.example.locky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Deliver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Deliver extends ListFragment {

    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
//    protected View mView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public Deliver() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Deliver.
     */
    // TODO: Rename and change types and number of parameters
    public Deliver newInstance(String param1, String param2) {
        Deliver fragment = new Deliver();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
//        getFragmentManager().beginTransaction().add(R.id.fragment, new Deliver(), "devices").commit();
        return fragment;
    }

//    ListView l;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//
////        super.onCreate(savedInstanceState);
////        setHasOptionsMenu(true);
//        Log.i("item count", String.valueOf(listItems.stream().count()));
//        View view = inflater.inflate(R.layout.fragment_deliver, container, false);
//        Log.i("Create", "Fragment created");
////        this.mView = view;
//
//        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), R.layout.fragment_deliver , listItems) {
//            @NonNull
//            @Override
//            public View getView(int position, View view, @NonNull ViewGroup parent) {
//                Log.i("view", "getting view");
//                BluetoothDevice device = listItems.get(position);
////                if (view == null)
////                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_deliver, parent, false);
//                TextView text1 = view.findViewById(R.id.text1);
//                TextView text2 = view.findViewById(R.id.text2);
//                text1.setText(device.getName());
//                text2.setText(device.getAddress());
//                return view;
//            }
//        };
//
//        Log.i("listadapter", String.valueOf(listAdapter.getCount()));
//
//
//        return view;
//
//
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i("bluetooth", "enabled");
        Log.i("item count", String.valueOf(listItems.stream().count()));
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup container) {
                Log.i("view", "getting view");
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_deliver, container, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
//                view.findViewById(R.id.text1).setVisibility(view.VISIBLE);
//                view.findViewById(R.id.text2).setVisibility(view.VISIBLE);
//                view.findViewById(R.id.devices).setVisibility(View.VISIBLE);
                text1.setText(device.getName());
                text2.setText(device.getAddress());
                return view;
            }
        };
        Log.i("listadapter", String.valueOf(listAdapter.getCount()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        View header = getActivity().getLayoutInflater().inflate(R.layout.fragment_deliver, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("initializing...");
        //Toast.makeText(getActivity(), "initializing...", Toast.LENGTH_SHORT).show();

//        ((TextView) getListView().getEmptyView()).setTextSize(18);
        setListAdapter(listAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        if (bluetoothAdapter == null)
            menu.findItem(R.id.bt_settings).setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothAdapter == null) {
            setEmptyText("<bluetooth not supported>");
            Log.i("state", "not supported");
        }
        else if (!bluetoothAdapter.isEnabled()) {
            setEmptyText("<bluetooth is disabled>");
            Log.i("state", "disabled");
        }
        else {
            setEmptyText("<no bluetooth devices found>");
            Log.i("state", "running");
        }
        Log.i("refresh", "refresh");
        refresh();
    }

    public void setEmptyText(@Nullable CharSequence text) {
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        Log.i("click", "click");
        BluetoothDevice device = listItems.get(position-1);
        Bundle args = new Bundle();
        args.putString("device", device.getAddress());
        Fragment fragment = new TerminalFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
//        v = getActivity().getLayoutInflater().inflate(R.layout.fragment_setup, null, false);
//        v.findViewById(R.id.devices).setVisibility(View.GONE);
//        v.findViewById(R.id.text1).setVisibility(v.GONE);
//        v.findViewById(R.id.text2).setVisibility(v.GONE);


    }

    void refresh() {
        listItems.clear();
        if (bluetoothAdapter != null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
                    if (device.getAddress().equals("08:3A:F2:52:25:A2") | device.getAddress().equals("7C:9E:BD:61:E6:C2")) {
//                        if(device.getName().substring(0,6).equalsIgnoreCase("LOCKER")) {
                        Log.i("found", device.getName());
                        listItems.add(device);
                        Log.i("items", String.valueOf(listItems.stream().count()));
//                        }
                    }
                }
            }
        }
        Collections.sort(listItems, Deliver::compareTo);
        listAdapter.notifyDataSetChanged();
        Log.i("length", String.valueOf(listAdapter.getCount()));


    }

    static int compareTo(BluetoothDevice a, BluetoothDevice b) {
        boolean aValid = a.getName() != null && !a.getName().isEmpty();
        boolean bValid = b.getName() != null && !b.getName().isEmpty();
        if (aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if (aValid) return -1;
        if (bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bt_settings) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}


