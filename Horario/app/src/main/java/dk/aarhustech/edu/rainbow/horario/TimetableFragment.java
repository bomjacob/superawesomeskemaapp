package dk.aarhustech.edu.rainbow.horario;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimetableFragment extends Fragment implements WifiLocator.OnLocationCallback, ShowFor {
    static final int POPUP_INTENT = 2;
    private static final String TAG = TimetableFragment.class.getSimpleName();
    private static final int STUDENT_INTENT = 0;
    private static final int ROOM_INTENT = 1;
    private SwipeRefreshLayout swipeRefresh;
    private WifiLocator wifiLocator;
    private boolean swipeRefreshScheduleDisable;
    private TimetableViewPagerAdapter adapter;
    private ViewPager viewPager;
    private View empty;
    private boolean locatorRefreshing;
    private boolean timetableRefreshing;
    private boolean something;

    public TimetableFragment() {
        // Required empty public constructor
    }

    static LocalDate today() {
        return LocalDate.now();
        // return new LocalDate(2018, 3, 23);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // https://stackoverflow.com/questions/26993853/viewpager-inside-fragment-how-to-retain-state
        adapter = new TimetableViewPagerAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.clearDisappearingChildren();
        View root = inflater.inflate(R.layout.fragment_timetable, container, false);
        setHasOptionsMenu(true);

        viewPager = (ViewPager) root.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        empty = root.findViewById(R.id.timetable_empty);
        setLoading(true);

//        List<Timetable.Lesson> lessons = new ArrayList<>();
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(8, 15), new LocalTime(9, 15), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(9, 35), new LocalTime(10, 35), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(10, 45), new LocalTime(11, 45), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(12, 15), new LocalTime(13, 15), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(13, 25), new LocalTime(14, 25), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//        lessons.add(new Timetable.Lesson("DP, el", new LocalTime(14, 30), new LocalTime(15, 30), new String[]{"tbu", "test"}, new String[]{"15xael"}, new String[]{"D2317"}, null, false));
//
//        adapter.addTimetable(new Timetable(new LocalDate(2018, 3, 10), lessons, "D2317", null));
//        adapter.addTimetable(new Timetable(new LocalDate(2018, 3, 10), lessons, null, "Jasmin Bom"));

        swipeRefresh = (SwipeRefreshLayout) root.findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                        if (swipeRefreshScheduleDisable) {
                            swipeRefresh.setEnabled(false);
                            swipeRefreshScheduleDisable = false;
                        }
                    }
                }
        );

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    swipeRefresh.setEnabled(true);
                } else {
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefreshScheduleDisable = true;
                    } else {
                        swipeRefresh.setEnabled(false);
                    }
                }
            }
        });

        wifiLocator = ((MainActivity) getActivity()).wifiLocator;
        wifiLocator.setThreshold(0.0);

        refresh(true);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.Timetable);
        }

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void refresh(boolean initial) {
        locatorRefreshing = true;
        wifiLocator.scan();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String login = preferences.getString("pref_login", null);
        if (login != null && !login.equals("")) {
            something = true;
            showForStudent(login, STUDENT_INTENT, !initial);
            timetableRefreshing = true;
        }
        updateRefreshing();
    }

    private void refresh() {
        refresh(false);
    }

    void updateRefreshing() {
        swipeRefresh.setRefreshing(locatorRefreshing || timetableRefreshing);
        if (!(locatorRefreshing || timetableRefreshing) && !something) {
            setEmpty(getString(R.string.no_timetables_to_find));
        }
    }

    @Override
    public void onLocation(List<WifiLocator.RoomResult> rooms) {
        locatorRefreshing = false;
        if (rooms != null) {
            showForRoom(rooms.get(0).name, ROOM_INTENT, false);
            something = true;
            timetableRefreshing = true;
        }
        updateRefreshing();
    }

    void showForTeacher(String teacher, int intent, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getTeacher(today(), teacher, force).enqueue(new UpdateTimetableCallback(intent));
    }

    void showForStudent(String student, int intent, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getStudent(today(), student, force).enqueue(new UpdateTimetableCallback(intent));
    }

    void showForRoom(String room, int intent, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getRoom(today(), room, force).enqueue(new UpdateTimetableCallback(intent));
    }

    void showForGroup(String group, int intent, boolean force) {
        ((MainActivity) getActivity()).timetableAPI.getGroup(today(), group, force).enqueue(new UpdateTimetableCallback(intent));
    }

    private void setLoading(boolean loading) {
        viewPager.setVisibility(loading ? View.GONE : View.VISIBLE);
        empty.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setEmpty(String msg) {
        ((ImageView) empty.findViewById(R.id.timetable_empty_img)).setImageResource(R.drawable.ic_warning);
        ((TextView) empty.findViewById(R.id.timetable_empty_text)).setText(msg);
        viewPager.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showForRoom(String room, boolean force) {
        this.showForRoom(room, POPUP_INTENT, force);
    }

    @Override
    public void showForTeacher(String teacher, boolean force) {
        this.showForTeacher(teacher, POPUP_INTENT, force);
    }

    @Override
    public void showForGroup(String group, boolean force) {
        this.showForGroup(group, POPUP_INTENT, force);
    }

    private class TimetableViewPagerAdapter extends FragmentStatePagerAdapter {
        private TimetableInnerFragment currentFragment;
        private Timetable studentTimetable;
        private Timetable roomTimetable;
        private int size;

        TimetableViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            if (size == 1) {
                return TimetableInnerFragment.newInstance(studentTimetable != null ? studentTimetable : roomTimetable);
            } else {
                if (position == 0) {
                    return TimetableInnerFragment.newInstance(studentTimetable);
                } else {
                    return TimetableInnerFragment.newInstance(roomTimetable);
                }
            }
        }

        @Override
        public void notifyDataSetChanged() {
            updateSize();
            super.notifyDataSetChanged();
            setLoading(size < 1);
        }

        @Override
        public int getCount() {
            return size;
        }

        void setStudentTimetable(Timetable studentTimetable) {
            this.studentTimetable = studentTimetable;
            notifyDataSetChanged();
        }

        void setRoomTimetable(Timetable roomTimetable) {
            this.roomTimetable = roomTimetable;
            notifyDataSetChanged();
        }

        private void updateSize() {
            size = 0;
            if (studentTimetable != null) size++;
            if (roomTimetable != null) size++;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (size == 1) {
                return (studentTimetable != null ? studentTimetable : roomTimetable).getShortTitle();
            } else {
                if (position == 0) {
                    return studentTimetable.getShortTitle();
                } else {
                    return roomTimetable.getShortTitle();
                }
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if (object != studentTimetable && object != roomTimetable) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (currentFragment != object) {
                currentFragment = ((TimetableInnerFragment) object);
                if (currentFragment != null) {
                    swipeRefresh.setOnChildScrollUpCallback((currentFragment));
                }
            }
            super.setPrimaryItem(container, position, object);
        }
    }

    private class UpdateTimetableCallback implements Callback<Timetable> {
        private final int intent;
        private TimetableInnerFragment fragment;

        UpdateTimetableCallback(int intent) {
            this.intent = intent;
            if (intent == POPUP_INTENT) {
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
        }

        @Override
        public void onResponse(@NonNull Call<Timetable> call, @NonNull Response<Timetable> response) {
            Timetable timetable = response.body();

            if (intent == STUDENT_INTENT) adapter.setStudentTimetable(timetable);
            if (intent == ROOM_INTENT) adapter.setRoomTimetable(timetable);
            if (intent == POPUP_INTENT) {
                fragment.setTimetable(timetable);
            } else {
                try {
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

                    if (actionBar != null && timetable != null) {
                        actionBar.setTitle(timetable.getTitleDate(getResources().getConfiguration().locale));
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "Mweh", e);
                }
            }
            timetableRefreshing = false;
            updateRefreshing();
        }

        @Override
        public void onFailure(@NonNull Call<Timetable> call, Throwable t) {
            Log.e(TAG, "Error when calling: " + call.toString(), t);
            timetableRefreshing = false;
            updateRefreshing();
        }
    }
}
