package com.example.ravi.tritonstemplate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mmi.LicenceManager;
import com.mmi.MapView;
import com.mmi.MapmyIndiaMapView;
import com.mmi.apis.place.Place;
import com.mmi.apis.place.autosuggest.AutoSuggest;
import com.mmi.apis.place.autosuggest.AutoSuggestListener;
import com.mmi.apis.place.autosuggest.AutoSuggestManager;
import com.mmi.apis.place.details.PlaceDetailsListener;
import com.mmi.apis.place.details.PlaceDetailsManager;
import com.mmi.apis.place.geocoder.GeocodeListener;
import com.mmi.apis.place.geocoder.GeocodeManager;
import com.mmi.apis.place.reversegeocode.ReverseGeocodeListener;
import com.mmi.apis.place.reversegeocode.ReverseGeocodeManager;
import com.mmi.apis.routing.DirectionListener;
import com.mmi.apis.routing.DirectionManager;
import com.mmi.apis.routing.Trip;
import com.mmi.events.MapListener;
import com.mmi.events.ScrollEvent;
import com.mmi.events.ZoomEvent;
import com.mmi.layers.MapEventsOverlay;
import com.mmi.layers.MapEventsReceiver;
import com.mmi.layers.Marker;
import com.mmi.layers.PathOverlay;
import com.mmi.layers.UserLocationOverlay;
import com.mmi.layers.location.GpsLocationProvider;
import com.mmi.util.GeoPoint;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.example.ravi.tritonstemplate.PathGuide;

public class MainActivity extends FragmentActivity implements MapEventsReceiver  {

    MapView mMapView = null;
    Context c;
    UserLocationOverlay mLocationOverlay;
    double k, l;
    static TextView t;
    AutoCompleteTextView searched;
    GeoPoint geoPoint, tgeoPoint;
    Notification notify;
    ArrayList<String> placenames;
    static ArrayList<String> placeList=new ArrayList<>();
    ArrayList<AutoSuggest> tempPlaces;
    NotificationManager notif;
    String currentLoc="donno";
    static  String vehicle="Taxi",route="Quickest";
    private BottomSheetBehavior mBottomSheetBehavior,mBottomSheetBehavior1;
    MapmyIndiaMapView mapMyIndiaMapView;
    ArrayList<GeoPoint> list = new ArrayList<GeoPoint>();
    Handler locationFoundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                   // mMapView.setCenter(mLocationOverlay.getMyLocation());
                    k = mLocationOverlay.getMyLocation().getLongitude();
                    l = mLocationOverlay.getMyLocation().getLatitude();
                    geoPoint = new GeoPoint(l, k);
                    list.add(0,geoPoint);
                    showNotification();
                    //Toast.makeText(MainActivity.this, "OK!! Location Found", Toast.LENGTH_LONG).show();
                   // mMapView.setZoom(mMapView.getMaxZoomLevel());
                    break;

            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return true;
                //case R.id.navigation_dashboard:
                //    CustomBottomSheetDialog bottomSheetDialog1 =new CustomBottomSheetDialog();
                //    bottomSheetDialog1.show(getSupportFragmentManager(), "Custom Bottom Sheet");
                 //   return true;
                case R.id.navigation_notifications:
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LicenceManager.getInstance().setRestAPIKey("MapMyIndia_RestAPI_Key");
        LicenceManager.getInstance().setMapSDKKey("MapMyIndia_SDK_Key");
        mapMyIndiaMapView = findViewById(R.id.map);
        mMapView = mapMyIndiaMapView.getMapView();
        View mLayoutBottomSheet=findViewById(R.id.layout_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mLayoutBottomSheet);
        View mLayoutBottomSheet1=findViewById(R.id.layout_bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(mLayoutBottomSheet1);
        t = mLayoutBottomSheet.findViewById(R.id.text_view_more_content);
        final RadioGroup vehicleType=findViewById(R.id.vehicle);
        RadioGroup routeType=findViewById(R.id.route);
        Button placePref=findViewById(R.id.set);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.floatingActionButton3);
        searched =  findViewById(R.id.editText);
        mMapView.setZoom(mMapView.getMinZoomLevel());

        getCurrentLocation();

