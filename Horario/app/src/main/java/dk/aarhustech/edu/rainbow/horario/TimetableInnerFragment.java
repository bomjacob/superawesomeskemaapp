package dk.aarhustech.edu.rainbow.horario;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static dk.aarhustech.edu.rainbow.horario.TimetableFragment.POPUP_INTENT;
import static dk.aarhustech.edu.rainbow.horario.TimetableFragment.today;

public class TimetableInnerFragment extends DialogFragment implements SwipeRefreshLayout.OnChildScrollUpCallback {
    private static final String TAG = TimetableInnerFragment.class.getSimpleName();
    private static final int SHOW_ON_MAP = 0;
    private static final int TIMETABLE_ROOM = 1;
    private static final int TIMETABLE_GROUP = 2;
    private static final int TIMETABLE_TEACHER = 3;
    private RecyclerView rv;
    private View empty;

    public TimetableInnerFragment() {
        // Required empty public constructor
    }

    static TimetableInnerFragment newInstance(Timetable timetable) {
        TimetableInnerFragment fragment = new TimetableInnerFragment();

        Bundle args = new Bundle();
        args.putSerializable("timetable", timetable);
        fragment.setArguments(args);

        return fragment;
    }

    static TimetableInnerFragment newInstance() {
        TimetableInnerFragment fragment = new TimetableInnerFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getShowsDialog()) setStyle(DialogFragment.STYLE_NORMAL, R.style.TimetableDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getShowsDialog()) {
            TextView tv = (TextView) getDialog().findViewById(android.R.id.title);
            tv.setSingleLine(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        Timetable timetable = (Timetable) args.getSerializable("timetable");

        if (!getShowsDialog()) {
            container.clearDisappearingChildren();
        }

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_timetable_inner, container, false);
        setHasOptionsMenu(true);

        rv = (RecyclerView) root.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        empty = root.findViewById(R.id.timetable_empty);

        if (timetable != null) {
            setTimetable(timetable);
        }

        return root;
    }

    public void setTimetable(Timetable timetable) {
        if (getShowsDialog()) {
            if (timetable != null) getDialog().setTitle(timetable.getTitle(getContext()));
            getDialog().setCanceledOnTouchOutside(true);
        }

        RecyclerView.Adapter adapter = new RVAdapter(timetable);
        rv.setAdapter(adapter);

        if (timetable == null || timetable.lessons.size() < 1) {
            ((ImageView) empty.findViewById(R.id.timetable_empty_img)).setImageResource(R.drawable.ic_warning);
            ((TextView) empty.findViewById(R.id.timetable_empty_text)).setText(R.string.no_timetable_lessons_found);
        } else {
            empty.setVisibility(View.GONE);
        }
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
        //Log.d(TAG, String.valueOf(rv.canScrollVertically(-1)));
        return rv.canScrollVertically(-1);
    }

    class RVAdapter extends RecyclerView.Adapter<RVAdapter.LessonViewHolder> {

        private final Timetable timetable;

        RVAdapter(Timetable timetable) {
            this.timetable = timetable;
        }

