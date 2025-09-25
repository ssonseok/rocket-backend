package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.Admin_UserDeleteDTO;
import shop.mit301.rocket.dto.Admin_UserListDTO;
import shop.mit301.rocket.dto.Admin_UserModifyDTO;
import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.repository.Admin_UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Admin_UserServiceImpl implements Admin_UserService {

    private final Admin_UserRepository adminUserRepository;
    private final ModelMapper modelMapper;

    //회원등록
    @Override
    public String registerUser(UserRegisterDTO dto) {

        // ID 중복 체크
        if(adminUserRepository.existsById(dto.getUserId())) {
            return "duplicateId";
        }
        // 이메일 중복 체크
        if(adminUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "duplicateEmail";
        }
        // 전화번호 중복 체크
        if(adminUserRepository.findByTel(dto.getTel()).isPresent()) {
            return "duplicateTel";
        }

        // Builder로 엔티티 생성
        User user = User.builder()
                .userid(dto.getUserId())
                .pw(dto.getPw())
                .name(dto.getName())
                .email(dto.getEmail())
                .tel(dto.getTel())
//                .permission((byte) dto.getPermission())
                .permission((byte) 0) //일반사용자로 고정
                .build();

        adminUserRepository.save(user);

        return "success";
    }

    //회원목록
    @Override
    public List<Admin_UserListDTO> getAllUsers() {
        List<User> users = adminUserRepository.findAll();


        return users.stream()
                .map(user -> Admin_UserListDTO.builder()
                        .userId(user.getUserid())
                        .name(user.getName())
                        .tel(user.getTel())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    //회원수정
    //1.수정화면에 기존 정보 조회
    @Override
    public Admin_UserModifyDTO getUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId가 null이거나 비어있습니다.");
        }

        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        return Admin_UserModifyDTO.builder()
                .userId(user.getUserid())
                .name(user.getName())
                .email(user.getEmail())
                .tel(user.getTel())
                .build();
    }

    //2.받은 정보 수정 처리
    @Override
    public String modifyUser(Admin_UserModifyDTO dto) {
        if (dto.getUserId() == null || dto.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId가 null입니다. 요청 확인 필요");
        }

        User existingUser = adminUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        // 이메일/전화번호 중복 체크
        if(adminUserRepository.findByEmail(dto.getEmail())
                .filter(u -> !u.getUserid().equals(dto.getUserId()))
                .isPresent()) return "duplicateEmail";

        if(adminUserRepository.findByTel(dto.getTel())
                .filter(u -> !u.getUserid().equals(dto.getUserId()))
                .isPresent()) return "duplicateTel";

        // Builder로 기존 객체 복사하면서 변경
        User updatedUser = existingUser.toBuilder()
                .name(dto.getName())
                .email(dto.getEmail())
                .tel(dto.getTel())
                .build();

        adminUserRepository.save(updatedUser);

        return "success";
    }

    @Override
    public String deleteUser(Admin_UserDeleteDTO dto) {
        if (dto.getUserId() == null || dto.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId가 null입니다. 요청 확인 필요");
        }

        User user = adminUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
        adminUserRepository.delete(user);
        return "success";
    }
}

