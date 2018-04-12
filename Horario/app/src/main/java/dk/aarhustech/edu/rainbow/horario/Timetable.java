package dk.aarhustech.edu.rainbow.horario;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Timetable implements Serializable {
    LocalDate date;
    String room;
    String group;
    Person teacher;
    Person student;
    List<Lesson> lessons;

    Timetable(LocalDate date, List<Lesson> lessons, @Nullable String room, @Nullable String group) {
        this.date = date;
        this.room = room;
        this.group = group;
        this.lessons = lessons;
    }

    String getShortTitle() {
        if (room != null) {
            return room;
        } else if (group != null) {
            return group;
        } else if (teacher != null) {
            return teacher.name;
        } else if (student != null) {
            return student.name;
        } else {
            return "???";
        }
    }

    String getTitleDate(Locale locale) {
        return date.toString("E d MMMM", locale);
    }

    String getTitle(Context context) {
        return context.getString(R.string.timetable_title, getTitleDate(context.getResources().getConfiguration().locale), getShortTitle());
    }

    static class Lesson implements Serializable {
        String subject;
        LocalTime startTime;
        LocalTime endTime;
        List<Person> teachers;
        List<String> groups;
        List<String> rooms;
        String comment;
        String booker;
        boolean isFree;

        Lesson(String subject, LocalTime startTime, LocalTime endTime, Person[] teachers, String[] groups, String[] rooms, String comment, String booker, boolean isFree) {
            this.subject = subject;
            this.startTime = startTime;
            this.endTime = endTime;
            this.teachers = Arrays.asList(teachers);
            this.groups = Arrays.asList(groups);
            this.rooms = Arrays.asList(rooms);
            this.comment = comment;
            this.booker = booker;
            this.isFree = isFree;
        }

        Lesson(String subject, LocalTime startTime, LocalTime endTime, List<Person> teachers, List<String> groups, List<String> rooms, String comment, String booker, boolean isFree) {
            this.subject = subject;
            this.startTime = startTime;
            this.endTime = endTime;
            this.teachers = teachers;
            this.groups = groups;
            this.rooms = rooms;
            this.comment = comment;
            this.booker = booker;
            this.isFree = isFree;
        }

        String getTeacherString() {
            if (booker != null && !booker.isEmpty()) {
                return booker;
            }
            if (teachers.size() > 2) {
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < teachers.size(); i++) {
                    out.append(teachers.get(i).extra);
                    if (i != teachers.size() - 1) out.append(", ");
                }
                return out.toString();
            } else {
                return TextUtils.join(", ", teachers);
            }
        }
    }

    static class Person implements Serializable {
        String name;
        String extra;

        Person(String name, String extra) {
            this.name = name;
            this.extra = extra;
        }

        @Override
        public String toString() {
            return name + (extra != null ? (" (" + extra + ")") : "");
        }
    }

    static class PersonSerializer implements JsonDeserializer<Timetable.Person>, JsonSerializer<Timetable.Person> {

        @Override
        public Timetable.Person deserialize(final JsonElement je, final Type type,
                                            final JsonDeserializationContext jdc) throws JsonParseException {
            Matcher matcher = Pattern.compile("(.*) \\((.*)\\)").matcher(je.getAsString());
            if (matcher.find()) {
                return new Timetable.Person(matcher.group(1), matcher.group(2));
            }
            return new Timetable.Person(je.getAsString(), null);
        }

        @Override
        public JsonElement serialize(final Timetable.Person src, final Type typeOfSrc,
                                     final JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

}
