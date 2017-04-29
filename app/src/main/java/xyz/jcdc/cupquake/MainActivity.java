package xyz.jcdc.cupquake;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jcdc.cupquake.model.Feature;
import xyz.jcdc.cupquake.model.FruitQuake;
import xyz.jcdc.cupquake.model.Geometry;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int FILTER_TODAY = 0;
    public static final int FILTER_LAST_DAY = 1;
    public static final int FILTER_LAST_WEEK = 2;
    public static final int FILTER_LAST_MONTH = 3;
    public static final int FILTER_LAST_YEAR = 4;
    public static final int FILTER_ALL = 5;

    private Context context;

    private MaterialDialog filterDialog, loadingDialog;

    private int filterValue = FILTER_TODAY;

    private GoogleMap googleMap;

    private FruitQuake.GetFruitQuake getFruitQuake;

    private List<Marker> quakeMarkers = new ArrayList<>();

    private BitmapDescriptor marker_great, marker_major, marker_strong, marker_moderate, marker_light, marker_minor;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initializeTasks();

        marker_great = BitmapDescriptorFactory.fromResource(R.mipmap.marker_great);
        marker_major = BitmapDescriptorFactory.fromResource(R.mipmap.marker_major);
        marker_strong = BitmapDescriptorFactory.fromResource(R.mipmap.marker_strong);
        marker_moderate = BitmapDescriptorFactory.fromResource(R.mipmap.marker_moderate);
        marker_light = BitmapDescriptorFactory.fromResource(R.mipmap.marker_light);
        marker_minor = BitmapDescriptorFactory.fromResource(R.mipmap.marker_minor);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            showFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.subtle_grayscale));
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        getFruitQuake.execute();

    }


    private void addMarkers(List<Feature> features) {
        for (Marker marker : quakeMarkers) {
            marker.remove();
        }
        quakeMarkers.clear();

        for (Feature feature : features) {
            if (feature != null) {
                if (feature.getGeometry() != null) {
                    Geometry geometry = feature.getGeometry();
                    LatLng quake_latlng = new LatLng(geometry.getCoordinates().get(1), geometry.getCoordinates().get(0));

                    if (feature.getProperties() != null) {
                        Log.d("MainActivity", "Magnitude: " + feature.getProperties().getMag());
                        quakeMarkers.add(googleMap.addMarker(new MarkerOptions().position(quake_latlng)
                                .title(feature.getProperties().getTitle())
                                .anchor(0.5f,0.5f)
                                .icon( feature.getProperties().getMag() != null ? getIcon(feature.getProperties().getMag()) : marker_minor)
                                .snippet(feature.getProperties().getPlace())));
                    }

                }
            }

        }

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(1.0f));
    }

    private BitmapDescriptor getIcon(double magnitude) {
        if (magnitude <= 3.9) {
            return marker_minor;
        } else if (magnitude >= 4 && magnitude <= 4.9) {
            return marker_light;
        } else if (magnitude >= 5 && magnitude <= 5.9) {
            return marker_moderate;
        } else if (magnitude >= 6 && magnitude <= 6.9) {
            return marker_strong;
        } else if (magnitude >= 7 && magnitude <= 7.9) {
            return marker_major;
        } else if (magnitude >= 8) {
            return marker_great;
        } else {
            return marker_minor;
        }

    }

    private void showFilterDialog() {
        filterDialog = new MaterialDialog.Builder(this)
                .title("Filter")
                .items(R.array.filter)
                .itemsCallbackSingleChoice(filterValue, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (filterValue != which) {
                            filterValue = which;
                            refresh(); //Nakakalito to ah
                        }

                        return true;
                    }
                })
                .positiveText("Apply")
                .show();
    }

    private void refresh() {
        initializeTasks();
        getFruitQuake.execute();
    }

    private void initializeTasks() {
        cancelTasks();

        getFruitQuake = new FruitQuake.GetFruitQuake(filterValue, new FruitQuake.FruitQuakeListener() {
            @Override
            public void onStartQuaking() {
                loadingDialog = new MaterialDialog.Builder(context)
                        .title("Loading")
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .content("Please wait")
                        .progress(true, 0)
                        .show();
            }

            @Override
            public void onQuake(FruitQuake fruitQuake) {
                loadingDialog.dismiss();
                if (fruitQuake != null) {
                    Log.d("MainActivity", "Count: " + fruitQuake.getMetadata().getCount());
                    addMarkers(fruitQuake.getFeatures());
                } else
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelTasks() {
        if (getFruitQuake != null)
            getFruitQuake.cancel(true);
    }
}
