package shop.mit301.rocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.mit301.rocket.domain.PasswordResetToken;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetTokenDTO {

    private Long id;
    private String token;
    private String userId;
    private LocalDateTime expiryDate;

    public static PasswordResetTokenDTO fromEntity(PasswordResetToken tokenEntity) {
        return PasswordResetTokenDTO.builder()
                .id(tokenEntity.getId())
                .token(tokenEntity.getToken())
                .userId(tokenEntity.getUser().getUserid())
                .expiryDate(tokenEntity.getExpiryDate())
                .build();
    }
}
