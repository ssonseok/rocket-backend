package shop.mit301.rocket.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {
    private String id;
    private String pw;
    private String name;
    private String tel;
    private boolean permission; // 사용자 0, 관리자 1
    private String email;
}
