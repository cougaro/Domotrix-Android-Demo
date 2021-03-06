package com.domotrix.domotrixdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
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
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.domotrix.android.services.IDomotrixService;
import com.domotrix.android.services.IDomotrixServiceListener;
import com.domotrix.domotrixdemo.sensors.RecognitionData;
import com.domotrix.domotrixdemo.sensors.Sensor;
import com.domotrix.language.DOMOTRIXCommand;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private final static String TAG = "DEMO";
    private SpeechRecognition mSpeechRecognition = new SpeechRecognition();
    private MediaPlayer mp;
    private TextToSpeech tts;

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
    TVFragment tvFragment = TVFragment.newInstance();
    CameraFragment cameraFragment = CameraFragment.newInstance();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Setup text to speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ITALY);
                }
            }
        });

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    ;

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        connectToRemoteService(getApplicationContext());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.domotrix.domotrixdemo/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.domotrix.domotrixdemo/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        disconnectFromRemoteService();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
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
            if (tvFragment != null) {
                tvFragment.setService(mService);
            }
            if (cameraFragment != null) {
                cameraFragment.setService(mService);
            }
            try {
                mService.remoteLog(TAG, "=======================================");
                mService.remoteLog(TAG, "=======================================");
                mService.remoteLog(TAG, "APP CLIENT - CONNECTED");
                mService.remoteLog(TAG, "=======================================");
                mService.remoteLog(TAG, "=======================================");
                String sdkVersion = mService.getVersion();
                Log.d(TAG, "SDK Version :" + sdkVersion);

                // Example Subscribe
                mService.subscribe("com.demo.beat", new IDomotrixServiceListener() {
                    @Override
                    public void onMessage(String wampEvent, String jsonMessage) throws RemoteException {
                        Log.d(TAG,"*****************************************************");
                        Log.d(TAG,"MESSAGE RECEIVED");
                        Log.d(TAG, wampEvent);
                        Log.d(TAG, jsonMessage);
                        Log.d(TAG,"*****************************************************");
                    }

                    @Override
                    public void onFault(String description) throws RemoteException {
                        Log.e(TAG,"*****************************************************");
                        Log.e(TAG, description);
                        Log.e(TAG,"*****************************************************");
                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });


            } catch (RemoteException e) {
                Log.e(TAG, "ERROR", e);
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
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, tvFragment)
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, cameraFragment)
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
        if (id == R.id.action_voice) {
            if (mService != null) {
                try {
                    if (mService.isConnected()) {
                        //mService.remoteLog(TAG,"SEND THE MESSAGE VIA WAMP....");
                        //mService.publish("com.myapp.radio","{\"state\":\"on\"}");
                        mSpeechRecognition.start(this, "DOMOTRIX DEMO");
                    } else {
                        if (tts != null) tts.speak("nessuna connessione", TextToSpeech.QUEUE_FLUSH, null);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                if (tts != null) tts.speak("servizio non attivo", TextToSpeech.QUEUE_FLUSH, null);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DOMOTRIXCommand domotrixCommand = mSpeechRecognition.recognize(requestCode, data);
        if (domotrixCommand != null) {
            //mp = MediaPlayer.create(this, R.raw.computerbeep35);
            //mp.start();
            Log.d(TAG, "Command :" + domotrixCommand.getCommand());
            Log.d(TAG, "Controller :" + domotrixCommand.getController());
            Log.d(TAG, "Mode :" + domotrixCommand.getMode());
            Log.d(TAG, "Location :" + domotrixCommand.getLocation());
            Log.d(TAG, "Start in :" + domotrixCommand.getStart() + " ms");
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
                    mService.publish("com.domotrix.recognitiondata", JSONMapper.encode(sensor.getData()));
                    mService.speech("eseguo.");
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
            //mp = MediaPlayer.create(this, R.raw.denybeep1);
            //mp.start();
            try {
                mService.speech("scusa, non ho capito.");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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

    public static class SensorFragment extends Fragment {
        private Context fragmentContext = null;
        private IDomotrixService mService = null;
        private SpeechRecognition mSpeechRecognition;

        public static SensorFragment newInstance() {
            SensorFragment fragment = new SensorFragment();
            return fragment;
        }

        protected SensorFragment() {}

        public void setService(IDomotrixService service) {
            mService = service;
        }

        public void setSpeechRecognition(SpeechRecognition speechRecognition) {
            mSpeechRecognition = speechRecognition;
        }

        private class LightClickListener implements View.OnClickListener {
            String state = null;
            String where = null;

            public LightClickListener(String state, String where) {
                this.state = state;
                this.where = where;
            }

            @Override
            public void onClick(View v) {
                if (mService != null) {
                    try {
                        if (mService.isConnected()) {
                            mService.publish("com.myapp.lights", "{\"state\":\""+state+"\",\"location\":\""+where+"\"}");
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
        }

        private class ChooseColorListener implements View.OnLongClickListener {
            String where = null;

            public ChooseColorListener(String where) {
                this.where = where;
            }

            @Override
            public boolean onLongClick(View v) {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(fragmentContext, Color.WHITE, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (mService != null) {
                            try {
                                if (mService.isConnected()) {
                                    mService.publish("com.myapp.lights", "{\"color\":\""+color+"\",\"location\":\""+where+"\"}");
                                } else {
                                    Toast.makeText(fragmentContext, "No connection", Toast.LENGTH_SHORT).show();
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(fragmentContext, "No Service Active", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
                dialog.show();

                return true;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sensor, container, false);

            fragmentContext = container.getContext();

            ImageView img0 = (ImageView) rootView.findViewById(R.id.imageButton0);
            img0.setOnClickListener(new LightClickListener("off", "all"));

            ImageView img0B = (ImageView) rootView.findViewById(R.id.imageButton0B);
            img0B.setOnClickListener(new LightClickListener("on", "all"));
            img0B.setOnLongClickListener(new ChooseColorListener("all"));

            ImageView img1 = (ImageView) rootView.findViewById(R.id.imageButton1);
            img1.setOnClickListener(new LightClickListener("off", "kitchen"));

            ImageView img1B = (ImageView) rootView.findViewById(R.id.imageButton1B);
            img1B.setOnClickListener(new LightClickListener("on","kitchen"));
            img1B.setOnLongClickListener(new ChooseColorListener("kitchen"));

            ImageView img2 = (ImageView) rootView.findViewById(R.id.imageButton2);
            img2.setOnClickListener(new LightClickListener("off", "bathroom"));

            ImageView img2B = (ImageView) rootView.findViewById(R.id.imageButton2B);
            img2B.setOnClickListener(new LightClickListener("on","bathroom"));
            img2B.setOnLongClickListener(new ChooseColorListener("bathroom"));

            ImageView img3 = (ImageView) rootView.findViewById(R.id.imageButton3);
            img3.setOnClickListener(new LightClickListener("off", "bedroom"));

            ImageView img3B = (ImageView) rootView.findViewById(R.id.imageButton3B);
            img3B.setOnClickListener(new LightClickListener("on","bedroom"));
            img3B.setOnLongClickListener(new ChooseColorListener("bedroom"));

            ImageView img4 = (ImageView) rootView.findViewById(R.id.imageButton4);
            img4.setOnClickListener(new LightClickListener("off","corridor"));

            ImageView img4B = (ImageView) rootView.findViewById(R.id.imageButton4B);
            img4B.setOnClickListener(new LightClickListener("on","corridor"));
            img4B.setOnLongClickListener(new ChooseColorListener("corridor"));

            ImageView img5 = (ImageView) rootView.findViewById(R.id.imageButton5);
            img5.setOnClickListener(new LightClickListener("off","dining"));

            ImageView img5B = (ImageView) rootView.findViewById(R.id.imageButton5B);
            img5B.setOnClickListener(new LightClickListener("on","dining"));
            img5B.setOnLongClickListener(new ChooseColorListener("dining"));

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

    public static class TVFragment extends Fragment {
        private IDomotrixService mService = null;

        public static TVFragment newInstance() {
            TVFragment fragment = new TVFragment();
            return fragment;
        }

        private TVFragment() {}

        public void setService(IDomotrixService service) {
            mService = service;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tv, container, false);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

    public static class CameraFragment extends Fragment {
        private IDomotrixService mService = null;

        public static CameraFragment newInstance() {
            CameraFragment fragment = new CameraFragment();
            return fragment;
        }

        private CameraFragment() {}

        public void setService(IDomotrixService service) {
            mService = service;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

            WebView webview = (WebView) rootView.findViewById(R.id.webview);
            /*
            webview.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    String data = "<html><body style=\"margin: 0; padding: 0\">" + "<img src=\"noise.gif\" width=\"100%\" height=\"100%\" /></body></html>";
                    view.loadDataWithBaseURL("file:///android_asset/",data , "text/html", "utf-8",null);
                }
            });
            */
            webview.loadUrl("http://192.168.0.103:81/videostream.cgi?loginuse=admin&loginpas=");

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }


}
