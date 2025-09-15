package shop.mit301.rocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @GetMapping("/401.html")
    public void one() {}

    @GetMapping("/404.html")
    public void four() {}

    @GetMapping("500.html")
    public void five() {}
}
