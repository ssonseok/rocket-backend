package shop.mit301.rocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @GetMapping("/login.html")
    public void login() {}

    @GetMapping("/charts.html")
    public void charts() {}

    @GetMapping("/layout-sidenav-light.html")
    public void light() {}

    @GetMapping("/layout-static.html")
    public void layout() {}

    @GetMapping("/password.html")
    public void password() {}

    @GetMapping("/register.html")
    public void register() {}

    @GetMapping("/tables.html")
    public void tables() {}

    @GetMapping("/index.html")
    public void index() {}

    @GetMapping("/findid.html")
    public void findid() {}
}
