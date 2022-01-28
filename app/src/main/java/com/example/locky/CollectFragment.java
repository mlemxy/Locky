package com.example.locky;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.URIBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.URISyntaxException;
import java.util.Random;

// Unlock page from collect fragment

public class CollectFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextView sendText2;
    private String lockerNum;


    //private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    //    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    private String BTresponse;
    private String bufBTresponse;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        setRetainInstance(true);
        assert getArguments() != null;
        deviceAddress = getArguments().getString("device");
        Log.i("Create running", "check");
        Log.i("address", deviceAddress);
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("start", "running");
        if (service != null) {
            service.attach(this);
            Log.i("service not null", "true");
        } else {
            Log.i("starting serial service", "running");
            getActivity().startService(new Intent(getActivity(), SerialService.class));
            Log.i("serial service", String.valueOf(service));
            Log.i("activity", String.valueOf(getActivity()));
        }// prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        Log.i("Attach", "running");
        super.onAttach(activity);
        requireActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        Log.i("service", String.valueOf(service));
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("creating view", "running");
        View view = inflater.inflate(R.layout.fragment_setup, container, false);

//        sendText2 = view.findViewById(R.id.send_text2); // this part is to confirm pw

        view.findViewById(R.id.confirmPWrow).setVisibility(View.GONE);


        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        //hexWatcher = new TextUtil.HexWatcher(sendText);
        //hexWatcher.enable(hexEnabled);
        //sendText.addTextChangedListener(hexWatcher);
        //sendText.setHint(hexEnabled ? "HEX mode" : "");
        //getActivity().onBackPressed();

        View sendBtn = view.findViewById(R.id.send_btn);

        View sendBtn2 = view.findViewById(R.id.send_btn2);

        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));

        sendBtn2.setOnClickListener(v -> send(""));

        view.findViewById(R.id.confirmPWrow).setVisibility(View.GONE);
        view.findViewById(R.id.unlockPWrow).setVisibility(View.GONE);
        view.findViewById(R.id.buttonrow).setVisibility(View.GONE);
        view.findViewById(R.id.textView2).setVisibility(View.GONE);
        view.findViewById(R.id.buttonrow2).setVisibility(View.GONE);

        receiveText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {

                    if ((receiveText.getText().toString().length() == 4) && (s.length() == 4)) {
                        Log.i("received", receiveText.getText().toString());
                        receiveText.removeTextChangedListener(this);
                        String fxBTresponse = receiveText.getText().toString();
                        Log.i("fxBTresponse", fxBTresponse);
                        Toast.makeText(getActivity(), fxBTresponse, Toast.LENGTH_SHORT).show();


                        Log.i("Check3", fxBTresponse);
                        // you can call or do what you want with your EditText here
                        Toast.makeText(getActivity(), "BTresponse...", Toast.LENGTH_SHORT).show();
                        view.findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.textView2)).setText(lockerNum);

                        if (fxBTresponse.charAt(1) == 'A') {
                            ((EditText) view.findViewById(R.id.send_text)).setText("");
                            view.findViewById(R.id.confirmPWrow).setVisibility(View.GONE);
                            view.findViewById(R.id.unlockPWrow).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.buttonrow).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.buttonrow2).setVisibility(View.GONE);

