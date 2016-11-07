package hastudios.androidwearsensorhack;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.attr.data;
import static android.R.attr.tag;

public class MainActivity extends Activity implements
                                                      GoogleApiClient.ConnectionCallbacks,
                                                      GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "MOBILE_MAIN";

    private static final String GYROLOG = "/Gyrolog";
    private static final String ACCELLOG = "/Accellog";

    File path;
    TextView connectionStatus;


    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(TAG, "ON CREATE");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mGoogleApiClient.connect();
        connectionStatus = (TextView)findViewById(R.id.textView2);
        TextView communicationTextView = (TextView)findViewById(R.id.textView3);

        connectionStatus.setText("NOT CONNECTED");
        communicationTextView.setText("NOTHING COMING IN YET");

        path = getApplicationContext().getFilesDir();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        Log.d(TAG, "INSIDE ONcONNECTED");
        connectionStatus.setText("CONNECTED");
        Wearable.DataApi.addListener(mGoogleApiClient, new SensorHackDataListener(getApplicationContext()));

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.e(TAG, "Connection to Google API client has failed");

    }

    public class SensorHackDataListener implements DataApi.DataListener
    {
        Context context;
        File gyroFile = new File(path, "gyroFile.txt");
        File accelFile = new File(path, "accelFile.txt");
        TextView gyroCommunicationTextView, accelCommunicationTextView;
        public SensorHackDataListener(Context context)
        {
            this.context = context;
            gyroCommunicationTextView = (TextView)findViewById(R.id.textView3);
            accelCommunicationTextView = (TextView)findViewById(R.id.textView4);
        }
        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer)
        {
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // DataItem changed
                    DataItem item = event.getDataItem();
                    if (item.getUri().getPath().compareTo(GYROLOG) == 0)
                    {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        Log.d(TAG, "" + dataMap.getFloat("firstValue"));
                        Log.d(TAG, "" + dataMap.getFloat("secondValue"));
                        Log.d(TAG, "" + dataMap.getFloat("thirdValue"));
                        String output = "GYROSCOPE "
                                + " X: " + dataMap.getFloat("firstValue")
                                + " Y: " + dataMap.getFloat("secondValue")
                                + " Z: " + dataMap.getFloat("thirdValue");


                       gyroCommunicationTextView.setText(output);
                        FileOutputStream stream = null;
                        try
                        {
                            stream = new FileOutputStream(gyroFile);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        try
                        {
                            stream.write(output.getBytes());
                            stream.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else if(item.getUri().getPath().compareTo(ACCELLOG) == 0)
                    {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        Log.d(TAG, "" + dataMap.getFloat("firstValue"));
                        Log.d(TAG, "" + dataMap.getFloat("secondValue"));
                        Log.d(TAG, "" + dataMap.getFloat("thirdValue"));
                        String output = "ACCELEROMETER "
                                + " X: " + dataMap.getFloat("firstValue")
                                + " Y: " + dataMap.getFloat("secondValue")
                                + " Z: " + dataMap.getFloat("thirdValue");

                        accelCommunicationTextView.setText(output);
                        FileOutputStream stream = null;
                        try
                        {
                            stream = new FileOutputStream(accelFile);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        try
                        {
                            stream.write(output.getBytes());
                            stream.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }


                    }
                } else if(event.getType() == DataEvent.TYPE_DELETED)
                {
                    // DataItem deleted
                }
            }

        }
    }
}