        placePref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton vehicleButton=findViewById(vehicleType.getCheckedRadioButtonId());
                if(vehicleButton!=null)
                vehicle = vehicleButton.getText()+"";
                RadioButton routeButton=findViewById(vehicleType.getCheckedRadioButtonId());
                if(routeButton!=null)
                route = routeButton.getText()+"";
                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        searched.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getsuggestionadapter(s+"");

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //getsuggestionadapter(s+"");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //getsuggestionadapter(s+"");
            }
        });

        showBottomSheetDialog();

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(geoPoint==null)
                getCurrentLocation();
                //showNotification();

                if (geoPoint != null) {
                    if (list.size() != 0 && list.size() != 1) {

                        clearPath();

                        PathGuide p=new PathGuide(MainActivity.this,mMapView);

                        p.pathGenerator(list,geoPoint);

                    } else if (list.size() == 1) {
                        try {
                            clearPath();
                            DirectionManager dm = new DirectionManager();
                            if(MainActivity.route.equals("Quickest"))
                                dm.setRouteType(DirectionManager.RouteType.QUICKEST);
                            else
                                dm.setRouteType(DirectionManager.RouteType.SHORTEST);
                            ArrayList<GeoPoint> viaPoints = null;
                            final GeoPoint startPoint = geoPoint, endPoint = list.get(0);
                            dm.getDirections(startPoint, endPoint, viaPoints, new DirectionListener() {
                                @Override
                                public void onResult(int code, final ArrayList<Trip> trips) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //clearOverlays();

                                            if (trips != null) {
                                                addPolyline(trips.get(0).getPath(),mMapView);


                                                addMarker(startPoint,mMapView);
                                                addMarker(endPoint,mMapView);
                                            }
                                        }
                                    });
                                }
                            });


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Add Some places to travel!!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please wait.. we are fetching your current location", Toast.LENGTH_LONG).show();
                    //getCurrentLocation();
                }
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOverlays();
            }
        });

        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });
        mMapView.removeAllViews();
        mMapView.invalidate();
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }


    public void addMarker(GeoPoint point,MapView mMapView) {
        Marker marker = new Marker(mMapView);

        marker.setPosition(point);
        marker.setInfoWindow(null);

        mMapView.getOverlays().add(marker);
        mMapView.postInvalidate();
    }

    public void addPolyline(final ArrayList<GeoPoint> geoPoints,final MapView mMapView) {

            PathOverlay pathOverlay = new PathOverlay(PathGuide.c);
            //Log.e("status","in polyline");
            pathOverlay.setColor(PathGuide.c.getResources().getColor(R.color.colorPrimary));
            //pathOverlay.setColor(1);
            pathOverlay.setWidth(7);
            pathOverlay.setPoints(geoPoints);
            mMapView.getOverlays().add(pathOverlay);
            mMapView.postInvalidate();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMapView.setBounds(geoPoints);
                }
            });
    }

    void clearOverlays() {
        clearPath();
        list = new ArrayList<GeoPoint>();
        list.add(geoPoint);
    }

    void clearPath()
    {
        mMapView.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(MainActivity.this, this);
        mMapView.getOverlays().add(mLocationOverlay);
        mMapView.getOverlays().add(mapEventsOverlay);
    }

    void getsuggestionadapter(String search)
    {
       // Toast.makeText(MainActivity.this,search+"",Toast.LENGTH_LONG).show();
        //searched.setDropDownHeight(5);
        if(search.length()<=1||searched.getThreshold()<5) {
            AutoSuggestManager autoSuggestManager = new AutoSuggestManager();
            autoSuggestManager.getSuggestions(search, new AutoSuggestListener() {
                @Override
                public void onResult(final int code, final ArrayList<AutoSuggest> places) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //    if(places!=null)
                            //        Toast.makeText(MainActivity.this,places.get(0).getPlaceId()+"",Toast.LENGTH_LONG).show();
                            //   else
                            //        Toast.makeText(MainActivity.this,"this isnt working!! "+code,Toast.LENGTH_LONG).show();
                            ArrayAdapter<String> adapter;

                            if (places != null) {

                                //Toast.makeText(MainActivity.this,"ok found",Toast.LENGTH_LONG).show();

                                placenames = new ArrayList<>();
                                tempPlaces = new ArrayList<>();

                                for (AutoSuggest place : places) {
                                    placenames.add(place.getAddr());
                                    tempPlaces.add(place);
                                }

                                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, placenames);

                                searched.setAdapter(adapter);

                                searched.showDropDown();
                            } else {
                                // Toast.makeText(MainActivity.this,"currently facing issues for auto suggest",Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
            }, true);

        }

        searched.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

                AutoSuggest selected = tempPlaces.get(i);
                placeList.add(placenames.get(i));
                  searched.setText(null);
                  //searched.setDropDownHeight(0);

                try {
                    String placeid=selected.getPlaceId();

                    PlaceDetailsManager placeDetailsManager=new PlaceDetailsManager();
                    placeDetailsManager.getPlaceDetails(placeid, new PlaceDetailsListener() {
                        @Override
                        public void onResult(int code,final Place place) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addMarker(place.getGeoPoint(),mMapView);
                                    mMapView.setCenter(place.getGeoPoint());
                                    mMapView.setZoom(mMapView.getMaxZoomLevel()-3);
                                    list.add(place.getGeoPoint());
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    void getCurrentLocation()
    {
        if(geoPoint==null)
        isGPSEnable();

        //Toast.makeText(this,"ok entered",Toast.LENGTH_LONG).show();
        mLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(MainActivity.this), mMapView);
        // mLocationOverlay.setCurrentLocationResId(R.drawable.usrloc);
        mLocationOverlay.enableMyLocation();
        //GeoPoint geoPoint= new GeoPoint(mLocationOverlay.getMyLocationProvider().getLastKnownLocation());
        // mMapView.setCenter(geoPoint);
        mMapView.getOverlays().add(mLocationOverlay);
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                locationFoundHandler.sendEmptyMessage(1);
            }
        });

    }

    public void isGPSEnable(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "I need GPS Access for working!!\nplease turn it on";

            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Close",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    public void showBottomSheetDialog() {

    }

    void showNotification() {
        int res = 0;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        if (geoPoint != null)
            placeFinder(geoPoint);
       // Toast.makeText(MainActivity.this, currentLoc + "", Toast.LENGTH_LONG).show();
        if (!currentLoc .equals("donno")) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this, channelId)
                  //  .setSmallIcon(R.drawable.usrloc)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle("you're traced")
                    .setContentText(currentLoc + "");
            notificationManager.notify(notificationId, mBuilder.build());
        }
       // else
        //    showNotification();
    }

    void placeFinder(GeoPoint geoPoint)
    {
       // final String currentLoc;

        ReverseGeocodeManager reverseGeocodeManager = new ReverseGeocodeManager();
        reverseGeocodeManager.getPlace(geoPoint, new ReverseGeocodeListener() {
            @Override
            public void onResult(int code,final Place place) {
                        if(place!=null)
                            currentLoc = place.getFullAddress();
                        else
                            currentLoc="problem in fetching your location name..";
                    }
        });
        Log.e("task status", "location ");
        while(currentLoc.equals("donno"))
        {

        }
        Log.e("task status", "location 1");

    }

}
