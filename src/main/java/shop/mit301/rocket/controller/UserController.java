package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 등록", description = "회원 정보 등록 및 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "실패", content = @Content(schema = @Schema(implementation = Map.class)))
    })



    //회원등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody UserRegisterDTO dto) {
        String result = userService.registerUser(dto);

        Map<String, String> response = new HashMap<>();
        if("success".equals(result)) {
            response.put("status", "success");
            response.put("errorType", "none");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "fail");
            response.put("errorType", result);
            return ResponseEntity.badRequest().body(response);
        }

    }
}
