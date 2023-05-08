package com.yoya.printtest;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yoya.printtest.R;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//implements View.OnClickListener

public class MainActivity extends AppCompatActivity {

    ListView mLvPairedDevices;
    Button mBtnSetting;
    Button mBtnTest;
    Button mBtnPrint;
    private static final int PERMISSION_REQUEST_CODE = 2;

    // DeviceListAdapter mAdapter;
    int mSelectedPosition = -1;

    final static int TASK_TYPE_CONNECT = 1;
    final static int TASK_TYPE_PRINT = 2;

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // initViews();

        mBtnPrint = (Button) findViewById(R.id.btn_print);
        mBtnPrint.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

                testPrinting();
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void testPrinting() {

        //BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
          //  return;
        }


        try {
            if(!btAdapter.isEnabled()){
                Log.e("UUID", "BT wasn't enabled");
                btAdapter.enable();
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Method getUuidsMethod = null;
        try {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            try {
                ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(btAdapter, null);
                if(uuids.length >0){

                    Iterator<ParcelUuid> it = Arrays.stream(uuids).iterator();

                    while(it.hasNext()) {
                        Log.e("UUID", String.valueOf(it.next().getUuid()));
                    }


                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


       Set<BluetoothDevice> printerDevices = btAdapter.getBondedDevices();

        // Get first paired device
        BluetoothDevice mBtDevice = printerDevices.iterator().next();

            final BluetoothPrinter mPrinter = new BluetoothPrinter(mBtDevice);

            if(mPrinter!=null){
                Log.e("printer",mPrinter.getDevice().getAddress());
                Log.e("printer",mPrinter.getDevice().getName());
            }else{
                Log.e("printer","No printer selected");
                return;
            }

            mPrinter.connectPrinter(new BluetoothPrinter.PrinterConnectListener() {

                @Override
                public void onConnected() {

                    try {
                        mPrinter.initPrinter();
                    } catch (IOException e) {
                        Log.d("BluetoothPrinter", "Printer Init failed");
                        e.printStackTrace();
                    }

                    Log.d("BluetoothPrinter", "Try to print");
                    mPrinter.setAlign(BluetoothPrinter.ALIGN_CENTER);
                    mPrinter.printText("Hello World!");
                    mPrinter.addNewLine();
                    mPrinter.finish();
                }

                @Override
                public void onFailed() {
                    Log.d("BluetoothPrinter", "BT printing failed");
                }

            });
        }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                          int[] grantResults){

        for(int result:grantResults) {
            if(result == PackageManager.PERMISSION_DENIED) {
                Log.d("Bluettoth printing", "Bluetooth permission rejected");
                return;
            }
        }

        switch(requestCode) {
            case PERMISSION_REQUEST_CODE:
                Log.d("Bluetooth printing", "User granted location permission");
                break;
        }

    }

//        private boolean checkPermission(String permission) {
//                // int result = ActivityCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
//                //int result = ActivityCompat.checkSelfPermission(getApplicationContext(), permission);
//               // return result == PackageManager.PERMISSION_GRANTED;
//                return true;
//        }
//
//        private void requestPermission(String permission) {
//                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
//        }
//
//
//        @Override
//        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//
//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//                super.onActivityResult(requestCode, resultCode, data);
//        }
//
//        @Override
//        protected void onResume() {
//                super.onResume();
//                fillAdapter();
//        }
//
//        private void initViews() {
//                mLvPairedDevices = (ListView) findViewById(R.id.lv_paired_devices);
//                mBtnSetting = (Button) findViewById(R.id.btn_goto_setting);
//                mBtnTest = (Button) findViewById(R.id.btn_test_conntect);
//                mBtnPrint = (Button) findViewById(R.id.btn_print);
//
//                mLvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                mSelectedPosition = position;
//                                mAdapter.notifyDataSetChanged();
//                        }
//                });
//
//                mBtnSetting.setOnClickListener(this);
//                mBtnTest.setOnClickListener(this);
//                mBtnPrint.setOnClickListener(this);
//
//                mAdapter = new DeviceListAdapter(this);
//                mLvPairedDevices.setAdapter(mAdapter);
//        }
//
//        /**
//         * 从所有已配对设备中找出打印设备并显示
//         */
//        private void fillAdapter() {
//                //推荐使用 BluetoothUtil.getPairedPrinterDevices()
//                List<BluetoothDevice> printerDevices = BluetoothUtil.getPairedDevices();
//                mAdapter.clear();
//                mAdapter.addAll(printerDevices);
//                refreshButtonText(printerDevices);
//        }
//
//        private void refreshButtonText(List<BluetoothDevice> printerDevices) {
//                if (printerDevices.size() > 0) {
//                        mBtnSetting.setText("配对更多设备-pair more devices");
//                } else {
//                        mBtnSetting.setText("还未配对打印机，去设置-The printer has not been paired yet");
//                }
//        }
//
//        @Override
//        public void onClick(View v) {
//                switch (v.getId()) {
//                        case R.id.btn_goto_setting:
//                                // startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
//                                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//                                mBluetoothAdapter = mBluetoothManager.getAdapter();
//
//                                startScan();
//
//                                break;
//
//                        case R.id.btn_test_conntect:
//                                connectDevice(TASK_TYPE_CONNECT);
//                                break;
//
//                        case R.id.btn_print:
//                                connectDevice(TASK_TYPE_PRINT);
//                                break;
//                }
//        }
//
//        private void startScan() throws IllegalStateException {
//
//                Boolean isallowed = checkPermission(BLUETOOTH_SCAN);
//
//                if (!isallowed) {
//                        requestPermission(BLUETOOTH_SCAN);
//                }
//
//                if (isallowed) {
//
//                        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
//                        if (scanner == null)
//                                throw new IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?");
//
//                        // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
//                        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
//
//                        scanner.startScan(null, settings, mScanCallback);
//
//                }
//        }
//
//        private ScanCallback mScanCallback = new ScanCallback() {
//                @Override
//                public void onScanResult(int callbackType, ScanResult result) {
//
//                        Boolean isallowed =  checkPermission(BLUETOOTH_SCAN);
//
//                        if(!isallowed){
//                                requestPermission(BLUETOOTH_SCAN);
//                                isallowed = checkPermission(BLUETOOTH_SCAN);
//                        }
//
//                        if (isallowed) {
//
//                                BluetoothDevice device = result.getDevice();
//
//                                if (device != null && device.getName() != null) {
//                                        invokeMethodUIThread("ScanResult", device);
//                                }
//
//                        }
//                }
//        };
//
//
//        private void invokeMethodUIThread(final String name, final BluetoothDevice device) {
//
//                Boolean isallowed =  checkPermission(BLUETOOTH_SCAN);
//
//                if(!isallowed){
//                        requestPermission(BLUETOOTH_SCAN);
//                }
//
//                if(isallowed){
//
//                        final Map<String, Object> ret = new HashMap<>();
//                        ret.put("address", device.getAddress());
//                        ret.put("name", device.getName());
//                        ret.put("type", device.getType());
//                }
//
//
//        }
//
//        private void connectDevice(int taskType) {
//                if (mSelectedPosition >= 0) {
//                        BluetoothDevice device = mAdapter.getItem(mSelectedPosition);
//                        if (device != null)
//                                super.connectDevice(device, taskType);
//                } else {
//                        Toast.makeText(this, "还未选择打印设备-No printing device selected yet", Toast.LENGTH_SHORT).show();
//                }
//        }
//
//        @Override
//        public void onConnected(BluetoothSocket socket, int taskType) {
//                switch (taskType) {
//                        case TASK_TYPE_PRINT:
//                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dd);
//                                PrintUtil.printTest(socket, bitmap);
//                                break;
//                }
//        }
//
//
//        class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
//
//                public DeviceListAdapter(Context context) {
//                        super(context, 0);
//                }
//
//                @Override
//                public View getView(int position, View convertView, ViewGroup parent) {
//
//                        BluetoothDevice device = getItem(position);
//                        if (convertView == null) {
//                                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
//                        }
//
//                        TextView tvDeviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
//                        CheckBox cbDevice = (CheckBox) convertView.findViewById(R.id.cb_device);
//
//                        Boolean isallowed =  checkPermission(BLUETOOTH_SCAN);
//
//                        if(!isallowed){
//                                requestPermission(BLUETOOTH_SCAN);
//                        }
//
//                        if(isallowed) {
//
//                                tvDeviceName.setText(device.getName());
//                                cbDevice.setChecked(position == mSelectedPosition);
//
//                        }
//
//                        return convertView;
//                }
//        }


}
