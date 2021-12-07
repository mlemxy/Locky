package com.example.locky;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Deliver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Deliver extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
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
    public static Deliver newInstance(String param1, String param2) {
        Deliver fragment = new Deliver();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_deliver, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
                text1.setText(device.getName());
                text2.setText(device.getAddress());
                return view;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("Create", "Fragment created");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        return inflater.inflate(R.layout.fragment_deliver, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        if (bluetoothAdapter == null)
            menu.findItem(R.id.bt_settings).setEnabled(false);
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


