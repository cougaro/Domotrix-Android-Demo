package com.domotrix.domotrixdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.domotrix.android.services.IDomotrixService;
import com.domotrix.domotrixdemo.sensors.RecognitionData;
import com.domotrix.domotrixdemo.sensors.Sensor;
import com.domotrix.language.DOMOTRIXCommand;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private final static String TAG = "DEMO";
    private SpeechRecognition mSpeechRecognition = new SpeechRecognition();
    private MediaPlayer mp;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Used for DomotrixService binding
     */
    protected IDomotrixService mService = null;
    private boolean mIsBound = false;

    SensorFragment sensorFragment = SensorFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    };

    @Override
    public void onStart() {
        super.onStart();
        connectToRemoteService(getApplicationContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnectFromRemoteService();
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        connectToRemoteService(getApplicationContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectFromRemoteService();
    }
    */

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IDomotrixService.Stub.asInterface(service);
            if (sensorFragment != null) {
                sensorFragment.setService(mService);
                sensorFragment.setSpeechRecognition(mSpeechRecognition);
            }
            try {
                mService.remoteLog(TAG,"=======================================");
                mService.remoteLog(TAG,"=======================================");
                mService.remoteLog(TAG,"APP CLIENT - CONNECTED");
                mService.remoteLog(TAG,"=======================================");
                mService.remoteLog(TAG,"=======================================");
                String sdkVersion = mService.getVersion();
                Log.d(TAG,"SDK Version :"+sdkVersion);
            } catch (RemoteException e) {
                Log.e(TAG,"ERROR",e);
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    protected void connectToRemoteService(Context ctx) {
        if (!mIsBound) {
            Intent serviceIntent = new Intent(IDomotrixService.class.getName());
            boolean bindResult = bindService(Utils.createExplicitFromImplicitIntent(getApplicationContext(), serviceIntent), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = bindResult;
        }
    }

    protected void disconnectFromRemoteService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, sensorFragment)
                        .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        DOMOTRIXCommand domotrixCommand = mSpeechRecognition.recognize(requestCode, data);
        if (domotrixCommand != null) {
            mp = MediaPlayer.create(this, R.raw.computerbeep35);
            mp.start();
            Log.d(TAG,"Command :"+domotrixCommand.getCommand());
            Log.d(TAG,"Controller :"+domotrixCommand.getController());
            Log.d(TAG,"Mode :"+domotrixCommand.getMode());
            Log.d(TAG,"Location :"+domotrixCommand.getLocation());
            Log.d(TAG,"Start in :"+domotrixCommand.getStart()+" ms");
            if (mService != null) {
                Sensor sensor = new Sensor("com.domotrix.sensor", new RecognitionData(
                        domotrixCommand.getCommand(),
                        domotrixCommand.getController(),
                        domotrixCommand.getLocation(),
                        domotrixCommand.getMode(),
                        domotrixCommand.getShow(),
                        domotrixCommand.getStart(),
                        domotrixCommand.getQuantity()
                ));
                try {
                    mService.publish("com.domotrix.recognitiondata",JSONMapper.encode(sensor.getData()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "not connected", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            mp = MediaPlayer.create(this, R.raw.denybeep1);
            mp.start();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SensorFragment extends Fragment {
        private IDomotrixService mService = null;
        private SpeechRecognition mSpeechRecognition;

        public static SensorFragment newInstance() {
            SensorFragment fragment = new SensorFragment();
            return fragment;
        }

        public SensorFragment() {}

        public void setService(IDomotrixService service) {
            mService = service;
        }

        public void setSpeechRecognition(SpeechRecognition speechRecognition) {
            mSpeechRecognition = speechRecognition;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sensor, container, false);

            Button btn = (Button)rootView.findViewById(R.id.commandButton);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mService != null) {
                        try {
                            if (mService.isConnected()) {
                                //mService.remoteLog(TAG,"SEND THE MESSAGE VIA WAMP....");
                                //mService.publish("com.myapp.radio","{\"state\":\"on\"}");
                                mSpeechRecognition.start(getActivity(), "DOMOTRIX DEMO");
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No Service Active", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ImageView img1 = (ImageView)rootView.findViewById(R.id.imageButton1);
            img1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mService != null) {
                        try {
                            if (mService.isConnected()) {
                                mService.publish("com.myapp.lights","{\"state\":\"on\",\"location\":\"kitchen\"}");
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No Service Active", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

}