//                            ((EditText) view.findViewById(R.id.send_text)).setText("");
                            ((TextView) view.findViewById(R.id.textView)).setText(R.string.TitleTextSet);
                            ((Button) sendBtn).setText(R.string.buttonSet);
                            Toast.makeText(getActivity(), "AVAILABLE!", Toast.LENGTH_SHORT).show();

                        } else if (fxBTresponse.charAt(1) == 'B') {
                            view.findViewById(R.id.textView).setVisibility(View.GONE);
                            view.findViewById(R.id.confirmPWrow).setVisibility(View.GONE);
                            view.findViewById(R.id.unlockPWrow).setVisibility(View.GONE);
                            view.findViewById(R.id.buttonrow).setVisibility(View.GONE);
                            view.findViewById(R.id.buttonrow2).setVisibility(View.VISIBLE);
//                            ((TextView) view.findViewById(R.id.textView)).setText(R.string.TitleText);
                            ((Button) sendBtn2).setText(R.string.button);
                            Toast.makeText(getActivity(), "BOOKED!", Toast.LENGTH_SHORT).show();

                        } else if (fxBTresponse.charAt(1) == 'O') {
                            Toast.makeText(getActivity(), "UNLOCKED!", Toast.LENGTH_SHORT).show();


                        }
                        receiveText.removeTextChangedListener(this);
                        receiveText.setText("");
                        receiveText.addTextChangedListener(this);
                    }
                } catch (NumberFormatException e) {
                    //do whatever you like when value is incorrect

                }
            }
        });

        return view;
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_terminal, menu);
//        menu.findItem(R.id.hex).setChecked(hexEnabled);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.clear) {
//            receiveText.setText("");
//            return true;
//        } else if (id == R.id.newline) {
//            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
//            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
//            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle("Newline");
//            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
//                newline = newlineValues[item1];
//                dialog.dismiss();
//            });
//            builder.create().show();
//            return true;
//        } else if (id == R.id.hex) {
//            //hexEnabled = !hexEnabled;
//            sendText.setText("");
//            //hexWatcher.enable(hexEnabled);
//            //sendText.setHint(hexEnabled ? "HEX mode" : "");
//            //item.setChecked(hexEnabled);
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            Log.i("device address1", "running");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.i("device address", deviceAddress);
            status("connecting...");
            Toast.makeText(getActivity(), "Connecting...", Toast.LENGTH_SHORT).show();
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
            lockerNum = device.getName().toUpperCase();


        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
        //if the str is master key, reset code $MM#
        //Authenticate here to check if user is supposed to have access. if yes send MM string , if no keep as Red.

        if (str.equals("")) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference lockerRef = db.collection("locker").document(lockerNum.toLowerCase());


            URIBuilder ub = null;
            try {
                ub = new URIBuilder("https://.herokuapp.com/");
                ub.addParameter("newReciever2", );
                ub.addParameter("newLockerNumber2", lockerNum.toLowerCase());
                ub.addParameter("newBooker2", signInAccount.getEmail());


            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            lockerRef.update("booked_status", false, "receiver", "", "booked_by", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });


            Log.i("before", str);
            str = "$MM#";
            Log.i("after", str);
        } else {
            Random random = new Random();
            int r = random.nextInt(999999);
            str = ('$' + String.valueOf(r) + "#");
            Log.i("random", str);

        }

        if (connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
//            if(hexEnabled) {
//                StringBuilder sb = new StringBuilder();
//                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
//                TextUtil.toHexString(sb, newline.getBytes());
//                msg = sb.toString();
//                data = TextUtil.fromHexString(msg);
//                Log.i("data1", msg);
//            } else {
            msg = str;
            Log.i("data2", msg);
            data = (str + newline).getBytes();
            Log.i("data3", String.valueOf(data));
//            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //receiveText.append(spn);//This is sending text...
            service.write(data);
            getActivity().onBackPressed();
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        String msg = new String(data);
        Log.i("msg3", msg);
//        if(hexEnabled) {
//            //receiveText.append(TextUtil.toHexString(data) + '\n'); //hex text?
//        } else {
//            String msg = new String(data);
        Log.i("e1", msg);
        if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
            // don't show CR as ^M if directly before LF
            msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
            Log.i("e2", msg);
            // special handling if CR and LF come in separate fragments
            if (pendingNewline && msg.charAt(0) == '\n') {
                Editable edt = receiveText.getEditableText();
                if (edt != null && edt.length() > 1)
                    edt.replace(edt.length() - 2, edt.length(), "");
            }
            pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            Log.i("e3", msg);
        }

        //Will open when connect "$AA#" "$AE#", pop up to set password
        //If someone booked, will wait for password "$BA#" "$BI#"
        //if password correct, will unlock and reset
        //Master reset need to send $MM#
        //Password set of unlock is $PPPPPP#
        //limit password to 6 digits.
        Log.i("e4", msg);

        receiveText.append(msg);
    }
//    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //receiveText.append(spn); //Status display
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        Toast.makeText(getActivity(), "connected...", Toast.LENGTH_SHORT).show();
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
        Toast.makeText(getActivity(), "Connection failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
        Toast.makeText(getActivity(), "Connection lost!", Toast.LENGTH_SHORT).show();
    }


}
