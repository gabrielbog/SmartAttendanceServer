package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.Constants;
import com.gabrielbog.attendanceserver.models.Specialization;
import com.gabrielbog.attendanceserver.models.Student;
import com.gabrielbog.attendanceserver.models.Subject;
import com.gabrielbog.attendanceserver.models.User;
import com.gabrielbog.attendanceserver.repositories.*;
import com.gabrielbog.attendanceserver.views.AddStudentForm;
import com.gabrielbog.attendanceserver.views.AddSubjectForm;
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
import java.util.ArrayList;
import java.util.List;
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

        try {
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
        catch (Exception ex) {
            model.addAttribute("addUserFormError", "An error has occurred. Please try again.");
            return "addUser";
        }
    }

    /*
        Add Student
    */
    @GetMapping("/addStudent")
    public String getAddStudentPage(Model model, HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Specialization> specializationList = new ArrayList<>();
        try {
            specialzationRepo.findAll().forEach(specializationList::add);
            model.addAttribute("specList", specializationList);
            return "addStudent";
        }
        catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addStudent")
    public String addStudent(@ModelAttribute("addStudentForm") AddStudentForm addStudentForm, Model model, HttpSession session) {

        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String firstName = addStudentForm.getFirstName();
        String lastName = addStudentForm.getLastName();
        String cnp =  addStudentForm.getCnp();
        String password = addStudentForm.getPassword();
        String specInput = addStudentForm.getSpecialization();
        String gradeInput = addStudentForm.getGrade();
        String grupInput = addStudentForm.getGrup();

        if(cnp.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || password.length() == 0 || specInput.length() == 0 || gradeInput.length() == 0 || grupInput.length() == 0) {
            model.addAttribute("addStudentFormError", "Please fill in all the boxes.");
            return "addStudent";
        }

        int spec = Integer.valueOf(specInput);
        int grade = Integer.valueOf(gradeInput);
        int grup = Integer.valueOf(grupInput);

        if(spec == 0) {
            model.addAttribute("addStudentFormError", "Select a specialization.");
            return "addStudent";
        }

        if(cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("addStudentFormError", "CNP is too short.");
            return "addStudent";
        }

        try {
            Optional<Specialization> specElement = specialzationRepo.findById(spec);
            if(specElement.isPresent()) {
                if (grade < specElement.get().getMaxYears()) {

                    Optional<User> existingUser = userRepo.findByCnp(cnp);
                    if(existingUser.isPresent()) {
                        model.addAttribute("addStudentFormError", "User with CNP already exists.");
                        return "addStudent";
                    }

                    try {
                        User user = new User();
                        System.out.println(user.getId());
                        user.setIsAdmin(0);
                        user.setCnp(cnp);
                        user.setPassword(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString());
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        userRepo.save(user);

                        try {
                            Student student = new Student();
                            student.setUserId(user.getId());
                            student.setSpec(spec);
                            student.setGrade(grade);
                            student.setGrup(grup);
                            studentRepo.save(student);
                            return "redirect:/index"; //redirect to list of users when said page is implemented
                        }
                        catch (Exception ex) {
                            model.addAttribute("addStudentFormError", "An error has occurred while adding the student.");
                            return "addStudent";
                        }
                    }
                    catch(Exception ex) {
                        model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
                        return "addStudent";
                    }
                }
                else {
                    model.addAttribute("addStudentFormError", "Grade exceeds maximum year for selected specialization.");
                    return "addStudent";
                }
            }
            else {
                model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
                return "addStudent";
            }
        }
        catch (Exception ex) {
            model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
            return "addStudent";
        }
    }

    /*
        Add Subject
    */
    @GetMapping("/addSubject")
    public String getAddSubjectPage(Model model, HttpSession session) {
        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Specialization> specializationList = new ArrayList<>();
        try {
            specialzationRepo.findAll().forEach(specializationList::add);
            model.addAttribute("specList", specializationList);
            return "addSubject";
        }
        catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addSubject")
    public String addSubject(@ModelAttribute("addSubjectForm") AddSubjectForm addSubjectForm, Model model, HttpSession session) {

        if(session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String name = addSubjectForm.getName();
        String specInput = addSubjectForm.getSpecialization();
        String gradeInput = addSubjectForm.getGrade();
        String type = addSubjectForm.getType();
        String semesterInput = addSubjectForm.getSemester();
        String attendanceTotalInput = addSubjectForm.getAttendanceTotal();
        String absencesAllowedInput = addSubjectForm.getAbsencesAllowed();

        System.out.println(specInput);
        System.out.println(type);
        System.out.println(semesterInput);

        if(name.length() == 0 || specInput.length() == 0 || gradeInput.length() == 0 || type.length() == 0 || semesterInput.length() == 0 || attendanceTotalInput.length() == 0 || absencesAllowedInput.length() == 0) {
            model.addAttribute("addSubjectFormError", "Please fill in all the boxes.");
            return "addSubject";
        }

        int spec = Integer.valueOf(specInput);
        int grade = Integer.valueOf(gradeInput);
        int semester = Integer.valueOf(semesterInput);
        int attendanceTotal = Integer.valueOf(attendanceTotalInput);
        int absencesAllowed = Integer.valueOf(absencesAllowedInput);

        if(spec == 0 || semester <= 0 || semester > 2 || type.equals("none")) {
            model.addAttribute("addSubjectFormError", "Select a valid element for each box.");
            return "addSubject";
        }

        try {
            Optional<Specialization> specElement = specialzationRepo.findById(spec);
            if(specElement.isPresent()) {
                System.out.println(grade);
                System.out.println(specElement.get().getMaxYears());
                if (grade < specElement.get().getMaxYears()) {

                    Subject subject = new Subject();
                    subject.setName(name);
                    subject.setSpec(spec);
                    subject.setGrade(grade);
                    subject.setType(type);
                    subject.setSemester(semester);
                    subject.setAttendanceTotal(attendanceTotal);
                    subject.setAbsencesAllowed(absencesAllowed);
                    subjectRepo.save(subject);
                    return "redirect:/index"; //redirect to list of users when said page is implemented
                }
                else {
                    model.addAttribute("addSubjectFormError", "Grade exceeds maximum year for selected specialization.");
                    return "addSubject";
                }
            }
            else {
                model.addAttribute("addSubjectFormError", "An error has occurred. Please try again.");
                return "addSubject";
            }
        }
        catch (Exception ex) {
            model.addAttribute("addSubjectFormError", "An error has occurred. Please try again.");
            return "addSubject";
        }
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