package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.PasswordResetToken;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserDTO;
import shop.mit301.rocket.jwt.JwtUtil;
import shop.mit301.rocket.repository.Admin_UserRepository;
import shop.mit301.rocket.repository.PasswordResetTokenRepository;
import shop.mit301.rocket.service.UserServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final Admin_UserRepository userRepository;
    private final UserServiceImpl userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/findId")
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

    @PostMapping("/changePw")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String id = request.get("id");

        Map<String, String> response = new HashMap<>();

        // username(아이디) + email이 정확히 일치하는 사용자만 조회
        Optional<User> optionalUser = userRepository.findByUseridAndEmail(id, email);

        if (optionalUser.isEmpty()) {
            response.put("message", "일치하는 사용자 정보가 없습니다.");
            return ResponseEntity.status(404).body(response);
        }

        User user = optionalUser.get();

        // 이메일 전송 서비스 호출
        userService.sendPasswordResetLink(user.getEmail());

        response.put("message", "이메일로 비밀번호 변경 링크가 전송되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/changePwLink")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> request) {

        String newPw = request.get("newPw");
        Map<String, String> response = new HashMap<>();

        if (token == null || newPw == null) {
            response.put("status", "fail");
            response.put("message", "잘못된 요청입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<PasswordResetToken> prtOptional = tokenRepository.findByToken(token);

        if (prtOptional.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "유효하지 않은 토큰입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        PasswordResetToken prt = prtOptional.get();

        if (prt.isExpired()) {
            response.put("status", "fail");
            response.put("message", "토큰이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User user = prt.getUser();
        User updatedUser = user.toBuilder()
                .pw(newPw)
                .build();
        userRepository.save(updatedUser);

        tokenRepository.delete(prt);

        response.put("status", "success");
        response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO request) {
        String userId = request.getUserid();
        String password = request.getPw();

        Optional<User> optionalUser = userRepository.findByUserid(userId);

        Map<String, String> response = new HashMap<>();

        if (optionalUser.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = optionalUser.get();

        if (!user.getPw().equals(password)) {
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = jwtUtil.generateToken(user.getUserid());

        response.put("status", "success");
        response.put("token", token);
        response.put("permission", String.valueOf(user.getPermission()));

        return ResponseEntity.ok(response);
    }
}