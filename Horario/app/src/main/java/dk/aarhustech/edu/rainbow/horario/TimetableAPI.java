package dk.aarhustech.edu.rainbow.horario;


import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface TimetableAPI {
    @GET("Timetables/get")
    Call<Timetable> getTeacher(@Query("date") LocalDate date, @Query("teacher") String teacher, @Query("force") boolean force);

    @GET("Timetables/get")
    Call<Timetable> getStudent(@Query("date") LocalDate date, @Query("student") String student, @Query("force") boolean force);

    @GET("Timetables/get")
    Call<Timetable> getRoom(@Query("date") LocalDate date, @Query("room") String room, @Query("force") boolean force);

    @GET("Timetables/get")
    Call<Timetable> getGroup(@Query("date") LocalDate date, @Query("group") String group, @Query("force") boolean force);

    @FormUrlEncoded
    @POST("Timetables/bookLesson")
    Call<Status> bookLesson(@Field("date") LocalDate date, @Field("startTime") LocalTime time, @Field("room") String room, @Field("booker") String booker);

    class Status {
        boolean success;
    }
}
