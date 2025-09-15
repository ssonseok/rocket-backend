package shop.mit301.rocket.controller;

import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody UserRegisterDTO dto) {
        String result = userService.registerUser(dto);

        Map<String, String> response = new HashMap<>();
        if("success".equals(result)) {
            response.put("status", "success");
        } else {
            response.put("status", "fail");
            response.put("errorType", result); // duplicateId / duplicateEmail / duplicateTel
        }

        return ResponseEntity.ok(response);
    }
}
