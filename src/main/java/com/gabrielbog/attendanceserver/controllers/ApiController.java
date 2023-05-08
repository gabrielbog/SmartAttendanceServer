package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.models.LogInResponse;
import com.gabrielbog.attendanceserver.models.User;
import com.gabrielbog.attendanceserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    UserRepository userRepo;

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        try {
            List<User> userList = new ArrayList<>();
            userRepo.findAll().forEach(userList::add);

            if (userList.isEmpty()) {
                return null;
            }

            return userList;
        } catch(Exception ex) {
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
        Optional<User> userList = userRepo.findByCnpAndPassword(cnp, password);
        if (userList.isPresent()) {
            return new LogInResponse(1, userList.get().getId(), userList.get().getIsAdmin(), userList.get().getFirstName(), userList.get().getLastName());
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

    @DeleteMapping("/deleteUserById")
    public int deleteUser(@PathVariable int id) {
        try {
            userRepo.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @DeleteMapping("/deleteAllUers")
    public int deleteAllUsers() {
        try {
            userRepo.deleteAll();
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

}