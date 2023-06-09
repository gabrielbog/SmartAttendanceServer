package com.gabrielbog.attendanceserver.controllers;

import com.gabrielbog.attendanceserver.views.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    //Variables
    @Autowired
    private Environment env;

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello World!");
        return "helloworld";
    }

    /*
        Log In Page
    */
    @GetMapping("/login")
    public String getLogInForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginForm loginForm, Model model) {

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();

        if(username.equals(env.getProperty("spring.datasource.username")) && password.equals(env.getProperty("spring.datasource.password"))) {
            return "index";
        }
        model.addAttribute("invalidCredentials", true);
        return "login";
    }
}