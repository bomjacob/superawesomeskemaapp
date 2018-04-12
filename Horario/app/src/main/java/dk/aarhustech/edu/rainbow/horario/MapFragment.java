package dk.aarhustech.edu.rainbow.horario;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dekoservidoni.omfm.OneMoreFabMenu;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snatik.polygon.Point;
import com.snatik.polygon.Polygon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static dk.aarhustech.edu.rainbow.horario.TimetableFragment.today;

public class MapFragment extends Fragment implements WifiLocator.OnLocationCallback, SingleFingerSwipeRefreshLayout.OnChildScrollUpCallback, OneMoreFabMenu.OptionsClick, MapView.OnClickLocationListener, ShowFor {
    private static final String TAG = MapFragment.class.getSimpleName();

    private SingleFingerSwipeRefreshLayout swipeRefresh;
    private WifiLocator wifiLocator;
    private MapView mapView;

    private HashMap<String, Room> rooms;
    private OneMoreFabMenu fabMenu;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.clearDisappearingChildren();
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(true);

        mapView = (MapView) root.findViewById(R.id.map);
        mapView.setMinimumDpi(160);
        mapView.setDoubleTapZoomDpi(200);
        setFloor("D2100");

        mapView.setOnClickLocationListener(this);

        fabMenu = (OneMoreFabMenu) root.findViewById(R.id.fab);
        fabMenu.setOptionsClick(this);

        swipeRefresh = (SingleFingerSwipeRefreshLayout) root.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
                new SingleFingerSwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );
        swipeRefresh.setOnChildScrollUpCallback(this);

        wifiLocator = ((MainActivity) getActivity()).wifiLocator;
        wifiLocator.setThreshold(0.0);

        AssetManager assets = getActivity().getAssets();
        Reader reader;
        try {
            reader = new InputStreamReader(assets.open("rooms.json"), "UTF-8");
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Room>>() {
            }.getType();
            this.rooms = gson.fromJson(reader, type);
        } catch (IOException e) {
            Log.e(TAG, "Can't load rooms.json", e);
        }

        refresh();

        return root;
    }

    @Override
    public void onClickLocation(PointF point) {
        for (Room room : rooms.values()) {
            if (room.containsPoint(point.x, point.y) && room.isOnFloor(mapView.getFloor())) {
                ((MainActivity) getActivity()).timetableAPI.getRoom(today(), room.name.split("_")[0], false).enqueue(new UpdateTimetableCallback());
            }
        }
    }

    public void setFloor(String name) {
        mapView.setFloor(name);
    }

    public void setRooms(Room[] rooms) {
        mapView.setRooms(rooms);
    }

    public void showForRoom(String room, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getRoom(today(), room, force).enqueue(new UpdateTimetableCallback());
    }

    public void showForTeacher(String teacher, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getTeacher(today(), teacher, force).enqueue(new UpdateTimetableCallback());
    }

    public void showForGroup(String group, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getGroup(today(), group, force).enqueue(new UpdateTimetableCallback());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Map - Dollerupvej 2");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        swipeRefresh.setRefreshing(true);
        wifiLocator.scan();
    }

    @Override
    public void onLocation(List<WifiLocator.RoomResult> roomResults) {
        if (roomResults != null) {
            ArrayList<Room> rooms = new ArrayList<>();
            for (Room room : this.rooms.values()) {
                if (room.name.startsWith(roomResults.get(0).name)) {
                    rooms.add(room);
                }
            }
            if (rooms.size() > 0) {
                setRooms(rooms.toArray(new Room[rooms.size()]));
            }
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
        //return true;
        return mapView.sourceToViewCoord(0, 0).y < 0.0;
    }

    @Override
    public void onOptionClick(Integer optionId) {
        switch (optionId) {
            case R.id.floor_D2100:
                this.setFloor("D2100");
                break;
            case R.id.floor_D2200:
                this.setFloor("D2200");
                break;
            case R.id.floor_D2300:
                this.setFloor("D2300");
                break;
        }
    }

    private class UpdateTimetableCallback implements Callback<Timetable> {

        private TimetableInnerFragment fragment;

        UpdateTimetableCallback() {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            Fragment prev = getChildFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                //ft.remove(prev);
                ((DialogFragment) prev).dismiss();
                //getChildFragmentManager().popBackStack();
            }
            ft.addToBackStack(null);


            fragment = TimetableInnerFragment.newInstance();
            fragment.show(ft, "dialog");
        }

        @Override
        public void onResponse(@NonNull Call<Timetable> call, @NonNull Response<Timetable> response) {
            Timetable timetable = response.body();
            fragment.setTimetable(timetable);
        }

        @Override
        public void onFailure(@NonNull Call<Timetable> call, @NonNull Throwable t) {
            Log.e(TAG, "Error when calling: " + call.toString(), t);
        }
    }

    @SuppressWarnings({"unused", "MismatchedReadAndWriteOfArray"})
    class Room {
        private double[][] points;
        private String name;

        private Polygon poly;

        String getFloor() {
            return name.substring(0, 3) + "00";
        }

        PointF[] getPoints() {
            ArrayList<PointF> points = new ArrayList<>();
            for (double[] point : this.points) {
                points.add(new PointF((float) point[0], (float) point[1]));
            }
            return points.toArray(new PointF[points.size()]);
        }

        boolean containsPoint(double x, double y) {
            if (poly == null) {
                Polygon.Builder builder = Polygon.Builder();
                for (double[] point : this.points) {
                    builder.addVertex(new Point(point[0], point[1]));
                }
                poly = builder.build();
            }
            return poly.contains(new Point(x, y));
        }

        boolean isOnFloor(String floor) {
            return name.startsWith(floor.substring(0, 3));
        }
    }
}
