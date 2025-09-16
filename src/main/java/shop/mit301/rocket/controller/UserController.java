package shop.mit301.rocket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.mit301.rocket.domain.PasswordResetToken;
import shop.mit301.rocket.dto.UserDTO;
import shop.mit301.rocket.repository.PasswordResetTokenRepository;
import shop.mit301.rocket.service.UserServiceImpl;
import shop.mit301.rocket.repository.UserRepository;
import shop.mit301.rocket.domain.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final PasswordResetTokenRepository tokenRepository;

    @PostMapping("/findid")
    public ResponseEntity<Map<String, String>> findIdByEmail(@RequestBody UserDTO requestDto) {
        String email = requestDto.getEmail();
        Map<String, String> response = new HashMap<>();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "해당 이메일로 등록된 사용자가 없습니다.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();
        UserDTO userDTO = UserDTO.fromEntity(user);

        userService.sendUserId(userDTO);

        response.put("status", "success");
        response.put("message", "아이디가 이메일로 전송되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/changePw")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody UserDTO requestDto) {
        String email = requestDto.getEmail();
        String userId = requestDto.getUserid();

        Map<String, String> response = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByUseridAndEmail(userId, email);

        if (optionalUser.isEmpty()) {
            response.put("message", "일치하는 사용자 정보가 없습니다.");
            return ResponseEntity.status(404).body(response);
        }

        User user = optionalUser.get();
        UserDTO userDTO = UserDTO.fromEntity(user);
        userService.sendPasswordResetLink(userDTO);

        response.put("message", "이메일로 비밀번호 변경 링크가 전송되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO requestDto) {
        String userId = requestDto.getUserid();
        String password = requestDto.getPw();

        Map<String, String> response = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByUseridAndPw(userId, password);

        if (optionalUser.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(401).body(response);
        }

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/changePwLink")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        Map<String, String> response = new HashMap<>();

        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            response.put("status", "fail");
            response.put("message", "유효하지 않은 토큰입니다.");
            return ResponseEntity.status(400).body(response);
        }

        PasswordResetToken resetToken = optionalToken.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            response.put("status", "fail");
            response.put("message", "토큰이 만료되었습니다.");
            return ResponseEntity.status(400).body(response);
        }

        User user = resetToken.getUser();
        user.setPw(newPassword); // 비밀번호 암호화 안한다고 하셨으니 단순 저장
        userRepository.save(user);

        // 토큰은 1회용으로 삭제
        tokenRepository.delete(resetToken);

        response.put("status", "success");
        response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }
}