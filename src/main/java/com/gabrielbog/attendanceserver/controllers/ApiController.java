package com.gabrielbog.attendanceserver.controllers;

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

    //Constants
    private static final int CODE_DURATION = 300000; //5 minutes in milliseconds

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


    //http requests

    /*
        users
    */
    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        try {
            List<User> userList = new ArrayList<>();
            userRepo.findAll().forEach(userList::add);

            if (userList.isEmpty()) {
                return null;
            }

            return userList;
        }
        catch(Exception ex) {
            return null;
        }
    }

    @GetMapping("/getUserById/{id}")
    public User getUserById(@PathVariable int id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        else {
            return null;
        }
    }

    @GetMapping("/getUserByCnpAndPassword/{cnp}&{password}")
    public LogInResponse getUserByCnpAndPassword(@PathVariable String cnp, @PathVariable String password) {
        //might be a good idea to find only by cnp, then compare passwords by code
        Optional<User> user = userRepo.findByCnpAndPassword(cnp, password);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (user.isPresent()) {
            if(user.get().getIsAdmin() == 0) {
                //check if student exists
                Optional<Student> student = studentRepo.findByUserId(user.get().getId());
                if(student.isPresent()) {
                    System.out.println("[" + timestamp.toString() + "] Student " + user.get().getCnp() + " logged in"); //debug
                    return new LogInResponse(1, user.get().getId(), user.get().getIsAdmin(), user.get().getFirstName(), user.get().getLastName());
                }
                else {
                    //invalid user - might be a good idea to store this in a log on the server
                    System.out.println("[" + timestamp.toString() + "] !!! STUDENT " + user.get().getCnp() + " HAS NO STUDENT TABLE ENTRY !!!"); //debug
                    return new LogInResponse(0, 0, 0, "", "");
                }
            }
            else {
                //return professor response
                System.out.println("[" + timestamp.toString() + "] Professor " + user.get().getCnp() + " logged in"); //debug
                return new LogInResponse(1, user.get().getId(), user.get().getIsAdmin(), user.get().getFirstName(), user.get().getLastName());
            }
        }
        else {
            return new LogInResponse(0, 0, 0, "", "");
        }
    }

    @PostMapping("/addUser")
    public int addUser(@RequestBody User user) {
        try {
            //hash the password using sha128/256
            userRepo.save(user);
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    @DeleteMapping("/deleteUserById/{id}")
    public int deleteUser(@PathVariable int id) {
        try {
            userRepo.deleteById(id);
            //check if user is student too, delete from that database aswell
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    @DeleteMapping("/deleteAllUsers")
    public int deleteAllUsers() {
        try {
            userRepo.deleteAll();
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    /*
        students
    */

    @GetMapping("/getAllStudents")
    public List<Student> getAllStudents() {
        try {
            List<Student> studList = new ArrayList<>();
            studentRepo.findAll().forEach(studList::add);

            if (studList.isEmpty()) {
                return null;
            }

            return studList;
        }
        catch(Exception ex) {
            return null;
        }
    }

    @PostMapping("/addStudent")
    public int addStudent(@RequestBody Student student) {
        try {
            Optional<User> user = userRepo.findById(student.getUserId());
            if (user.isPresent()) {
                if(user.get().getIsAdmin() != 1) { //save student if not professor
                    try {
                        studentRepo.save(student);
                        return 2;
                    }
                    catch (Exception e) {
                        return -1;
                    }
                }
                else { //don't add anything if user is admin/professor in the user table
                    return 1;
                }
            }
            else {  //don't add anything if user doesn't exist in the user table
                return 0;
            }
        }
        catch (Exception ex) {
            return -1;
        }
    }

    @DeleteMapping("/deleteStudentById/{id}")
    public int deleteStudent(@PathVariable int id) {
        try {
            studentRepo.deleteById(id);
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    /*
        specializations
    */

    @PostMapping("/addSpecialization")
    public int addSpecialization(@RequestBody Specialization specialization) {
        try {
            specialzationRepo.save(specialization);
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    /*
        subjects
    */

    @GetMapping("/getAllSubjects")
    public List<Subject> getAllSubjects() {
        try {
            List<Subject> subjectList = new ArrayList<>();
            subjectRepo.findAll().forEach(subjectList::add);

            if (subjectList.isEmpty()) {
                return null;
            }

            return subjectList;
        }
        catch(Exception ex) {
            return null;
        }
    }

    @PostMapping("/addSubject")
    public int addSubject(@RequestBody Subject subject) {
        try {
            subjectRepo.save(subject);
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

    /*
        scancode
    */
    @GetMapping("/getScancodeByCode/{code}")
    public Scancode getScancodeByCode(@PathVariable String code) {
        try {
            Optional<Scancode> scancode = scancodeRepo.findByCode(code);
            if(scancode.isPresent()) {
                return scancode.get();
            }
            else {
                return null;
            }
        }
        catch(Exception ex) {
            return null;
        }
    }

    /*
        attendance
    */

    /*
    //here in case short duration code validity doesn't work
    @GetMapping("/generateQrCode/{id}")
    public QrCodeResponse generateQrCode(@PathVariable int id) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis()); //replace with currentDate and currentTime
        LocalDate currentDate = LocalDate.now();
        Time currentTime = Time.valueOf(LocalTime.now());

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {

                if(user.get().getIsAdmin() == 1) {

                    try {

                        List<Schedule> scheduleList = new ArrayList<>();
                        scheduleRepo.findByProfessorId(id).forEach(scheduleList::add);

                        for(Schedule schedule : scheduleList) {

                            int timeStartDifference = currentTime.compareTo(schedule.getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(schedule.getTimeStop());

                            //check if the schedule found is right for this time
                            if(schedule.getWeekday() == currentDate.getDayOfWeek().getValue() && timeStartDifference > 0 && timeStopDifference < 0) {

                                String qrString = Hashing.sha256()
                                        .hashString(String.valueOf(id) + user.get().getFirstName() + schedule.getId() + dateTimeFormat.format(timestamp).toString(), StandardCharsets.UTF_8)
                                        .toString();

                                Scancode scancode = new Scancode(); //it's impossible to append the values inside the constructor without it requiring a value for the id
                                scancode.setSubjectId(schedule.getSubjectId());
                                scancode.setScheduleId(schedule.getId());
                                scancode.setCode(qrString);
                                scancode.setCreationDate(Date.valueOf(currentDate));
                                scancode.setTimeStart(schedule.getTimeStart());
                                scancode.setTimeStop(schedule.getTimeStop());

                                try {
                                    scancodeRepo.save(scancode);
                                }
                                catch (Exception ex) {
                                    return new QrCodeResponse(-1, "Error during request.", ""); //error during request
                                }

                                try {
                                    Optional<Subject> subject = subjectRepo.findById(schedule.getSubjectId());
                                    if(subject.isPresent()) {
                                        System.out.println("[" + timestamp.toString() + "] Professor " + user.get().getFirstName() + " code: " + qrString); //debug
                                        return new QrCodeResponse(2, qrString, subject.get().getName());
                                    }
                                    else { //impossible condition
                                        System.out.println("[" + timestamp.toString() + "] Professor " + user.get().getFirstName() + " code: " + qrString); //debug
                                        return new QrCodeResponse(2, qrString, "");
                                    }
                                }
                                catch (Exception ex) {
                                    return new QrCodeResponse(-1, "Error during request.", ""); //error during request
                                }
                            }
                        }

                        return new QrCodeResponse(3, "You don't have any schedule in progress right now.", ""); //error during request
                    }
                    catch (Exception ex) {
                        return new QrCodeResponse(-1, "Error during request.", ""); //error during request
                    }
                }
                else {
                    System.out.println("[" + timestamp.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
                    return new QrCodeResponse(1, "", ""); //request from non-professor
                }
            }
            else {
                return new QrCodeResponse(0, "", ""); //invalid id
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, "Error during request.", ""); //error during request
        }
    }
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
                        scheduleRepo.findByProfessorId(id).forEach(scheduleList::add);

                        for(Schedule schedule : scheduleList) {

                            int timeStartDifference = currentTime.compareTo(schedule.getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(schedule.getTimeStop());

                            //check if the schedule found is right for this time
                            if(schedule.getWeekday() == currentDate.getDayOfWeek().getValue() && timeStartDifference > 0 && timeStopDifference < 0) {

                                try {

                                    Optional<Scancode> existingScancode = scancodeRepo.findByCreationDateAndScheduleId(Date.valueOf(currentDate), schedule.getId());
                                    Scancode scancode = new Scancode();
                                    if(existingScancode.isPresent()) { //generated code already exists

                                        scancode = existingScancode.get();
                                        long timeDifference = currentTime.getTime() - scancode.getTimeGenerated().getTime();
                                        System.out.println(timeDifference);
                                        if(timeDifference >= CODE_DURATION) {

                                            String qrString = Hashing.sha256()
                                                    .hashString(String.valueOf(id) + user.get().getFirstName() + schedule.getId() + currentDate.toString() + " " + currentTime.toString(), StandardCharsets.UTF_8)
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
                                                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
                                                    return new QrCodeResponse(2, CODE_DURATION, qrString, subject.get().getName(), schedule.getStudentGrup());
                                                }
                                                else { //impossible condition
                                                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
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
                                                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " requested code: " + existingScancode.get().getCode()); //debug
                                                    return new QrCodeResponse(2, CODE_DURATION - timeDifference, existingScancode.get().getCode(), subject.get().getName(), schedule.getStudentGrup());
                                                }
                                                else { //impossible condition
                                                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " requested code: " + existingScancode.get().getCode()); //debug
                                                    return new QrCodeResponse(2, 0, existingScancode.get().getCode(), "", 0);
                                                }
                                            }
                                            catch (Exception ex) {
                                                return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                            }
                                        }
                                    }
                                    else { //create a new code
                                        String qrString = Hashing.sha256()
                                                .hashString(String.valueOf(id) + user.get().getFirstName() + schedule.getId() + currentDate.toString() + " " + currentTime.toString(), StandardCharsets.UTF_8)
                                                .toString();

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
                                                System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " generated code: " + qrString); //debug
                                                return new QrCodeResponse(2, CODE_DURATION, qrString, subject.get().getName(), schedule.getStudentGrup());
                                            }
                                            else { //impossible condition
                                                System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " generated code: " + qrString); //debug
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
                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
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

                            int timeStartDifference = currentTime.compareTo(scancode.get().getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(scancode.get().getTimeStop());

                            if(timeStartDifference > 0 && timeStopDifference < 0) {
                                String qrString = Hashing.sha256()
                                        .hashString(String.valueOf(id) + user.get().getFirstName() + scancode.get().getScheduleId() + currentDate.toString() + " " + currentTime.toString(), StandardCharsets.UTF_8)
                                        .toString();
                                scancode.get().setCode(qrString);
                                scancode.get().setTimeGenerated(currentTime);

                                try {
                                    scancodeRepo.save(scancode.get());
                                }
                                catch (Exception ex) {
                                    return new QrCodeResponse(-1, 0, "Error during request.", "", 0); //error during request
                                }

                                System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] Professor " + user.get().getFirstName() + " refreshed new code: " + qrString); //debug
                                return new QrCodeResponse(2, CODE_DURATION, qrString, "", 0); //returning subjectString and grup is pointless, mobile app is supposed to have it already
                            }
                            else {
                                return new QrCodeResponse(3, 0, "The schedule has finished.", "", 0);
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
                    System.out.println("[" + currentDate.toString() + " " + currentTime.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
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

        System.out.println(id + " " + code); //debug

        try {
            Optional<Student> student = studentRepo.findByUserId(id);
            if (student.isPresent()) {

                try {
                    Optional<Scancode> scancode = scancodeRepo.findByCode(code);
                    if(scancode.isPresent()) {

                        try {

                            List<Attendance> attendanceList = new ArrayList<>();
                            attendanceRepo.findByScanDate(currentDate).forEach(attendanceList::add);

                            for(Attendance attendance : attendanceList) {
                                if(attendance.getStudentId() == id && attendance.getScheduleId() == scancode.get().getScheduleId()) { //stop if user already scanned this code once
                                    return new QrCodeResponse(1, 0, "You're already attending this schedule.", "", 0);
                                }
                            }

                            int timeStartDifference = currentTime.compareTo(scancode.get().getTimeStart()); //timeStartDifference > 0 - currentTime comes after timeStart; < 0 - currentTime comes before timeStart
                            int timeStopDifference = currentTime.compareTo(scancode.get().getTimeStop());

                            //check if the date and time at scan is valid
                            if(scancode.get().getCreationDate().equals(currentDate) && timeStartDifference > 0 && timeStopDifference < 0) {
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

                                                        System.out.println("[" + timestamp.toString() + "] Student " + student.get().getUserId() + " scanned code successfully"); //debug
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
        @GetMapping("/refreshQrCode/{id}&{code}")
        public QrCodeResponse refreshQrCode(@PathVariable int id, @PathVariable String code) {
            //check if date and time are still valid, if not, return error code and remove qr code in-app
            //else search for code, if exists, make new hash like at generation, but change the table entry instead
        }
    */

    /*
        schedules
    */

    @PostMapping("/addSchedule")
    public int addSchedule(@RequestBody Schedule schedule) {
        try {
            scheduleRepo.save(schedule);
            return 1;
        }
        catch (Exception ex) {
            return 0;
        }
    }

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
                        scheduleRepo.findByProfessorId(user.get().getId()).forEach(scheduleList::add);
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

                        //sort it alphabetically
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
                                subjectRepo.findBySpecAndGrade(student.get().getSpec(), student.get().getGrade()).forEach(subjectList::add); //build list with student year and grade subjects
                                //sort it alphabetically
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
                    scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId).forEach(scheduleList::add);

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
                            studentRepo.findByGradeAndSpec(subject.get().getGrade(), subject.get().getSpec()).forEach(studentList::add);
                            try {
                                List<Attendance> attendanceList = new ArrayList<>();
                                attendanceRepo.findByScanDateAndScheduleId(scanDate, scheduleId).forEach(attendanceList::add);
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
            scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId).forEach(scheduleList::add);
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
                    studentRepo.findByGradeAndSpec(subject.get().getGrade(), subject.get().getSpec()).forEach(studentList::add);
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
                        scheduleRepo.findByProfessorIdAndSubjectId(professorId, subjectId).forEach(scheduleList::add);

                        while (nextDate.isBefore(endLocalDate)) { //iterates through all days
                            for(Schedule schedule : scheduleList) {
                                if (nextDate.getDayOfWeek().getValue() == schedule.getWeekday() && schedule.getStudentGrup() == grup) { //checks if the days match

                                    try {
                                        List<Attendance> attendanceList = new ArrayList<>();
                                        attendanceRepo.findByScanDateAndScheduleId(Date.valueOf(nextDate), schedule.getId()).forEach(attendanceList::add);
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
                            attendanceRepo.findByStudentIdAndSubjectId(studentId, subjectId).forEach(attendanceList::add); //this might not work, check out
                            List<Schedule> scheduleList = new ArrayList<>();
                            scheduleRepo.findBySubjectId(subjectId).forEach(scheduleList::add);

                            int completeCalendarCount = 0;
                            while (nextDate.isBefore(endLocalDate)) { //iterates through all days
                                for(Schedule schedule : scheduleList) {
                                    if ((schedule.getStudentGrup() == student.get().getGrup() || schedule.getStudentGrup() == 0) && nextDate.getDayOfWeek().getValue() == schedule.getWeekday()) { //checks if the days match
                                        if(nextDate.compareTo(currentDate) < 0) { //student checks attendance before semester ends
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