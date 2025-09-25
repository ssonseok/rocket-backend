package shop.mit301.rocket.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.mit301.rocket.domain.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String userid;
    private String pw;
    private String name;
    private String email;
    private String tel;
    private byte permission;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .userid(user.getUserid())
                .pw(user.getPw())
                .name(user.getName())
                .email(user.getEmail())
                .tel(user.getTel())
                .permission(user.getPermission())
                .build();
    }
}
