package com.example.ravi.tritonstemplate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mmi.MapView;
import com.mmi.MapmyIndiaMapView;
import com.mmi.apis.distance.Distance;
import com.mmi.apis.distance.DistanceListener;
import com.mmi.apis.distance.DistanceManager;
import com.mmi.apis.routing.DirectionListener;
import com.mmi.apis.routing.DirectionManager;
import com.mmi.apis.routing.Trip;
import com.mmi.layers.MapEventsReceiver;
import com.mmi.layers.Marker;
import com.mmi.layers.PathOverlay;
import com.mmi.util.GeoPoint;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

public class PathGuide extends AppCompatActivity implements MapEventsReceiver {

    static Context c;
    ArrayList<Integer> distances = new ArrayList<>();
    ArrayList<GeoPoint> path = new ArrayList<>();
    long temp = 99999999, l = 1, cost = 0;
    int i,j,placesSize,v;
    static long[][] a = new long[1000][1000];
    int[] k = new int[1000];
    ArrayList<GeoPoint> places=new ArrayList<>();
    ArrayList<String> placenames=new ArrayList<>();
    long dist;
    Fragment fragment_obj;
    MapView mMapView;
    GeoPoint currenLoc;


    //ArrayList<GeoPoint> path=new ArrayList<>();

    PathGuide() {

    }

    PathGuide(Context c,MapView mMapView) {
        this.c = c;
        this.mMapView=mMapView;
        //mMapView = mapMyIndiaMapView.getMapView();
    }


    void pathGenerator(ArrayList<GeoPoint> arrayList,GeoPoint geo) {
        fragment_obj = getFragmentManager().findFragmentByTag("bottom_sheet");
        placesSize=arrayList.size();
        for (i = 0; i < arrayList.size(); i++)
            places.add(arrayList.get(i));
        currenLoc=geo;
        for (i = 0; i < arrayList.size(); i++)
            k[i] = 0;
        for (i = 0; i < arrayList.size(); i++) {
            // ListIterator<GeoPoint> listplaces = arrayList.listIterator();
            for (j = 0; j < arrayList.size(); j++) {
                new getDistance().execute(arrayList.get(i), arrayList.get(j));
                //a[i][j]=getDistance(arrayList.get(i),arrayList.get(j));
            }
        }
        //Log.e("task status", a[placesSize - 1][placesSize - 2] + "array formed in pathGenerator");
       // return path;
    }

