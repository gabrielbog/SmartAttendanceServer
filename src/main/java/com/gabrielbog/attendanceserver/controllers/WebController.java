package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.Constants;
import com.gabrielbog.attendanceserver.models.User;
import com.gabrielbog.attendanceserver.repositories.*;
import com.gabrielbog.attendanceserver.views.AddUserForm;
import com.gabrielbog.attendanceserver.views.LoginForm;
import com.google.common.hash.Hashing;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class WebController {

    //Variables
    @Autowired
    private Environment env;

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

    /*
        Index
    */
    @GetMapping("/")
    public String getMainPage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    /*
        Log In Page
    */
    @GetMapping("/login")
    public String getLogInPage(HttpSession session) {
        if(session.getAttribute("status") != null) {
            return "redirect:/index";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginForm loginForm, Model model, HttpSession session) {

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();

        if(username.equals(env.getProperty("spring.datasource.username")) && password.equals(env.getProperty("spring.datasource.password"))) {
            session.setAttribute("status", 1); //user is now logged in
            return "redirect:/index";
        }
        model.addAttribute("invalidCredentials", true);
        return "login";
    }

    /*
        Log Out
    */
    @GetMapping("/logout")
    public String logOutAction(HttpSession session) {
        if(session.getAttribute("status") != null) {
            session.removeAttribute("status");
        }
        return "redirect:/login";
    }

    /*
        Add User
    */
    @GetMapping("/addUser")
    public String getAddUserPage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("addUserForm") AddUserForm addUserForm, Model model, HttpSession session) {

        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String firstName = addUserForm.getFirstName();
        String lastName = addUserForm.getLastName();
        String cnp =  addUserForm.getCnp();
        String password = addUserForm.getPassword();

        if(cnp.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || password.length() == 0) {
            model.addAttribute("addUserFormError", "Please fill in all the boxes.");
            return "addUser";
        }

        if(cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("addUserFormError", "CNP is too short.");
            return "addUser";
        }

        Optional<User> existingUser = userRepo.findByCnp(cnp);
        if(existingUser.isPresent()) {
            model.addAttribute("addUserFormError", "User with CNP already exists.");
            return "addUser";
        }

        try {
            User user = new User();
            user.setIsAdmin(1);
            user.setCnp(cnp);
            user.setPassword(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepo.save(user);
            return "redirect:/index"; //redirect to list of users when said page is implemented
        }
        catch(Exception ex) {
            model.addAttribute("addUserFormError", "An error has occurred. Please try again.");
            return "addUser";
        }
    }

    /*
        Add Student
    */
    @GetMapping("/addStudent")
    public String getAddStudentPage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addStudent";
    }

    /*
        Add Subject
    */
    @GetMapping("/addSubject")
    public String getAddSubjectPage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addSubject";
    }

    /*
        Add Schedule
    */
    @GetMapping("/addSchedule")
    public String getAddSchedulePage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addSchedule";
    }

    /*
        Add Attendance
    */
    @GetMapping("/addAttendance")
    public String getAddAttendancePage(HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addAttendance";
    }
}