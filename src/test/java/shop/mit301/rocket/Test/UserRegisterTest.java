package shop.mit301.rocket.Test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import shop.mit301.rocket.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class UserRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1. 정상 회원가입 테스트
     */
    @Test
    public void registerUser_Success() throws Exception {
        String json = """
            {
                "userId": "testuser3",
                "pw": "12345",
                "name": "성춘향",
                "email": "test3@test.com",
                "tel": "010-5555-6666",
                "permission": 1
            }
        """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andDo(print());

        // DB에 실제로 들어갔는지 확인
        boolean exists = userRepository.existsById("testuser3");
        if (exists) {
            System.out.println("✅ DB 저장 확인: testuser3 존재함");
        } else {
            System.out.println("❌ DB 저장 확인 실패: testuser3 없음");
        }
    }

    /**
     * 2. 중복 체크 테스트 (ID 중복)
     */
    @Test
    public void registerUser_DuplicateId() throws Exception {
        // 먼저 DB에 동일한 userId 넣기
        userRepository.save(
                shop.mit301.rocket.domain.User.builder()
                        .user_id("testuser4")
                        .pw("1234")
                        .name("임꺽정")
                        .email("test4@test.com")
                        .tel("010-7777-8888")
                        .permission((byte)1)
                        .build()
        );

        String json = """
            {
                "userId": "testuser4",
                "pw": "abcd",
                "name": "홍길동",
                "email": "duplicate@test.com",
                "tel": "010-9999-0000",
                "permission": 1
            }
        """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.errorType").value("duplicateId"))
                .andDo(print());

        System.out.println("✅ 중복 체크 테스트 완료: duplicateId 검증");
    }
}
