package com.example.DevHub.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // This is a regular Controller, not a RestController
public class ViewController {

    @GetMapping({"/", "/login"}) // Changed to map / and /login to the login view directly.
    // Spring Security's loginPage("/login") setting will handle the /login path itself
    // for authentication flow purposes, but this controller ensures the view is served.
    public String showLoginPage() {
        return "login"; // Refers to src/main/resources/templates/login.html
    }

    // Add mappings for other HTML pages that users might directly navigate to
    // Ensure these names match your .html files in src/main/resources/templates/
    @GetMapping("/index.html")
    public String showIndexPage() {
        return "index";
    }

    @GetMapping("/blogs.html")
    public String showBlogsPage() {
        return "blogs";
    }

    @GetMapping("/project.html")
    public String showProjectPage() {
        return "project";
    }

    @GetMapping("/comments.html")
    public String showCommentsPage() {
        return "comments";
    }

    @GetMapping("/account.html")
    public String showAccountPage() {
        return "account";
    }

    @GetMapping("/adminpage.html")
    public String showAdminPage() {
        return "adminpage";
    }
}