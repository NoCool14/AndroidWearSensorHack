package hastudios.androidwearsensorhack;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.Manifest.permission_group.STORAGE;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.google.android.gms.internal.zzs.TAG;

public class MainActivity extends Activity
{

    private static final String GYROLOG = "/Gyrolog";
    private static final String ACCELLOG = "/Accellog";
    private TextView mTextView;


    private SensorManager mSensorManager;
    private Sensor sGyroscope, sAccelerometer;

    private double accelerometerVectorOne = 0;
    private double accelerometerVectorTwo = 0;

    private double gyrometerVectorOne = 0;
    private double gyrometerVectorTwo = 0;

    GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        getSensors();

        sGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(new GyroscopeSensorListener(getApplicationContext()), sGyroscope, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(new AccelerometerSensorListener(getApplicationContext()), sAccelerometer, 50000);
    }


    private void getSensors() {

        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor type : deviceSensors) {
            Log.i("sensors", type.getName().toString());
        }
    }


    private class GyroscopeSensorListener implements SensorEventListener
    {


        Context context;
        public GyroscopeSensorListener(Context context)
        {
            this.context = context;
        }
        @Override
        public void onSensorChanged(SensorEvent event)
        {


            if (Math.abs(event.values[0] - gyrometerVectorOne) > 0.1 && Math.abs(event.values[1] - gyrometerVectorTwo) > 0.1)
            {
                TextView gyroText = (TextView) findViewById(R.id.Gyro);
                gyroText.setText("GYROSCOPE \n X: " + event.values[0] + "\n Y: " + event.values[1] + "\n Z: " + event.values[2]);
                sendToPhone(GYROLOG,event.values[0], event.values[1], event.values[2]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class AccelerometerSensorListener implements SensorEventListener {
        Context context;
        public AccelerometerSensorListener(Context context)
        {
            this.context = context;
        }



        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if (Math.abs(event.values[0] - accelerometerVectorOne) > 0.1 && Math.abs(event.values[1] - accelerometerVectorTwo) > 0.1)
            {
                accelerometerVectorOne = event.values[0]; accelerometerVectorTwo = event.values[1];
                TextView accelerometerTextView= (TextView)findViewById(R.id.Accel) ;
                accelerometerTextView.setText("ACCELEROMETER \nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                sendToPhone(ACCELLOG,event.values[0], event.values[1], event.values[2]);


            }

        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }
    private void sendToPhone(String sensor, float firstValue, float secondValue, float thirdValue) {
        Log.d("SEND TO PHONE", "sendToPhone: ");
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(sensor);
        putDataMapRequest.getDataMap().putFloat("firstValue", firstValue);
        putDataMapRequest.getDataMap().putFloat("secondValue", secondValue);
        putDataMapRequest.getDataMap().putFloat("thirdValue", thirdValue);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();


        if (!mGoogleApiClient.isConnected())
        {
            Log.d("SEND TO PHONE", "!CONNECTED");
            return;
        }

        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "attempt to put data was a " +
                        (dataItemResult.getStatus().isSuccess()? "success!"
                                : "failure =( " + dataItemResult.getStatus().getStatusCode()));
            }
        });
    }

}
