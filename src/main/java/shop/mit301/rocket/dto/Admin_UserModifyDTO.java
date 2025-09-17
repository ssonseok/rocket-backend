package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin_UserModifyDTO {
    private String userId; // 수정할 때 대상 식별자 (화면에서는 수정 불가, readonly)
    private String name;
    private String tel;
    private String email;
}
