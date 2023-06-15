package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.Constants;
import com.gabrielbog.attendanceserver.models.*;
import com.gabrielbog.attendanceserver.repositories.*;
import com.gabrielbog.attendanceserver.views.*;
import com.google.common.hash.Hashing;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    //Variables
    @Autowired
    Environment env;

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
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    /*
        Log In Page
    */
    @GetMapping("/login")
    public String getLogInPage(HttpSession session) {
        if (session.getAttribute("status") != null) {
            return "redirect:/index";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginForm loginForm, Model model, HttpSession session) {

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();

        if (username.equals(env.getProperty("spring.datasource.username")) && password.equals(env.getProperty("spring.datasource.password"))) {
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
        if (session.getAttribute("status") != null) {
            session.removeAttribute("status");
        }
        return "redirect:/login";
    }

    /*
        Add User
    */
    @GetMapping("/addUser")
    public String getAddUserPage(HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }
        return "addUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("addUserForm") AddUserForm addUserForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String firstName = addUserForm.getFirstName();
        String lastName = addUserForm.getLastName();
        String cnp = addUserForm.getCnp();
        String password = addUserForm.getPassword();

        if (cnp.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || password.length() == 0) {
            model.addAttribute("addUserFormError", "Please fill in all the boxes.");
            return "addUser";
        }

        if (cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("addUserFormError", "CNP is too short.");
            return "addUser";
        }

        try {
            Optional<User> existingUser = userRepo.findByCnp(cnp);
            if (existingUser.isPresent()) {
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
            } catch (Exception ex) {
                model.addAttribute("addUserFormError", "An error has occurred. Please try again.");
                return "addUser";
            }
        } catch (Exception ex) {
            model.addAttribute("addUserFormError", "An error has occurred. Please try again.");
            return "addUser";
        }
    }

    /*
        Add Student
    */
    @GetMapping("/addStudent")
    public String getAddStudentPage(Model model, HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Specialization> specializationList = new ArrayList<>();
        try {
            specialzationRepo.findAll().forEach(specializationList::add);
            model.addAttribute("specList", specializationList);
            return "addStudent";
        } catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addStudent")
    public String addStudent(@ModelAttribute("addStudentForm") AddStudentForm addStudentForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String firstName = addStudentForm.getFirstName();
        String lastName = addStudentForm.getLastName();
        String cnp = addStudentForm.getCnp();
        String password = addStudentForm.getPassword();
        String specInput = addStudentForm.getSpecialization();
        String gradeInput = addStudentForm.getGrade();
        String grupInput = addStudentForm.getGrup();

        if (cnp.length() == 0 || firstName.length() == 0 || lastName.length() == 0 || password.length() == 0 || specInput.length() == 0 || gradeInput.length() == 0 || grupInput.length() == 0) {
            model.addAttribute("addStudentFormError", "Please fill in all the boxes.");
            return "addStudent";
        }

        int spec = Integer.valueOf(specInput);
        int grade = Integer.valueOf(gradeInput);
        int grup = Integer.valueOf(grupInput);

        if (spec == 0) {
            model.addAttribute("addStudentFormError", "Select a specialization.");
            return "addStudent";
        }

        if (cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("addStudentFormError", "CNP is too short.");
            return "addStudent";
        }

        try {
            Optional<Specialization> specElement = specialzationRepo.findById(spec);
            if (specElement.isPresent()) {
                if (grade < specElement.get().getMaxYears()) {

                    Optional<User> existingUser = userRepo.findByCnp(cnp);
                    if (existingUser.isPresent()) {
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
                        } catch (Exception ex) {
                            model.addAttribute("addStudentFormError", "An error has occurred while adding the student.");
                            return "addStudent";
                        }
                    } catch (Exception ex) {
                        model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
                        return "addStudent";
                    }
                } else {
                    model.addAttribute("addStudentFormError", "Grade exceeds maximum year for selected specialization.");
                    return "addStudent";
                }
            } else {
                model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
                return "addStudent";
            }
        } catch (Exception ex) {
            model.addAttribute("addStudentFormError", "An error has occurred. Please try again.");
            return "addStudent";
        }
    }

    /*
        Add Subject
    */
    @GetMapping("/addSubject")
    public String getAddSubjectPage(Model model, HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Specialization> specializationList = new ArrayList<>();
        try {
            specialzationRepo.findAll().forEach(specializationList::add);
            model.addAttribute("specList", specializationList);
            return "addSubject";
        } catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addSubject")
    public String addSubject(@ModelAttribute("addSubjectForm") AddSubjectForm addSubjectForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
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

        if (name.length() == 0 || specInput.length() == 0 || gradeInput.length() == 0 || type.length() == 0 || semesterInput.length() == 0 || attendanceTotalInput.length() == 0 || absencesAllowedInput.length() == 0) {
            model.addAttribute("addSubjectFormError", "Please fill in all the boxes.");
            return "addSubject";
        }

        int spec = Integer.valueOf(specInput);
        int grade = Integer.valueOf(gradeInput);
        int semester = Integer.valueOf(semesterInput);
        int attendanceTotal = Integer.valueOf(attendanceTotalInput);
        int absencesAllowed = Integer.valueOf(absencesAllowedInput);

        if (spec == 0 || semester <= 0 || semester > 2 || type.equals("none")) {
            model.addAttribute("addSubjectFormError", "Select a valid element for each box.");
            return "addSubject";
        }

        try {
            Optional<Specialization> specElement = specialzationRepo.findById(spec);
            if (specElement.isPresent()) {
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
                } else {
                    model.addAttribute("addSubjectFormError", "Grade exceeds maximum year for selected specialization.");
                    return "addSubject";
                }
            } else {
                model.addAttribute("addSubjectFormError", "An error has occurred. Please try again.");
                return "addSubject";
            }
        } catch (Exception ex) {
            model.addAttribute("addSubjectFormError", "An error has occurred. Please try again.");
            return "addSubject";
        }
    }

    /*
        Add Schedule
    */
    @GetMapping("/addSchedule")
    public String getAddSchedulePage(Model model, HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Subject> subjectList = new ArrayList<>();
        List<User> professorList = new ArrayList<>();
        try {
            subjectRepo.findAll().forEach(subjectList::add);

            try {
                userRepo.findByIsAdmin(1).forEach(professorList::add);
                model.addAttribute("subjectList", subjectList);
                model.addAttribute("professorList", professorList);
                return "addSchedule";
            } catch (Exception ex) {
                return "redirect:/index";
            }
        } catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addSchedule")
    public String addSchedule(@ModelAttribute("addScheduleForm") AddScheduleForm addScheduleForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String subjectIdInput = addScheduleForm.getSubject();
        String professorIdInput = addScheduleForm.getProfessor();
        String timeStartInput = addScheduleForm.getTimeStart();
        String timeStopInput = addScheduleForm.getTimeStop();
        String weekdayInput = addScheduleForm.getWeekday();
        String studentGrupInput = addScheduleForm.getGrup();
        String room = addScheduleForm.getRoom();

        if (subjectIdInput.length() == 0 || professorIdInput.length() == 0 || timeStartInput.length() == 0 || timeStopInput.length() == 0 || weekdayInput.length() == 0 || studentGrupInput.length() == 0 || room.length() == 0) {
            model.addAttribute("addScheduleFormError", "Please fill in all the boxes.");
            return "addSchedule";
        }

        int subjectId = Integer.valueOf(subjectIdInput);
        int professorId = Integer.valueOf(professorIdInput);
        timeStartInput += ":00"; //add seconds value for proper processing
        timeStopInput += ":00";
        Time timeStart = Time.valueOf(timeStartInput);
        Time timeStop = Time.valueOf(timeStopInput);
        int weekday = Integer.valueOf(weekdayInput);
        int studentGrup = Integer.valueOf(studentGrupInput);

        if (subjectId == 0 || professorId == 0 || weekday == -1) {
            model.addAttribute("addScheduleFormError", "Select a valid element for each box.");
            return "addSchedule";
        }

        int timeDifference = timeStop.compareTo(timeStart);
        if (timeDifference <= 0) {
            model.addAttribute("addScheduleFormError", "Selected time is not valid.");
            return "addSchedule";
        }

        try {
            Optional<Subject> formSubject = subjectRepo.findById(subjectId);
            if (formSubject.isPresent()) {
                List<Schedule> scheduleList = new ArrayList<>();
                try {
                    int scheduleFound = 0;
                    scheduleRepo.findByProfessorId(professorId).forEach(scheduleList::add);
                    for (Schedule schedule : scheduleList) { //verify professor schedule
                        if (schedule.getWeekday() == weekday) { //professor has a schedule for selected day
                            int timeStartDifference = timeStart.compareTo(schedule.getTimeStart());

                            if (timeStartDifference < 0) { //form timeStart exists before schedule timeStart
                                int otherTimeDifference = timeStop.compareTo(schedule.getTimeStart());
                                if (otherTimeDifference >= 0) { //form timeStop extends over found schedule
                                    scheduleFound = 1;
                                    break;
                                }
                            } else {
                                int otherTimeDifference = timeStart.compareTo(schedule.getTimeStop());
                                if (otherTimeDifference <= 0) { //form schedule starts after existing schedule
                                    scheduleFound = 1;
                                    break;
                                }
                            }
                        }
                    }

                    /*
                    scheduleList = new ArrayList<>();
                    try {
                        scheduleRepo.findByStudentGrup(studentGrup).forEach(scheduleList::add);
                        for(Schedule schedule : scheduleList) { //verify schedule for selected group of specialization matching with selected subject
                            if(schedule.getWeekday() == weekday) { //professor has a schedule for selected day
                                Optional<Subject> subject = subjectRepo.findById(schedule.getSubjectId());
                                if(subject.isPresent()) {
                                    if(subject.get().getSpec() == formSubject.get().getSpec()) {
                                        int timeStartDifference = timeStart.compareTo(schedule.getTimeStart());

                                        if(timeStartDifference < 0) { //form timeStart exists before schedule timeStart
                                            int otherTimeDifference = timeStop.compareTo(schedule.getTimeStart());
                                            if(otherTimeDifference >= 0) { //form timeStop extends over found schedule
                                                scheduleFound = 1;
                                                break;
                                            }
                                        }
                                        else {
                                            int otherTimeDifference = timeStart.compareTo(schedule.getTimeStop());
                                            if(otherTimeDifference <= 0) { //form schedule starts after existing schedule
                                                scheduleFound = 1;
                                                break;
                                            }
                                        }
                                    }
                                }
                                else {
                                    model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                                    return "addSchedule";
                                }
                            }
                        }
                    }
                    catch(Exception ex) {
                        model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                        return "addSchedule";
                    }
                    */

                    if (scheduleFound == 0) {
                        Schedule schedule = new Schedule();
                        schedule.setSubjectId(subjectId);
                        schedule.setProfessorId(professorId);
                        schedule.setTimeStart(timeStart);
                        schedule.setTimeStop(timeStop);
                        schedule.setWeekday(weekday);
                        schedule.setStudentGrup(studentGrup);
                        schedule.setRoom(room);
                        try {
                            scheduleRepo.save(schedule);
                            return "redirect:/index"; //redirect to list of users when said page is implemented
                        }
                        catch (Exception ex) {
                            model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                            return "addSchedule";
                        }
                    }

                    model.addAttribute("addScheduleFormError", "Selected professor already has a schedule appointed in this time frame.");
                    return "addSchedule";
                } catch (Exception ex) {
                    model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                    return "addSchedule";
                }
            } else {
                model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                return "addSchedule";
            }
        } catch (Exception ex) {
            model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
            return "addSchedule";
        }
    }

    /*
        Add Attendance
    */
    @GetMapping("/addAttendance")
    public String getAddAttendancePage(Model model, HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        List<Subject> subjectList = new ArrayList<>();
        try {
            subjectRepo.findAll().forEach(subjectList::add);
            model.addAttribute("subjectList", subjectList);
            return "addAttendance";
        } catch (Exception ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/addAttendance")
    public String addAttendance(@ModelAttribute("addAttendanceForm") AddAttendanceForm addAttendanceForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String cnp = addAttendanceForm.getCnp();
        String subjectInput = addAttendanceForm.getSubject();
        String dateInput = addAttendanceForm.getDate();
        String timeInput = addAttendanceForm.getTime();

        if (cnp.length() == 0 || subjectInput.length() == 0 || dateInput.length() == 0 || timeInput.length() == 0) {
            model.addAttribute("addAttendanceFormError", "Please fill in all the boxes.");
            return "addAttendance";
        }

        int subject = Integer.valueOf(subjectInput);
        Date date = Date.valueOf(dateInput);
        int weekday = LocalDate.parse(dateInput).getDayOfWeek().getValue();
        timeInput += ":00"; //add seconds value for proper processing
        Time time = Time.valueOf(timeInput);

        if (cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("addAttendanceFormError", "CNP is too short.");
            return "addAttendance";
        }

        if(subject == 0) {
            model.addAttribute("addAttendanceFormError", "Select a valid element for each box.");
            return "addAttendance";
        }

        try {
            Optional<User> user = userRepo.findByCnp(cnp);
            if(user.isPresent()) {
                try {
                    Optional<Student> student = studentRepo.findByUserId(user.get().getId());
                    if(student.isPresent()) {
                        try {
                            int scheduleFound = 0;
                            List<Schedule> scheduleList = new ArrayList<>();
                            scheduleRepo.findBySubjectId(subject).forEach(scheduleList::add);
                            for(Schedule schedule : scheduleList) {
                                if(schedule.getWeekday() == weekday) {
                                    if(schedule.getStudentGrup() == 0 || schedule.getStudentGrup() == student.get().getGrup()) {
                                        int timeStartDifference = time.compareTo(schedule.getTimeStart()); //timeStartDifference > 0 - time comes after timeStart; < 0 - time comes before timeStart
                                        int timeStopDifference = time.compareTo(schedule.getTimeStop());

                                        if(timeStartDifference > 0 && timeStopDifference < 0) {
                                            scheduleFound = schedule.getId();
                                            break;
                                        }
                                    }
                                }
                            }

                            if(scheduleFound != 0) {
                                Attendance attendance = new Attendance();
                                attendance.setStudentId(student.get().getUserId());
                                attendance.setSubjectId(subject);
                                attendance.setScheduleId(scheduleFound);
                                attendance.setScanDate(date);
                                attendance.setScanTime(time);
                                try {
                                    attendanceRepo.save(attendance);
                                    return "redirect:/index"; //redirect to list of users when said page is implemented
                                }
                                catch (Exception ex) {
                                    model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                                    return "addSchedule";
                                }
                            }

                            model.addAttribute("addScheduleFormError", "Date and time provided are inappropriate for this subject.");
                            return "addAttendance";
                        }
                        catch(Exception ex) {
                            model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                            return "addAttendance";
                        }
                    }
                    else {
                        model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                        return "addAttendance";
                    }
                }
                catch(Exception ex) {
                    model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
                    return "addAttendance";
                }
            }
            else {
                model.addAttribute("addScheduleFormError", "User does not exist.");
                return "addAttendance";
            }
        }
        catch(Exception ex) {
            model.addAttribute("addScheduleFormError", "An error has occurred. Please try again.");
            return "addAttendance";
        }
    }

    /*
        Change Password
    */
    @GetMapping("/changePassword")
    public String getChangePasswordPage(HttpSession session) {
        if (session.getAttribute("status") == null) {
            return "redirect:/index";
        }
        return "changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm, Model model, HttpSession session) {

        if (session.getAttribute("status") == null) {
            return "redirect:/login";
        }

        String cnp = changePasswordForm.getCnp();
        String password = changePasswordForm.getPassword();

        if (cnp.length() == 0 || password.length() == 0) {
            model.addAttribute("changePasswordFormError", "Please fill in all the boxes.");
            return "changePassword";
        }

        if (cnp.length() != Constants.CNP_LENGTH) {
            model.addAttribute("changePasswordFormError", "CNP is too short.");
            return "changePassword";
        }

        try {
            Optional<User> user = userRepo.findByCnp(cnp);
            if (user.isPresent()) {

                try {
                    user.get().setPassword(Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString());
                    userRepo.save(user.get());
                    return "redirect:/index"; //redirect to list of users when said page is implemented
                }
                catch(Exception ex) {
                    model.addAttribute("changePasswordFormError", "An error has occurred. Please try again.");
                    return "changePassword";
                }
            }
            else {
                model.addAttribute("changePasswordFormError", "User with CNP does not exist.");
                return "changePassword";
            }
        }
        catch (Exception ex) {
            model.addAttribute("changePasswordFormError", "An error has occurred. Please try again.");
            return "changePassword";
        }
    }
}