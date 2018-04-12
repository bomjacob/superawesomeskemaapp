package dk.aarhustech.edu.rainbow.horario;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class RoomsFragment extends Fragment implements WifiLocator.OnLocationCallback {
    private static final String TAG = RoomsFragment.class.getSimpleName();
    private TextView result;
    private SwipeRefreshLayout swipeRefresh;
    private WifiLocator wifiLocator;

    public RoomsFragment() {
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
        View root = inflater.inflate(R.layout.fragment_rooms, container, false);
        setHasOptionsMenu(true);

        FloatingActionButton myFab = (FloatingActionButton) root.findViewById(R.id.add);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ensureStoragePermissions()) {
                    startAddRoom();
                }
            }
        });

        swipeRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        result = (TextView) root.findViewById(R.id.result);

        wifiLocator = ((MainActivity) getActivity()).wifiLocator;
        wifiLocator.setThreshold(1.0);

        refresh();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rooms, menu);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Rooms");
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

    void startAddRoom() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(getActivity(), R.string.no_ext_storage_found_cannot_add_room, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity()).setTitle(R.string.add_room);
        final EditText editText = new EditText(getActivity());
        editText.setHint(R.string.room_placeholder);
        builder.setView(editText);
        builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addRoom(editText.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        try {
            //noinspection ConstantConditions
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } catch (NullPointerException e) {
            Log.e(TAG, "Error showing soft keyboard", e);
        }
        dialog.show();
    }

    void addRoom(String name) {
        final ProgressDialog progress = new ProgressDialog(this.getActivity());
        progress.setTitle(getString(R.string.scanning_for_aps));
        progress.setMessage(getString(R.string.please_wait_for_first_fix));
        progress.setCanceledOnTouchOutside(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                wifiLocator.stopAdd();
                Gson gson = new Gson();
                RoomData roomData = wifiLocator.getRoomData();
                String json = gson.toJson(roomData);
                Log.d(TAG, json);
                File parent = new File(Environment.getExternalStorageDirectory(), "horarioRooms");
                if (!parent.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    parent.mkdirs();
                }
                File file = new File(parent, roomData.name + ".json");
                try {
                    Writer writer = new FileWriter(file);
                    writer.write(json);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't write json file", e);
                }
                dialog.dismiss();
            }
        });
        progress.show();

        wifiLocator.startAdd(name, new WifiLocator.OnAddProgress() {
            @Override
            public void onAddProgress(int aps, int dataPoints) {
                progress.setMessage(getString(R.string.scanning_status, aps, dataPoints));
            }
        });
    }

    private boolean ensureStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainActivity.PERMISSION_REQUEST_STORAGE);
            return false;
        }
        return true;
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
    public void onLocation(List<WifiLocator.RoomResult> rooms) {
        if (rooms == null) {
            result.setText(R.string.not_at_school);
        } else {
            result.setText("");

            for (WifiLocator.RoomResult room : rooms) {
                result.append(room.name + " (" + Math.round(room.score * 100) + "%)\n");
            }
        }
        swipeRefresh.setRefreshing(false);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
