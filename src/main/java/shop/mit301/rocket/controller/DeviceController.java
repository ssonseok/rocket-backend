package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.repository.UserRepository;
import shop.mit301.rocket.service.UserServiceImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final UserRepository userRepository;
    private final UserServiceImpl userService;  // 주입 추가

    @PostMapping("/findid")
    public ResponseEntity<Map<String, String>> findIdByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();

        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "해당 이메일로 등록된 사용자가 없습니다.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();

        userService.sendUserId(user.getEmail());  // 인스턴스 메서드 호출

        response.put("status", "success");
        response.put("message", "아이디가 이메일로 전송되었습니다.");
        return ResponseEntity.ok(response);
    }
}