    long getDistance(GeoPoint startPoint,GeoPoint endPoint)
    {
        ArrayList<GeoPoint> endpoint=new ArrayList<>();
        endpoint.add(endPoint);
        try {
            new DistanceManager().getDistance(startPoint, endpoint, new DistanceListener() {
                @Override
                public void onResult(final int code, final ArrayList<Distance> distances) {
                    if (code == 0) {
                        if (distances != null) {
                            //Toast.makeText(c, "duration= " + distances.get(0).getDuration() + " distance= " + distances.get(0).getDistance(), Toast.LENGTH_LONG).show();
                          //  dist = distances.get(0).getDuration();
                        }
                    } else {
                        // Toast.makeText(c, "code=" + code, Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        return dist;
    }

    private class getDistance extends AsyncTask<GeoPoint, Void, Integer> {

        int iPre=i,jPre=j;

        @Override
        protected Integer doInBackground(GeoPoint... params) {
            dist=0;
            GeoPoint startPoint=params[0];
            ArrayList<GeoPoint> endpoint=new ArrayList<>();
            endpoint.add(params[1]);
            Log.e("dist:","entered");

            try {
                DistanceManager distanceManager=new DistanceManager();
               if(MainActivity.route.equals("Quickest"))
                distanceManager.setRouteType(DistanceManager.RouteType.QUICKEST);
               else
                   distanceManager.setRouteType(DistanceManager.RouteType.SHORTEST);
               if(MainActivity.vehicle.equals("passenger"))
                   distanceManager.setVehicleType(DistanceManager.VehicleType.PASSENGER);
               else
                   distanceManager.setVehicleType(DistanceManager.VehicleType.TAXI);
                distanceManager.getDistance(startPoint, endpoint, new DistanceListener() {
                    @Override
                    public void onResult(final int code, final ArrayList<Distance> distances) {
                        if (code == 0) {
                            if (distances != null) {
                                //Toast.makeText(c, "duration= " + distances.get(0).getDuration() + " distance= " + distances.get(0).getDistance(), Toast.LENGTH_LONG).show();
                                dist = distances.get(0).getDistance();
                                //Log.e("dist:",dist+"");
                            }
                        } else {
                            // Toast.makeText(c, "code=" + code, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if(iPre==jPre)
            return (int)dist;
            else
                if(dist!=0)
                    return (int)dist;
                else
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            return (int)dist;
        }

        @Override
        protected void onPostExecute(Integer result) {

            a[iPre][jPre]=result;
            Log.e("task status", iPre+" "+jPre+" "+a[iPre][jPre] + "array formed after post");
            if(iPre==placesSize-1&&jPre==placesSize-1)
                pathSet();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    void pathSet()
    {
        Log.e("task status", a[placesSize - 1][placesSize - 2] + "array formed in pathSet");
        i = 0;
        path.add(places.get(i));
        k[i] = 1;
        while (true) {
            if (l == places.size())
                break;
            for (j = 0; j < places.size(); j++) {
                if (a[i][j] != 0 && k[j] != 1) {
                    if (temp > a[i][j]) {
                        temp = a[i][j];
                        v = j;
                    }
                }
            }
            if (temp != 99999999) {
                cost += temp;
                temp = 99999999;
            }
            placenames.add(MainActivity.placeList.get(v-1));
            path.add(places.get(v));
            Log.e(i+1+"",v+1+"");
            //printf("%d--->%d\n",i+1,v+1);
            i = v;
            k[v] = 1;
            l++;
        }
        MainActivity.t.setText("");
        MainActivity.t.append("your Location"+"\n\t\t|\n");
        for (j = 0; j < placenames.size(); j++) {
            if(j==placenames.size()-1)
                MainActivity.t.append(placenames.get(j) + "\n\t\t");
            else
                MainActivity.t.append(placenames.get(j) + "\n\t\t|\n");
        }
        Log.e("places",MainActivity.t.getText()+"");
        DrawPath(path);
    }

    void DrawPath(ArrayList<GeoPoint> path)
    {
        ListIterator<GeoPoint> listplaces = path.listIterator();
        GeoPoint tgeoPoint = currenLoc;
        while (listplaces.hasNext()) {
            try {
                DirectionManager dm = new DirectionManager();
                if(MainActivity.route.equals("Quickest"))
                    dm.setRouteType(DirectionManager.RouteType.QUICKEST);
                else
                    dm.setRouteType(DirectionManager.RouteType.SHORTEST);
                ArrayList<GeoPoint> viaPoints = null;
                final GeoPoint startPoint = tgeoPoint, endPoint = listplaces.next();
                dm.getDirections(startPoint, endPoint, viaPoints, new DirectionListener() {
                    @Override
                    public void onResult(int code, final ArrayList<Trip> trips) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //clearOverlays();

                                if (trips != null) {
                                    addPolyline(trips.get(0).getPath(),mMapView);
                                    addMarker(startPoint);
                                    addMarker(endPoint);
                                }
                            }
                        });
                    }
                });
                tgeoPoint = endPoint;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addMarker(GeoPoint startPoint) {
        MainActivity m=new MainActivity();
        m.addMarker(startPoint,mMapView);
    }

    private void addPolyline(ArrayList<GeoPoint> path,MapView mMapView) {
        MainActivity m=new MainActivity();
        m.addPolyline(path,mMapView);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

}


