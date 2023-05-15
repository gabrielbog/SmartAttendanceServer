package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.models.*;
import com.gabrielbog.attendanceserver.repositories.*;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    //Time Format
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy:HH.mm.ss");

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
        if (user.isPresent()) {
            if(user.get().getIsAdmin() == 0) {
                //check if student exists
                Optional<Student> student = studentRepo.findByUserId(user.get().getId());
                if(student.isPresent()) {
                    return new LogInResponse(1, user.get().getId(), user.get().getIsAdmin(), user.get().getFirstName(), user.get().getLastName());
                }
                else {
                    //invalid user - might be a good idea to store this in a log on the server
                    return new LogInResponse(0, 0, 0, "", "");
                }
            }
            else {
                //return professor response
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
        attendance
    */

    @GetMapping("/generateQrCode/{id}")
    public QrCodeResponse
            e(@PathVariable int id) {
        //search for professor
        //if professor exists, search for the most appropriate schedule row at the time of the request based on received id
        //if there's any appropriate schedule, generate sha1 hash of id + lastName + time mentioned above and send back

        try {
            Optional<User> user = userRepo.findById(id);
            if (user.isPresent()) {
                if(user.get().getIsAdmin() == 1) {
                    //search for appropriate schedule

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String qrString = Hashing.sha256()
                            .hashString(String.valueOf(id) + user.get().getFirstName() + dateTimeFormat.format(timestamp).toString(), StandardCharsets.UTF_8)
                            .toString();

                    //store on database

                    System.out.println("Professor " + user.get().getFirstName() + " generated at " + dateTimeFormat.format(timestamp).toString() + " code: " + qrString); //debug
                    return new QrCodeResponse(2, qrString);
                }
                else {
                    return new QrCodeResponse(1, ""); //request from non-professor
                }
            }
            else {
                return new QrCodeResponse(0, ""); //invalid id
            }
        }
        catch (Exception ex) {
            return new QrCodeResponse(-1, ""); //error during request
        }
    }
}