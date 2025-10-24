package shop.mit301.rocket.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {

    // JWT 파싱 로직에 맞춰 수정
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey("your-256-bit-secret-your-256-bit-secret".getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
        return claims.getSubject();  // String 그대로 반환
    }
}