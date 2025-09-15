package shop.mit301.rocket.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {
    private String userId;
    private String pw;
    private String name;
    private String tel;
    private int permission; // 사용자 0, 관리자 1
    private String email;

}