        @Override
        public LessonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_lesson, parent, false);
            return new LessonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LessonViewHolder holder, int position) {
            final Timetable.Lesson lesson = timetable.lessons.get(position);

            holder.lessonTime.setText(lesson.startTime.toString("HH:mm") + " - " + lesson.endTime.toString("HH:mm"));
            if (lesson.isFree) {
                holder.bookButton.setVisibility(View.VISIBLE);
                holder.bookButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String login = preferences.getString("pref_login", null);
                        if (login != null && !login.equals("")) {
                            ((MainActivity) getActivity()).timetableAPI.bookLesson(today(), lesson.startTime, timetable.room, login).enqueue(new Callback<TimetableAPI.Status>() {
                                @Override
                                public void onResponse(@NonNull Call<TimetableAPI.Status> call, @NonNull Response<TimetableAPI.Status> response) {
                                    ((MainActivity)getActivity()).timetableAPI.getRoom(today(), timetable.room, false).enqueue(new Callback<Timetable>() {
                                        @Override
                                        public void onResponse(Call<Timetable> call, Response<Timetable> response) {
                                            setTimetable(response.body());
                                        }

                                        @Override
                                        public void onFailure(Call<Timetable> call, Throwable t) {
                                            Log.e(TAG, "Error when calling: " + call.toString(), t);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NonNull Call<TimetableAPI.Status> call, @NonNull Throwable t) {
                                    Log.e(TAG, "Error when calling: " + call.toString(), t);
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), R.string.please_add_unic_username, Toast.LENGTH_LONG).show();
                        }

                    }
                });
                holder.lessonSubject.setText("Fri lektion");
                holder.lessonTeachers.setVisibility(View.GONE);
                holder.lessonExtra.setVisibility(View.GONE);
                holder.cv.setCardBackgroundColor(Color.LTGRAY);
            } else {
                holder.bookButton.setVisibility(View.GONE);
                holder.lessonSubject.setText(lesson.subject);
                String teacherString = lesson.getTeacherString();
                holder.lessonTeachers.setText(teacherString);

                if (lesson.groups != null && lesson.groups.size() > 0) {
                    holder.lessonExtra.setText(TextUtils.join(", ", lesson.groups));
                }
                if (lesson.rooms != null && lesson.rooms.size() > 0) {
                    holder.lessonExtra.setText(TextUtils.join(", ", lesson.rooms));
                }

                holder.cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PopupMenu popup = new PopupMenu(getActivity(), v);

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == -1) return false;

                                switch (item.getGroupId()) {
                                    case SHOW_ON_MAP:
                                        break;
                                    case TIMETABLE_ROOM:
                                        ((ShowFor) getParentFragment()).showForRoom(lesson.rooms.get(item.getItemId()), false);
                                        break;
                                    case TIMETABLE_TEACHER:
                                        ((ShowFor) getParentFragment()).showForTeacher(lesson.teachers.get(item.getItemId()).extra, false);
                                        break;
                                    case TIMETABLE_GROUP:
                                        ((ShowFor) getParentFragment()).showForGroup(lesson.groups.get(item.getItemId()), false);
                                        break;
                                }
                                return false;
                            }
                        });

                        Menu menu = popup.getMenu();

                        if (lesson.rooms.size() > 1) {
                            SubMenu sm = menu.addSubMenu(TIMETABLE_ROOM, -1, 0, R.string.show_room_on_map);
                            for (int i = 0; i < lesson.rooms.size(); i++) {
                                sm.add(SHOW_ON_MAP, i, 0, lesson.rooms.get(i));
                            }
                        } else if (lesson.rooms.size() > 0) {
                            menu.add(SHOW_ON_MAP, 0, 0, getString(R.string.show_room_x_on_map, lesson.rooms.get(0)));
                        }

                        if (lesson.rooms.size() > 1) {
                            SubMenu sm = menu.addSubMenu(TIMETABLE_ROOM, -1, 0, R.string.show_timetable_for_room);
                            for (int i = 0; i < lesson.rooms.size(); i++) {
                                sm.add(TIMETABLE_ROOM, i, 0, lesson.rooms.get(i));
                            }
                        } else if (lesson.rooms.size() > 0) {
                            menu.add(TIMETABLE_ROOM, 0, 0, getString(R.string.show_for_room, lesson.rooms.get(0)));
                        }

                        if (lesson.teachers.size() > 1) {
                            SubMenu sm = menu.addSubMenu(TIMETABLE_TEACHER, -1, 0, R.string.show_timetable_for_teacher);
                            for (int i = 0; i < lesson.teachers.size(); i++) {
                                sm.add(TIMETABLE_TEACHER, i, 0, lesson.teachers.get(i).toString());
                            }
                        } else if (lesson.teachers.size() > 0) {
                            menu.add(TIMETABLE_TEACHER, 0, 0, getString(R.string.show_for_teacher, lesson.teachers.get(0).toString()));
                        }

                        if (lesson.groups.size() > 1) {
                            SubMenu sm = menu.addSubMenu(TIMETABLE_GROUP, -1, 0, R.string.show_timetable_for_group);
                            for (int i = 0; i < lesson.groups.size(); i++) {
                                sm.add(TIMETABLE_GROUP, i, 0, lesson.groups.get(i));
                            }
                        } else if (lesson.groups.size() > 0) {
                            menu.add(TIMETABLE_GROUP, 0, 0, getString(R.string.show_for_group, lesson.groups.get(0)));
                        }

                        popup.show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return timetable.lessons.size();
        }

        class LessonViewHolder extends RecyclerView.ViewHolder {
            final TextView lessonTime;
            final TextView lessonSubject;
            final TextView lessonTeachers;
            final TextView lessonExtra;
            final Button bookButton;
            final CardView cv;

            LessonViewHolder(View itemView) {
                super(itemView);

                cv = (CardView) itemView.findViewById(R.id.cv);
                lessonTime = (TextView) cv.findViewById(R.id.time);
                lessonSubject = (TextView) cv.findViewById(R.id.subject);
                lessonTeachers = (TextView) cv.findViewById(R.id.teachers);
                lessonExtra = (TextView) cv.findViewById(R.id.extra);
                bookButton = (Button) cv.findViewById(R.id.bookButton);
            }
        }
    }
}
