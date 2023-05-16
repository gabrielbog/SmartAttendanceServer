package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.models.*;
import com.gabrielbog.attendanceserver.repositories.*;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    //Time Format
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy:HH.mm.ss");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");

    //Database Access
    @Autowired
    UserRepository userRepo;

    @Autowired
    StudentRepository studentRepo;

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

    @GetMapping("/generateQrCode/{id}")
    public QrCodeResponse generateQrCode(@PathVariable int id) {
        //search for professor
        //if professor exists, search for the most appropriate schedule row at the time of the request based on received id
        //if there's any appropriate schedule, generate sha1 hash of id + lastName + time mentioned above and send back
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                if(user.get().getIsAdmin() == 1) {
                    //search for appropriate schedule

                    String qrString = Hashing.sha256()
                            .hashString(String.valueOf(id) + user.get().getFirstName() + dateTimeFormat.format(timestamp).toString(), StandardCharsets.UTF_8)
                            .toString();

                    //get these from schedule table later
                    Time timeStart = Time.valueOf(LocalTime.now());
                    Scancode scancode = new Scancode(); //it's impossible to append the values inside the constructor without it requiring a value for the id
                    scancode.setSubjectId(0); //change this to the id from the schedule table
                    scancode.setCode(qrString);
                    scancode.setTimeStart(timeStart); //change these values to the time from the schedule table
                    scancode.setTimeStop(timeStart);

                    try {
                        scancodeRepo.save(scancode);
                    }
                    catch (Exception ex) {
                        return new QrCodeResponse(-1, "Error during request."); //error during request
                    }

                    System.out.println("[" + timestamp.toString() + "] Professor " + user.get().getFirstName() + " code: " + qrString); //debug
                    return new QrCodeResponse(2, qrString);
                }
                else {
                    System.out.println("[" + timestamp.toString() + "] !!! STUDENT " + user.get().getCnp() + " TRIED TO GENERATE QR CODE !!!"); //debug
                    return new QrCodeResponse(1, ""); //request from non-professor
                }
            }
            else {
                return new QrCodeResponse(0, ""); //invalid id
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, "Error during request."); //error during request
        }
    }

    @GetMapping("/scanQrCode/{id}&{code}")
    public QrCodeResponse scanQrCode(@PathVariable int id, @PathVariable String code) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date date = Date.valueOf(LocalDate.now());
        Time time = Time.valueOf(LocalTime.now());

        System.out.println(id + " " + code); //debug

        try {

            Optional<Student> student = studentRepo.findByUserId(id);
            if (student.isPresent()) {
                try {

                    Optional<Scancode> scancode = scancodeRepo.findByCode(code);
                    if(scancode.isPresent()) {

                        Attendance attendance = new Attendance();
                        attendance.setStudentId(id);
                        attendance.setScheduleId(0);
                        attendance.setScanDate(date);
                        attendance.setScanTime(time);
                        try {
                            attendanceRepo.save(attendance);
                        }
                        catch (Exception ex) {
                            return new QrCodeResponse(-1, "Error during request."); //error during request
                        }

                        System.out.println("[" + timestamp.toString() + "] Student " + student.get().getUserId() + " scanned code successfully"); //debug
                        return new QrCodeResponse(2, "You're now attending!"); //error during request
                    }
                    else {
                        return new QrCodeResponse(1, "Invalid QR Code."); //error during request
                    }
                }
                catch (Exception ex) {
                    return new QrCodeResponse(-1, "Error during request."); //error during request
                }
            }
            else {
                return new QrCodeResponse(0, "Invalid student."); //error during request
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, "Error during request."); //error during request
        }
    }
}