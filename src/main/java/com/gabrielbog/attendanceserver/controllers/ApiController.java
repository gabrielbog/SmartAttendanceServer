package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.models.*;
import com.gabrielbog.attendanceserver.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    //database access
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
        Optional<User> userList = userRepo.findById(id);
        if (userList.isPresent()) {
            return userList.get();
        } else {
            return null;
        }
    }

    @GetMapping("/getUserByCnpAndPassword/{cnp}&{password}")
    public LogInResponse getUserByCnpAndPassword(@PathVariable String cnp, @PathVariable String password) {
        Optional<User> user = userRepo.findByCnpAndPassword(cnp, password);
        if (user.isPresent()) {
            if(user.get().getIsAdmin() == 0) {
                //check if student exists
                Optional<Student> student = studentRepo.findById(user.get().getId());
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
        } else {
            return new LogInResponse(0, 0, 0, "", "");
        }
    }

    @PostMapping("/addUser")
    public int addUser(@RequestBody User user) {
        try {
            User userList = userRepo.save(user);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @DeleteMapping("/deleteUserById/{id}")
    public int deleteUser(@PathVariable int id) {
        try {
            userRepo.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @DeleteMapping("/deleteAllUsers")
    public int deleteAllUsers() {
        try {
            userRepo.deleteAll();
            return 1;
        } catch (Exception e) {
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
            Student studentList = studentRepo.save(student);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @DeleteMapping("/deleteStudentById/{id}")
    public int deleteStudent(@PathVariable int id) {
        try {
            studentRepo.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}