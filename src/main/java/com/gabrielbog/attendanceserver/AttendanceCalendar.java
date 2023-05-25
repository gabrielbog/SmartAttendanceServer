package com.gabrielbog.attendanceserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.*;

@Getter
@ToString
public class AttendanceCalendar {

    private static AttendanceCalendar inst = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Date yearStart;
    private Date yearStop;
    private Date semesterIstop;
    private Date semesterIIstart;

    private AttendanceCalendar() {

        try {
            BufferedReader br = new BufferedReader(new FileReader("calendar.json"));
            JsonObject json = new Gson().fromJson(br, JsonObject.class);

            java.util.Date date = dateFormat.parse(json.get("yearStart").getAsString());
            java.sql.Date yearStart = new java.sql.Date(date.getTime());

            date = dateFormat.parse(json.get("semesterIstop").getAsString());
            java.sql.Date semesterIstop = new java.sql.Date(date.getTime());

            date = dateFormat.parse(json.get("semesterIIstart").getAsString());
            java.sql.Date semesterIIstart = new java.sql.Date(date.getTime());

            date = dateFormat.parse(json.get("yearStop").getAsString());
            java.sql.Date yearStop = new java.sql.Date(date.getTime());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static AttendanceCalendar getInstance() {
        if(inst == null) {
            inst = new AttendanceCalendar();
        }
        return inst;
    }
}
