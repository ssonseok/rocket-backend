package shop.mit301.rocket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import shop.mit301.rocket.dto.Admin_UserDeleteDTO;
import shop.mit301.rocket.dto.Admin_UserListDTO;
import shop.mit301.rocket.dto.Admin_UserModifyDTO;
import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.service.Admin_UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class Admin_UserController {

    private final Admin_UserService adminUserService;


    @Operation(summary = "회원 등록", description = "회원 정보 등록 및 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "실패", content = @Content(schema = @Schema(implementation = Map.class)))
    })

    //회원등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody UserRegisterDTO dto) {
        String result = adminUserService.registerUser(dto);

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

    @Operation(summary = "회원 목록 조회", description = "모든 회원 정보를 리스트로 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Admin_UserListDTO.class)))),
            @ApiResponse(responseCode = "400", description = "실패")
    })
    @GetMapping("/list")
    public ResponseEntity<List<Admin_UserListDTO>> getUserList() {
        List<Admin_UserListDTO> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @Operation(summary = "회원 정보 조회", description = "수정 화면에서 특정 유저 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<Admin_UserModifyDTO> getUser(@PathVariable String userId) {
        Admin_UserModifyDTO dto = adminUserService.getUserById(userId);
        return ResponseEntity.ok(dto);
    }

    // 2. 특정 유저 정보 수정 (PUT)
    @Operation(summary = "회원 정보 수정", description = "회원 정보 수정 및 이메일/전화번호 중복 체크")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "수정 실패")
    })
    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyUser(@RequestBody Admin_UserModifyDTO dto) {
        String result = adminUserService.modifyUser(dto);

        Map<String, String> response = new HashMap<>();
        if ("success".equals(result)) {
            response.put("status", "success");
            response.put("errorType", "none");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "fail");
            response.put("errorType", result);
            return ResponseEntity.badRequest().body(response);
        }
    }

    //회원삭
    @Operation(summary = "회원 삭제", description = "특정 회원을 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestBody Admin_UserDeleteDTO dto) {
        adminUserService.deleteUser(dto);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("errorType", "none");

        return ResponseEntity.ok(response);
    }


}
