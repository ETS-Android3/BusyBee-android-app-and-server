package com.zubov.android.bzb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.zubov.android.bzb.fragments.DescriptionFragment;
import com.zubov.android.bzb.model.DTO.MarkerDTO;
import com.zubov.android.bzb.model.DTO.OrderDTO;
import com.zubov.android.bzb.model.DTO.OrderResponseDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
DescriptionFragment.OnFragmentInteractionListener
{

    private FrameLayout fragmentContainer;

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String EMAIL = "user_email";
    public static final String TOKEN = "token";
    public static final String DESCRIPTION = "description_order";
    public static final String ORDER_ID = "order_id";

    private static final String CAM_POS_LAT = "LastCameraPositionLatitude";
    private static final String CAM_POS_LON = "LastCameraPositionLongitude";
    private static final String CAM_ZOOM = "LastCameraZoom";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private FloatingActionButton btnPlus;
    private IMapController mapController;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocationManager;
    private List<Marker> mMarkersList = new ArrayList<>();
    private Double lastCenterLat;
    private Double lastCenterLon;
    private boolean FlagToCurrentLocation;
    int addingMode;
    Marker marker;
    Marker mMarkerStart;
    int addedOrderId;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //handle permissions first, before map is created. not depicted here
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setZoomInEnabled(true);
        map.getZoomController().setZoomOutEnabled(true);
        mapController = map.getController();
        mapController.setZoom(15f);
        btnPlus = (FloatingActionButton) findViewById(R.id.floatingActionButton);



        requestPermissionsIfNecessary(new String[] {
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), map);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);
        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                addingMode = 0;
            } else {
                addingMode = extras.getInt("add");
            }
        }

        if(savedInstanceState != null){
            addingMode = savedInstanceState.getInt("add",0);
            lastCenterLat = savedInstanceState.getDouble(CAM_POS_LAT,0);
            lastCenterLon = savedInstanceState.getDouble(CAM_POS_LON,0);
            mapController.animateTo(new GeoPoint(lastCenterLat,lastCenterLon));
            double lastZoom = savedInstanceState.getDouble(CAM_ZOOM,0);
            mapController.setZoom(lastZoom);
        }
        else {
            mLocationOverlay.enableFollowLocation();
        }

        mLocationManager = (LocationManager) getSystemService(ctx.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        placeMarkers();

        fragmentContainer =(FrameLayout) findViewById(R.id.fragment_container);

        //initialize & assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set map selected
        bottomNavigationView.setSelectedItemId(R.id.nav_map);
        // Perform itemSelectListener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    //check id
                    case R.id.nav_user:
                        startActivity(new Intent(ctx,PersonActivity.class));
                        //overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_map:
                        startActivity(new Intent(ctx,MainActivity.class));
                        return true;

                }
                return false;
            }
        });

        // begin our work with "adding_order" mode
        if (addingMode == 1) {
            FlagToCurrentLocation = true;
            mMarkerStart = new Marker(map);
            mMarkerStart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mMarkerStart.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_flag));

            if (lastCenterLat != null && lastCenterLon != null){
                mMarkerStart.setPosition(new GeoPoint(lastCenterLat,lastCenterLon));
                map.getOverlays().add(mMarkerStart);
                FlagToCurrentLocation = false;
            }
            if (FlagToCurrentLocation) {
                Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLoc == null) {
                    lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (lastKnownLoc != null) {
                    GeoPoint curLocation = new GeoPoint(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                    mMarkerStart.setPosition(curLocation);
                    map.getOverlays().add(mMarkerStart);
                }
            }



            btnPlus.show();
            marker = new Marker(map);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_flag));
            Overlay mOverlay = new Overlay() {
                @Override
                public boolean onScroll(MotionEvent pEvent1, MotionEvent pEvent2, float pDistanceX, float pDistanceY, MapView pMapView) {

                    mMarkerStart.remove(map);
                    marker.setPosition(new GeoPoint((float) pMapView.getMapCenter().getLatitude(),
                            (float) pMapView.getMapCenter().getLongitude()));

                    return super.onScroll(pEvent1, pEvent2, pDistanceX, pDistanceY, pMapView);
                }
            };
            map.getOverlays().add(mOverlay);
            map.getOverlays().add(marker);
        } else {
            btnPlus.hide();
        }

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askConfirmationDialog();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        mLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
        mLocationOverlay.disableMyLocation();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(CAM_POS_LAT,map.getMapCenter().getLatitude());
        outState.putDouble(CAM_POS_LON,map.getMapCenter().getLongitude());
        outState.putDouble(CAM_ZOOM,map.getZoomLevelDouble());
        if (addingMode == 1) {
            outState.putInt("add",1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void placeMarkers(){
        NetworkService.getInstance()
                .getJSONApi()
                .getAllOrders()
                .enqueue(new Callback<List<MarkerDTO>>() {

                    @Override
                    public void onResponse(Call<List<MarkerDTO>> call,
                                           Response<List<MarkerDTO>> response) {
                        if (!response.isSuccessful()){
                            new Toaster(ctx,String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        List<MarkerDTO> markerList = response.body();
                        for (MarkerDTO m : markerList) {
                            Marker tmp = new Marker(map);
                            tmp.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_location_pin));
                            tmp.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    double latitude = marker.getPosition().getLatitude();
                                    double longitude = marker.getPosition().getLongitude();
                                    openFragment(latitude,longitude);
                                    return true;
                                }
                            });
                            GeoPoint pnt = new GeoPoint(m.getMarkerLatitude(), m.getMarkerLongitude());
                            tmp.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            tmp.setPosition(pnt);
                            addMarkersToList(tmp);
                            map.getOverlays().add(tmp);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MarkerDTO>> call, Throwable t) {
                        new Toaster(ctx,R.string.err_server).ShowToast();
                        t.printStackTrace();
                    }
                });
    }

    public void openFragment(double lat,double lon){
        DescriptionFragment fragment = DescriptionFragment.newInstance(lat,lon);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack("DESCRIPTION_FRAGMENT");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment_container,fragment,"DESCRIPTION_FRAGMENT").commit();
    }

    @Override
    public void onFragmentInteraction() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void askConfirmationDialog(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(R.string.title_approve)
                .setMessage(R.string.up_dialog_add_order_msg)
                .setNegativeButton("нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do
                    }
                })
                .setPositiveButton("да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        placeOrder();
                    }
                });
        AlertDialog dialog = builder1.create();
        dialog.show();
    }

    private void placeOrder(){
        OrderDTO newOrder = new OrderDTO();
        SharedPreferences shearedPrefs = getSharedPreferences(SHEARED_PREFS,MODE_PRIVATE);
        newOrder.setUserEmail(shearedPrefs.getString(EMAIL,""));
        newOrder.setUserToken(shearedPrefs.getString(TOKEN,""));
        newOrder.setMarkerLatitude(marker.getPosition().getLatitude());
        newOrder.setMarkerLongitude(marker.getPosition().getLongitude());
        newOrder.setText(shearedPrefs.getString(DESCRIPTION,""));

        NetworkService
                .getInstance()
                .getJSONApi()
                .addOrder(newOrder)
                .enqueue(new Callback<OrderResponseDTO>() {
                    @Override
                    public void onResponse(Call<OrderResponseDTO> call, Response<OrderResponseDTO> response) {
                        if (!response.isSuccessful()){
                            new Toaster(ctx,String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        OrderResponseDTO orderResponseObj = response.body();
                        if (orderResponseObj != null){
                            addedOrderId = orderResponseObj.getOrderID();
                        }
                        if (orderResponseObj == null || addedOrderId == 0) {
                            new Toaster(ctx,R.string.token_null).ShowToast();
                        }
                        else{
                            SharedPreferences.Editor editor = shearedPrefs.edit();
                            editor.putString(DESCRIPTION,"");
                            editor.apply();
                            new Toaster(ctx,R.string.success_to_add_order).ShowToast();
                            redirectToUserPage(1);
                        }

                    }

                    @Override
                    public void onFailure(Call<OrderResponseDTO> call, Throwable t) {
                        t.printStackTrace();
                        new Toaster(ctx,R.string.fail_to_add_order).ShowToast();
                        redirectToUserPage(0);
                    }
                });
    }

    private void redirectToUserPage(int isSuccess){
        Intent intent = new Intent(ctx,PersonActivity.class);
        if(isSuccess == 1){
            intent.putExtra(ORDER_ID,addedOrderId);
        }
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private void addMarkersToList(Marker m){
        mMarkersList.add(m);
    }

}