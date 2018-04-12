package dk.aarhustech.edu.rainbow.horario;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback, WifiLocator.OnLocationCallback {
    static final int PERMISSION_REQUEST_STORAGE = 1;
    static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    WifiLocator wifiLocator;
    TimetableAPI timetableAPI;
    private MenuItem previousMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        updateAdvancedVisibility();

        wifiLocator = new WifiLocator(this, this);

        final GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer())
                .registerTypeAdapter(Timetable.Person.class, new Timetable.PersonSerializer());
        final Gson gson = builder.create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.getString(R.string.API_URL))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(GsonStringConverterFactory.create(gson))
                .build();

        timetableAPI = retrofit.create(TimetableAPI.class);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        if (savedInstanceState != null) {
            fragment = fragmentManager.getFragment(savedInstanceState, "fragment");
        }
        if (fragment == null) {
            fragment = new TimetableFragment();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();

    }

    public void updateAdvancedVisibility() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        navigationView.getMenu().findItem(R.id.nav_rooms).setVisible(preferences.getBoolean("pref_advanced", false));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.location_perm_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.location_perm_denied, Toast.LENGTH_SHORT).show();
                this.finish();
            }
        } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.storage_perm_granted, Toast.LENGTH_SHORT).show();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("TOP");
                if (fragment instanceof RoomsFragment) ((RoomsFragment) fragment).startAddRoom();
            } else {
                Toast.makeText(this, R.string.storage_perm_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        wifiLocator.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        wifiLocator.unregister(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("pref_advanced", false)) {
            getMenuInflater().inflate(R.menu.debug, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_debug_set_room) {
            debugSetRoom();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void debugSetRoom() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set room");
        alert.setMessage("Example: D2265");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String room = input.getText().toString();
                List<WifiLocator.RoomResult> result = new ArrayList<>();
                result.add(new WifiLocator.RoomResult(room, 1.0));
                onLocation(result);
            }
        });

        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_help) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getResources().getString(R.string.url_help)));
            startActivity(i);
        } else {
            item.setChecked(true);
            if (previousMenuItem != null) {
                previousMenuItem.setChecked(false);
            }
            previousMenuItem = item;


            Fragment fragment;
            if (id == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else if (id == R.id.nav_rooms) {
                fragment = new RoomsFragment();
            } else {
                fragment = new TimetableFragment();
            }

            Log.d(TAG, fragment.toString());

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocation(List<WifiLocator.RoomResult> rooms) {
        if (rooms == null) {
            Snackbar.make(findViewById(R.id.coordinator), R.string.no_school_aps_found, 2000).show();
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if (fragment != null && fragment.isVisible() && fragment instanceof WifiLocator.OnLocationCallback) {
            ((WifiLocator.OnLocationCallback) fragment).onLocation(rooms);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment topFragment = fragmentManager.findFragmentById(R.id.content);
        if (topFragment != null) {
            fragmentManager.putFragment(outState, "fragment", topFragment);
        }
    }
}
