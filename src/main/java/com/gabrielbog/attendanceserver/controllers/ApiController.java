package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.constants.Constants;
import com.gabrielbog.attendanceserver.AttendanceCalendar;
import com.gabrielbog.attendanceserver.models.*;
import com.gabrielbog.attendanceserver.models.responses.*;
import com.gabrielbog.attendanceserver.repositories.*;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    //Database Access
    @Autowired
    UserRepository userRepo;

    @Autowired
    StudentRepository studentRepo;

    @Autowired
    SpecializationRepository specialzationRepo;

    @Autowired
    SubjectRepository subjectRepo;

    @Autowired
    ScheduleRepository scheduleRepo;

    @Autowired
    AttendanceRepository attendanceRepo;

    @Autowired
    ScancodeRepository scancodeRepo;

    /*
        users
    */

    @GetMapping("/checkUserExistence/{cnp}&{password}")
    public LogInResponse checkUserExistence(@PathVariable String cnp, @PathVariable String password) {
        //might be a good idea to find only by cnp, then compare passwords by code
        try {
            Optional<User> user = userRepo.findByCnp(cnp);
            Date date = Date.valueOf(LocalDate.now());
            Time time = Time.valueOf(LocalTime.now());

            if (user.isPresent()) {
                if(user.get().getPassword().equals(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString())) {
                    if(user.get().getIsAdmin() == 0) {
                        //check if student exists
                        try {
                            Optional<Student> student = studentRepo.findByUserId(user.get().getId());
                            if(student.isPresent()) {
                                //System.out.println("[" + date.toString() + " " + time.toString() + "] Student " + user.get().getCnp() + " logged in"); //debug
                                return new LogInResponse(1, user.get().getId(), user.get().getIsAdmin(), user.get().getFirstName(), user.get().getLastName());
                            }
                            else {
                                //invalid user - might be a good idea to store this in a log on the server
                                //System.out.println("[" + date.toString() + " " + time.toString() + "] !!! STUDENT " + user.get().getCnp() + " HAS NO STUDENT TABLE ENTRY !!!"); //debug
                                return new LogInResponse(0, 0, 0, "", "");
                            }
                        }
                        catch(Exception ex) {
                            return new LogInResponse(-1, 0, 0, "Error during request", "");
                        }
                    }
                    else {
                        //return professor response
                        //System.out.println("[" + date.toString() + " " + time.toString() + "] Professor " + user.get().getCnp() + " logged in"); //debug
                        return new LogInResponse(1, user.get().getId(), user.get().getIsAdmin(), user.get().getFirstName(), user.get().getLastName());
                    }
                }
                else {
                    return new LogInResponse(0, 0, 0, "", "");
                }
            }
            else {
                return new LogInResponse(0, 0, 0, "", "");
            }
        }
        catch(Exception ex) {
            return new LogInResponse(-1, 0, 0, "Error during request", "");
        }
    }

    /*
        attendance
    */

    @GetMapping("/generateQrCode/{id}")
    public QrCodeResponse generateQrCode(@PathVariable int id) {

        LocalDate currentDate = LocalDate.now();
        Time currentTime = Time.valueOf(LocalTime.now());

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {

                if(user.get().getIsAdmin() == 1) {

                    try {

                        List<Schedule> scheduleList = new ArrayList<>();
                        scheduleList = scheduleRepo.findByProfessorId(id);

                        for(Schedule schedule : scheduleList) {

                            int timeStartDifference = currentTime.compareTo(schedule.getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(schedule.getTimeStop());

                            //check if the schedule found is right for this time
                            if(schedule.getWeekday() == currentDate.getDayOfWeek().getValue() && timeStartDifference >= 0 && timeStopDifference <= 0) {

                                try {

                                    Optional<Scancode> existingScancode = scancodeRepo.findByCreationDateAndScheduleId(Date.valueOf(currentDate), schedule.getId());
                                    Scancode scancode = null;
                                    if(existingScancode.isPresent()) { //generated code already exists

                                        scancode = existingScancode.get();
                                        long timeDifference = currentTime.getTime() - scancode.getTimeGenerated().getTime();
                                        //System.out.println(timeDifference);
                                        if(timeDifference >= Constants.CODE_DURATION) { //is the code older than it's supposed to?

                                            String originalString = id + user.get().getFirstName() + schedule.getId() + currentDate.toString() + " " + currentTime.toString();
                                            String qrString = Hashing.sha256()
                                                    .hashString(originalString, StandardCharsets.UTF_8)
                                                    .toString();
                                            scancode.setCode(qrString);
                                            scancode.setTimeGenerated(currentTime);

                                            try {
                                                scancodeRepo.save(scancode);
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                            }

                                            try {
                                                Optional<Subject> subject = subjectRepo.findById(schedule.getSubjectId());
                                                if(subject.isPresent()) {
                                                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
                                                    return new QrCodeResponse(2, Constants.CODE_DURATION, qrString, subject.get().getName(), schedule.getStudentGrup());
                                                }
                                                else { //impossible condition
                                                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
                                                    return new QrCodeResponse(2, 0, qrString, "", 0);
                                                }
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                            }
                                        }
                                        else { //return existing code but with different duration for the application refresh time

                                            try {
                                                Optional<Subject> subject = subjectRepo.findById(schedule.getSubjectId());
                                                if(subject.isPresent()) {
                                                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " requested code: " + existingScancode.get().getCode()); //debug
                                                    return new QrCodeResponse(2, Constants.CODE_DURATION - timeDifference, existingScancode.get().getCode(), subject.get().getName(), schedule.getStudentGrup());
                                                }
                                                else { //impossible condition
                                                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " requested code: " + existingScancode.get().getCode()); //debug
                                                    return new QrCodeResponse(2, 0, existingScancode.get().getCode(), "", 0);
                                                }
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                            }
                                        }
                                    }
                                    else { //create a new code
                                        String originalString = id + user.get().getFirstName() + schedule.getId() + currentDate.toString() + " " + currentTime.toString();
                                        String qrString = Hashing.sha256()
                                                .hashString(originalString, StandardCharsets.UTF_8)
                                                .toString();

                                        scancode = new Scancode();
                                        scancode.setSubjectId(schedule.getSubjectId());
                                        scancode.setScheduleId(schedule.getId());
                                        scancode.setCode(qrString);
                                        scancode.setCreationDate(Date.valueOf(currentDate));
                                        scancode.setTimeGenerated(currentTime);
                                        scancode.setTimeStart(schedule.getTimeStart());
                                        scancode.setTimeStop(schedule.getTimeStop());

                                        try {
                                            scancodeRepo.save(scancode);
                                        }
                                        catch (Exception ex) {
                                            return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                        }

                                        try {
                                            Optional<Subject> subject = subjectRepo.findById(schedule.getSubjectId());
                                            if(subject.isPresent()) {
                                                //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " generated code: " + qrString); //debug
                                                return new QrCodeResponse(2, Constants.CODE_DURATION, qrString, subject.get().getName(), schedule.getStudentGrup());
                                            }
                                            else { //impossible condition
                                                //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " generated code: " + qrString); //debug
                                                return new QrCodeResponse(2, 0, qrString, "", 0);
                                            }
                                        }
                                        catch (Exception ex) {
                                            return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                        }
                                    }
                                }
                                catch(Exception ex) {
                                    return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                }
                            }
                        }

                        return new QrCodeResponse(3, 0, "You don't have any schedule in progress right now.", "", 0); //error during request
                    }
                    catch (Exception ex) {
                        return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                    }
                }
                else {
                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
                    return new QrCodeResponse(1, 0, "", "", 0); //request from non-professor
                }
            }
            else {
                return new QrCodeResponse(0, 0, "", "", 0); //invalid id
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
        }
    }

    @GetMapping("/refreshQrCode/{id}&{code}")
    public QrCodeResponse refreshQrCode(@PathVariable int id, @PathVariable String code) {

        Date currentDate = Date.valueOf(LocalDate.now());
        Time currentTime = Time.valueOf(LocalTime.now());

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                if (user.get().getIsAdmin() == 1) {
                    try {
                        Optional<Scancode> scancode = scancodeRepo.findByCode(code);
                        if (scancode.isPresent()) {
                            if(!scancode.get().getCreationDate().equals(currentDate)) {
                                return new QrCodeResponse(3, 0, "The schedule has finished.", "", 0);
                            }

                            try {
                                Optional<Schedule> schedule = scheduleRepo.findById(scancode.get().getScheduleId());
                                if(schedule.isPresent()) {
                                    if(schedule.get().getProfessorId() == id) {
                                        int timeStartDifference = currentTime.compareTo(scancode.get().getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                                        int timeStopDifference = currentTime.compareTo(scancode.get().getTimeStop());

                                        if(timeStartDifference >= 0 && timeStopDifference <= 0) {
                                            String originalString = id + user.get().getFirstName() + scancode.get().getScheduleId() + currentDate.toString() + " " + currentTime.toString();
                                            String qrString = Hashing.sha256()
                                                    .hashString(originalString, StandardCharsets.UTF_8)
                                                    .toString();
                                            scancode.get().setCode(qrString);
                                            scancode.get().setTimeGenerated(currentTime);

                                            try {
                                                scancodeRepo.save(scancode.get());
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                            }

                                            //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
                                            return new QrCodeResponse(2, Constants.CODE_DURATION, qrString, "", 0); //returning subjectString and grup is pointless, mobile app is supposed to have it already
                                        }
                                        else {
                                            return new QrCodeResponse(3, 0, "The schedule has finished.", "", 0);
                                        }
                                    }
                                    else {
                                        return new QrCodeResponse(0, 0, "", "", 0);
                                    }
                                }
                                else { //impossible request
                                    return new QrCodeResponse(0, 0, "", "", 0);
                                }
                            }
                            catch (Exception ex) {
                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                            }
                        }
                        else { //impossible request
                            return new QrCodeResponse(0, 0, "", "", 0);
                        }
                    }
                    catch (Exception ex) {
                        return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                    }
                }
                else {
                    //System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
                    return new QrCodeResponse(1, 0, "", "", 0); //request from non-professor
                }
            }
            else { //impossible request
                return new QrCodeResponse(0, 0, "", "", 0); //invalid id
            }
        }
        catch(Exception ex) {
            return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
        }
    }

    @GetMapping("/scanQrCode/{id}&{code}")
    public QrCodeResponse scanQrCode(@PathVariable int id, @PathVariable String code) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date currentDate = Date.valueOf(LocalDate.now());
        Time currentTime = Time.valueOf(LocalTime.now());

        try {
            Optional<Student> student = studentRepo.findByUserId(id);
            if (student.isPresent()) {

                try {
                    Optional<Scancode> scancode = scancodeRepo.findByCode(code);
                    if(scancode.isPresent()) {

                        try {

                            List<Attendance> attendanceList = new ArrayList<>();
                            attendanceList = attendanceRepo.findByScanDate(currentDate);

                            for(Attendance attendance : attendanceList) {
                                if(attendance.getStudentId() == id && attendance.getScheduleId() == scancode.get().getScheduleId()) { //stop if user already scanned this code once
                                    return new QrCodeResponse(1, 0, "You're already attending this schedule.", "", 0);
                                }
                            }

                            int timeStartDifference = currentTime.compareTo(scancode.get().getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(scancode.get().getTimeStop());

                            //check if the date and time at scan is valid
                            if(scancode.get().getCreationDate().equals(currentDate) && timeStartDifference >= 0 && timeStopDifference <= 0) {

                                //mark code as invalid if it didn't refresh; this is in case the professor hasn't refreshed the code
                                long timeDifference = currentTime.getTime() - scancode.get().getTimeGenerated().getTime();
                                if(timeDifference < Constants.CODE_DURATION) {
                                    try {
                                        //check if the student year grade specialization match
                                        Optional<Schedule> schedule = scheduleRepo.findById(scancode.get().getScheduleId());
                                        if(schedule.isPresent()) {

                                            try {
                                                Optional<Subject> subject = subjectRepo.findById(schedule.get().getSubjectId());
                                                if(subject.isPresent()) {
                                                    if(student.get().getSpec() == subject.get().getSpec() && student.get().getGrade() == subject.get().getGrade()) {
                                                        if(schedule.get().getStudentGrup() == student.get().getGrup() || schedule.get().getStudentGrup() == 0) {

                                                            Attendance attendance = new Attendance();
                                                            attendance.setStudentId(id);
                                                            attendance.setScheduleId(scancode.get().getScheduleId());
                                                            attendance.setSubjectId(subject.get().getId());
                                                            attendance.setScanDate(currentDate);
                                                            attendance.setScanTime(currentTime);
                                                            try {
                                                                attendanceRepo.save(attendance);
                                                            }
                                                            catch (Exception ex) {
                                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
                                                            }

                                                            //System.out.println("[" + timestamp.toString() + "] Student " + student.get().getUserId() + " scanned code successfully"); //debug
                                                            return new QrCodeResponse(2, 0, "You're now attending!", "", 0);
                                                        }
                                                        else {
                                                            return new QrCodeResponse(1, 0, "Invalid QR Code.", "", 0);
                                                        }
                                                    }
                                                    else {
                                                        return new QrCodeResponse(1, 0, "Invalid QR Code.", "", 0);
                                                    }
                                                }
                                                else { //impossible error
                                                    return new QrCodeResponse(0, 0, "", "", 0);
                                                }
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
                                            }
                                        }
                                        else { //impossible error
                                            return new QrCodeResponse(0, 0, "", "", 0);
                                        }
                                    }
                                    catch (Exception ex) {
                                        return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
                                    }
                                }
                                else {
                                    return new QrCodeResponse(1, 0, "The code has expired.", "", 0);
                                }
                            }
                            else {
                                return new QrCodeResponse(1, 0, "The code has expired.", "", 0);
                            }
                        }
                        catch(Exception ex) {
                            return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
                        }
                    }
                    else {
                        return new QrCodeResponse(1, 0, "Invalid QR Code.", "", 0);
                    }
                }
                catch (Exception ex) {
                    return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
                }
            }
            else { //impossible error
                return new QrCodeResponse(0, 0, "Invalid student.", "", 0);
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, 0, "Error during request.", "", 0);
        }
    }

    /*
        schedules
    */

    @GetMapping("/getSubjectList/{id}")
    public SubjectListResponse getSubjectList(@PathVariable int id) {

        SubjectListResponse response = new SubjectListResponse();
        List<Subject> subjectList = new ArrayList<>();

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {

                if(user.get().getIsAdmin() == 1) {

                    try {
                        //try catch
                        List<Schedule> scheduleList = new ArrayList<>();
                        scheduleList = scheduleRepo.findByProfessorId(user.get().getId());
                        int duplicateFound = 0;

                        for(Schedule scheduleElement : scheduleList) {

                            try {
                                Optional<Subject> subject = subjectRepo.findById(scheduleElement.getSubjectId());
                                if(subject.isPresent()) {
                                    duplicateFound = 0;
                                    for (Subject subjectElement : subjectList) {
                                        if(subjectElement.getId() == subject.get().getId()) {
                                            duplicateFound = 1;
                                            break;
                                        }
                                    }
                                    if(duplicateFound == 0) {
                                        subjectList.add(subject.get());
                                    }
                                }
                            }
                            catch (Exception ex) {
                                response.setCode(-1);
                                return response;
                            }
                        }

                        response.setCode(1);
                        response.setSubjectList(subjectList);
                        return response;
                    }
                    catch (Exception ex) {
                        response.setCode(-1);
                        return response;
                    }
                }
                else {

                    try {
                        Optional<Student> student = studentRepo.findByUserId(user.get().getId());
                        if(student.isPresent()) {

                            try {
                                subjectList = subjectRepo.findBySpecAndGrade(student.get().getSpec(), student.get().getGrade()); //build list with student year and grade subjects
                                response.setCode(1);
                                response.setSubjectList(subjectList);
                                return response;
                            }
                            catch (Exception ex) {
                                response.setCode(-1);
                                return response;
                            }
                        }
                        else {
                            response.setCode(0);
                            return response;
                        }
                    }
                    catch (Exception ex) {
                        response.setCode(-1);
                        return response;
                    }
                }
            }
            else {
                response.setCode(0);
                return response;
            }
        }
        catch (Exception ex) {
            response.setCode(-1);
            return response;
        }
    }

    @GetMapping("/getScheduleCalendar/{professorId}&{subjectId}")
    public ScheduleCalendarResponse getScheduleCalendar(@PathVariable int professorId, @PathVariable int subjectId) {

        ScheduleCalendarResponse response = new ScheduleCalendarResponse();
        List<ScheduleCalendar> scheduleCalendar = new ArrayList<>();

        try {
            Optional<Subject> subject = subjectRepo.findById(subjectId);
            if(subject.isPresent()) {
                try {
                    AttendanceCalendar attendanceCalendar = AttendanceCalendar.getInstance(); //reads the calendar for the year and semester periods

                    //change start and stop depending on semester and current date
                    LocalDate startLocalDate = attendanceCalendar.getYearStart().toLocalDate();
                    LocalDate endLocalDate = attendanceCalendar.getYearStop().toLocalDate();
                    if(subject.get().getSemester() == 1) { //semester intervals
                        endLocalDate = attendanceCalendar.getSemesterIstop().toLocalDate();
                    }
                    else {
                        startLocalDate = attendanceCalendar.getSemesterIIstart().toLocalDate();
                    }
                    LocalDate currentDate = LocalDate.now();
                    if(endLocalDate.compareTo(currentDate) > 0) { //student checks attendance before semester ends
                        endLocalDate = currentDate;
                    }
                    LocalDate nextDate = startLocalDate;

                    List<Schedule> scheduleList = new ArrayList<>();
                    scheduleList = scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId);

                    while (nextDate.isBefore(endLocalDate)) { //iterates through all days
                        for(Schedule schedule : scheduleList) {
                            if (nextDate.getDayOfWeek().getValue() == schedule.getWeekday()) { //checks if the days match
                                ScheduleCalendar scheduleCalendarElement = new ScheduleCalendar(schedule.getId(), Date.valueOf(nextDate), schedule.getTimeStart(), schedule.getTimeStop(), schedule.getStudentGrup());
                                scheduleCalendar.add(scheduleCalendarElement); //adds the date to the list
                            }
                        }
                        nextDate = nextDate.plus(1, ChronoUnit.DAYS);
                    }

                    response.setCode(1);
                    response.setScheduleCalendarList(scheduleCalendar);
                    return response;
                }
                catch (Exception ex) {
                    response.setCode(-1);
                    return response;
                }
            }
            else {
                response.setCode(0);
                return response;
            }
        }
        catch (Exception ex) {
            response.setCode(-1);
            return response;
        }
    }

    @GetMapping("/getAttendingStudentsList/{scanDate}&{scheduleId}")
    public StudentAttendanceResponse getAttendingStudentsList(@PathVariable Date scanDate, @PathVariable int scheduleId) {

        StudentAttendanceResponse studentAttendanceResponse = new StudentAttendanceResponse();
        List<StudentAttendance> studentAttendanceList = new ArrayList<>();

        try {
            Optional<Schedule> schedule = scheduleRepo.findById(scheduleId);
            if(schedule.isPresent()) {
                try {
                    Optional<Subject> subject = subjectRepo.findById(schedule.get().getSubjectId());
                    if(subject.isPresent()) {
                        try {
                            List<Student> studentList = new ArrayList<>();
                            List<Student> tempStudentList = new ArrayList<>();
                            studentList = studentRepo.findByGradeAndSpec(subject.get().getGrade(), subject.get().getSpec());

                            List<User> userList = new ArrayList<>(); //sort the student list by names
                            userList = userRepo.findByIsAdmin(0);
                            userList.sort(User::compareByName);
                            for(User user : userList) {
                                for(Student student : studentList) {
                                    if(user.getId() == student.getUserId()) {
                                        tempStudentList.add(student);
                                        break;
                                    }
                                }
                            }
                            studentList = tempStudentList;

                            try {
                                List<Attendance> attendanceList = new ArrayList<>();
                                attendanceList = attendanceRepo.findByScanDateAndScheduleId(scanDate, scheduleId);
                                for(Student student : studentList) {
                                    try {
                                        Optional<User> user = userRepo.findById(student.getUserId());
                                        if(user.isPresent()) {

                                            int studentFound = 0;
                                            if(schedule.get().getStudentGrup() == 0 || schedule.get().getStudentGrup() == student.getGrup()) { //only take in consideration specific groups or all groups {
                                                for(Attendance attendance : attendanceList) {
                                                    if(attendance.getScanDate().equals(scanDate) && attendance.getStudentId() == student.getUserId()) {
                                                        studentAttendanceList.add(new StudentAttendance(0, scanDate, null, null, user.get().getFirstName(), user.get().getLastName(), "present"));
                                                        studentFound = 1;
                                                        break;
                                                    }
                                                }
                                                if(studentFound == 0) {
                                                    studentAttendanceList.add(new StudentAttendance(0, scanDate, null, null, user.get().getFirstName(), user.get().getLastName(), "absent"));
                                                }
                                            }
                                        }
                                    }
                                    catch (Exception ex) {
                                        studentAttendanceResponse.setCode(-1);
                                        return studentAttendanceResponse;
                                    }
                                }
                                studentAttendanceResponse.setCode(1);
                                studentAttendanceResponse.setStudentAttendanceList(studentAttendanceList);
                                return studentAttendanceResponse;
                            }
                            catch (Exception ex) {
                                studentAttendanceResponse.setCode(-1);
                                return studentAttendanceResponse;
                            }
                        }
                        catch (Exception ex) {
                            studentAttendanceResponse.setCode(-1);
                            return studentAttendanceResponse;
                        }
                    }
                    else {
                        //impossible condition
                        studentAttendanceResponse.setCode(0);
                        return studentAttendanceResponse;
                    }
                }
                catch (Exception ex) {
                    studentAttendanceResponse.setCode(-1);
                    return studentAttendanceResponse;
                }
            }
            else {
                //impossible condition
                studentAttendanceResponse.setCode(0);
                return studentAttendanceResponse;
            }
        }
        catch (Exception ex) {
            studentAttendanceResponse.setCode(-1);
            return studentAttendanceResponse;
        }
    }

    @GetMapping("/getProfessorGrups/{professorId}&{subjectId}")
    public ProfessorGrupsResponse getProfessorGrups(@PathVariable int professorId, @PathVariable int subjectId) {
        //this is only meant for schedules that aren't courses
        ProfessorGrupsResponse response = new ProfessorGrupsResponse();
        List<ProfessorGrups> professorGrupsList = new ArrayList<>();

        try {
            List<Schedule> scheduleList = new ArrayList<>();
            scheduleList = scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId);
            for(Schedule schedule : scheduleList) {
                int grupFound = 0;
                for(ProfessorGrups professorGrups : professorGrupsList) {
                    if(professorGrups.getGrup() == schedule.getStudentGrup()) {
                        grupFound = 1;
                        break;
                    }
                }
                if(grupFound == 0) {
                    professorGrupsList.add(new ProfessorGrups(schedule.getStudentGrup()));
                }
            }

            response.setCode(1);
            Collections.sort(professorGrupsList, ProfessorGrups::compareTo);
            response.setProfessorGrupsList(professorGrupsList);
            return response;
        }
        catch (Exception ex) {
            response.setCode(-1);
            return response;
        }
    }

    @GetMapping("/getTotalAttendingStudentsList/{professorId}&{subjectId}&{grup}")
    public StudentAttendanceResponse getTotalAttendingStudentsList(@PathVariable int professorId, @PathVariable int subjectId, @PathVariable int grup) {

        StudentAttendanceResponse studentAttendanceResponse = new StudentAttendanceResponse();
        List<StudentAttendance> studentAttendanceList = new ArrayList<>();

        try {
            Optional<Subject> subject = subjectRepo.findById(subjectId);
            if(subject.isPresent()) {
                try {
                    List<Student> studentList = new ArrayList<>();
                    List<Student> tempStudentList = new ArrayList<>();
                    studentList = studentRepo.findByGradeAndSpec(subject.get().getGrade(), subject.get().getSpec());

                    List<User> userList = new ArrayList<>(); //sort the student list by names
                    userList = userRepo.findByIsAdmin(0);
                    userList.sort(User::compareByName);
                    for(User user : userList) {
                        for(Student student : studentList) {
                            if(user.getId() == student.getUserId()) {
                                tempStudentList.add(student);
                                break;
                            }
                        }
                    }
                    studentList = tempStudentList;
                    try {
                        AttendanceCalendar attendanceCalendar = AttendanceCalendar.getInstance(); //reads the calendar for the year and semester periods

                        //change start and stop depending on semester and current date
                        LocalDate startLocalDate = attendanceCalendar.getYearStart().toLocalDate();
                        LocalDate endLocalDate = attendanceCalendar.getYearStop().toLocalDate();
                        if(subject.get().getSemester() == 1) { //semester intervals
                            endLocalDate = attendanceCalendar.getSemesterIstop().toLocalDate();
                        }
                        else {
                            startLocalDate = attendanceCalendar.getSemesterIIstart().toLocalDate();
                        }
                        LocalDate currentDate = LocalDate.now();
                        if(endLocalDate.compareTo(currentDate) > 0) { //student checks attendance before semester ends
                            endLocalDate = currentDate;
                        }
                        LocalDate nextDate = startLocalDate;

                        List<Schedule> scheduleList = new ArrayList<>();
                        scheduleList = scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId);

                        while (nextDate.isBefore(endLocalDate)) { //iterates through all days
                            for(Schedule schedule : scheduleList) {
                                if (nextDate.getDayOfWeek().getValue() == schedule.getWeekday() && schedule.getStudentGrup() == grup) { //checks if the days match

                                    try {
                                        List<Attendance> attendanceList = new ArrayList<>();
                                        attendanceList = attendanceRepo.findByScanDateAndScheduleId(Date.valueOf(nextDate), schedule.getId());
                                        for(Student student : studentList) {
                                            try {
                                                Optional<User> user = userRepo.findById(student.getUserId());
                                                if(user.isPresent()) {

                                                    int studentFound = 0;
                                                    if(schedule.getStudentGrup() == 0 || schedule.getStudentGrup() == student.getGrup()) { //only take in consideration specific groups or all groups {
                                                        for(Attendance attendance : attendanceList) {
                                                            if(attendance.getScanDate().equals(Date.valueOf(nextDate)) && attendance.getStudentId() == student.getUserId()) {
                                                                studentAttendanceList.add(new StudentAttendance(0, Date.valueOf(nextDate), schedule.getTimeStart(), schedule.getTimeStop(), user.get().getFirstName(), user.get().getLastName(), "present"));
                                                                studentFound = 1;
                                                                break;
                                                            }
                                                        }
                                                        if(studentFound == 0) {
                                                            studentAttendanceList.add(new StudentAttendance(0, Date.valueOf(nextDate), schedule.getTimeStart(), schedule.getTimeStop(), user.get().getFirstName(), user.get().getLastName(), "absent"));
                                                        }
                                                    }
                                                }
                                            }
                                            catch (Exception ex) {
                                                studentAttendanceResponse.setCode(-1);
                                                return studentAttendanceResponse;
                                            }
                                        }
                                    }
                                    catch (Exception ex) {
                                        studentAttendanceResponse.setCode(-1);
                                        return studentAttendanceResponse;
                                    }

                                }
                            }
                            nextDate = nextDate.plus(1, ChronoUnit.DAYS);
                        }

                        studentAttendanceResponse.setCode(1);
                        studentAttendanceResponse.setStudentAttendanceList(studentAttendanceList);
                        return studentAttendanceResponse;
                    }
                    catch (Exception ex) {
                        studentAttendanceResponse.setCode(-1);
                        return studentAttendanceResponse;
                    }
                }
                catch (Exception ex) {
                    studentAttendanceResponse.setCode(-1);
                    return studentAttendanceResponse;
                }
            }
            else {
                studentAttendanceResponse.setCode(0);
                return studentAttendanceResponse;
            }
        }
        catch (Exception ex) {
            studentAttendanceResponse.setCode(-1);
            return studentAttendanceResponse;
        }
    }

    @GetMapping("/getSubjectAttendanceList/{studentId}&{subjectId}")
    public StudentAttendanceResponse getSubjectAttendanceList(@PathVariable int studentId, @PathVariable int subjectId) {

        StudentAttendanceResponse studentAttendanceResponse = new StudentAttendanceResponse();
        List<StudentAttendance> studentAttendanceList = new ArrayList<>();

        try {
            Optional<Student> student = studentRepo.findByUserId(studentId);
            if(student.isPresent()) {
                try {
                    Optional<Subject> subject = subjectRepo.findById(subjectId);
                    if(subject.isPresent()) {
                        AttendanceCalendar attendanceCalendar = AttendanceCalendar.getInstance(); //reads the calendar for the year and semester periods

                        //change start and stop depending on semester and current date
                        LocalDate startLocalDate = attendanceCalendar.getYearStart().toLocalDate();
                        LocalDate endLocalDate = attendanceCalendar.getYearStop().toLocalDate();
                        if(subject.get().getSemester() == 1) { //semester intervals
                            endLocalDate = attendanceCalendar.getSemesterIstop().toLocalDate();
                        }
                        else {
                            startLocalDate = attendanceCalendar.getSemesterIIstart().toLocalDate();
                        }
                        LocalDate currentDate = LocalDate.now();
                        LocalDate nextDate = startLocalDate;

                        try {

                            List<Attendance> attendanceList = new ArrayList<>();
                            attendanceList = attendanceRepo.findByStudentIdAndSubjectId(studentId, subjectId);
                            List<Schedule> scheduleList = new ArrayList<>();
                            scheduleList = scheduleRepo.findBySubjectId(subjectId);

                            int completeCalendarCount = 0;
                            while (nextDate.isBefore(endLocalDate)) { //iterates through all days until semester end
                                for(Schedule schedule : scheduleList) {
                                    if ((schedule.getStudentGrup() == student.get().getGrup() || schedule.getStudentGrup() == 0) && nextDate.getDayOfWeek().getValue() == schedule.getWeekday()) { //checks if the days match
                                        if(nextDate.compareTo(currentDate) <= 0) { //student checks attendance until current day
                                            int attendanceFound = 0;
                                            for(Attendance attendance : attendanceList) {
                                                if(attendance.getScanDate().equals(Date.valueOf(nextDate)) && attendance.getScheduleId() == schedule.getId()) {
                                                    studentAttendanceList.add(new StudentAttendance(1, Date.valueOf(nextDate), schedule.getTimeStart(), schedule.getTimeStop(), "", "", "present"));
                                                    attendanceFound = 1;
                                                    break;
                                                }
                                            }
                                            if(attendanceFound == 0){
                                                studentAttendanceList.add(new StudentAttendance(1, Date.valueOf(nextDate), schedule.getTimeStart(), schedule.getTimeStop(), "", "", "absent"));
                                            }
                                        }
                                        ++completeCalendarCount; //count every valid day for the student to check in app
                                    }
                                }
                                nextDate = nextDate.plus(1, ChronoUnit.DAYS);
                            }

                            studentAttendanceResponse.setCode(1);
                            studentAttendanceResponse.setCompleteCalendarCount(completeCalendarCount);
                            studentAttendanceResponse.setStudentAttendanceList(studentAttendanceList);
                            return studentAttendanceResponse;
                        }
                        catch (Exception ex) {
                            studentAttendanceResponse.setCode(-1);
                            return studentAttendanceResponse;
                        }
                    }
                    else { //impossible request
                        studentAttendanceResponse.setCode(0);
                        return studentAttendanceResponse;
                    }
                }
                catch (Exception ex) {
                    studentAttendanceResponse.setCode(-1);
                    return studentAttendanceResponse;
                }
            }
            else { //impossible request
                studentAttendanceResponse.setCode(0);
                return studentAttendanceResponse;
            }
        }
        catch (Exception ex) {
            studentAttendanceResponse.setCode(-1);
            return studentAttendanceResponse;
        }
    }
}