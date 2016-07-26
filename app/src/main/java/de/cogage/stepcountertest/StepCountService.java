package de.cogage.stepcountertest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StepCountService extends Service {
    private SensorManager sensorManager;
    private HashMap<String, String> steps;
    private Service me ;
    private int last=0;
    private CountDownTimer cdt;
    private boolean running = false;
    public StepCountService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        steps = new HashMap<String ,String>();
        cdt = new CountDownTimer(15000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                System.out.println("Starting Upload");
                RequestQueue queue = Volley.newRequestQueue(me);
                String url ="http://cooper.wineme.fb5.uni-siegen.de/steps";
                StringRequest sr = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(me,"Uploaded ! Response is: "+ response.toString(),Toast.LENGTH_LONG).show();
                        steps = new HashMap<String, String>();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("StepError", error.toString());
                        Toast.makeText(me,"Error Couldn't upload",Toast.LENGTH_LONG).show();
                    }
                })
                {
                    @Override
                    protected Map<String,String> getParams(){
                        return steps;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };
                queue.add(sr);


               if(running) cdt.start();
            }
        };
        cdt.start();
        me = this;
        if (countSensor != null) {
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if(last!=0){
                        Toast.makeText(me, "Schritte" + event.values[0], Toast.LENGTH_SHORT).show();
                        steps.put(""+new Date().getTime(), ""+(event.values[0]-last));
                        last = (int)event.values[0];
                    }else{
                        last=(int)event.values[0];
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        running = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        running= false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
