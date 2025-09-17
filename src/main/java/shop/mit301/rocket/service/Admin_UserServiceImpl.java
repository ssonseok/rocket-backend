package shop.mit301.rocket.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import shop.mit301.rocket.domain.User;
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
                .permission((byte) dto.getPermission())
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
        User user = adminUserRepository.findById(userId).get();

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
        User user = adminUserRepository.findById(dto.getUserId()).get();

        // 2. 이메일 중복 체크 (자기 자신 제외)
        if(adminUserRepository.findByEmail(dto.getEmail())
                .filter(u -> !u.getUserid().equals(dto.getUserId()))
                .isPresent()) {
            return "duplicateEmail";
        }

        // 3. 전화번호 중복 체크 (자기 자신 제외)
        if(adminUserRepository.findByTel(dto.getTel())
                .filter(u -> !u.getUserid().equals(dto.getUserId()))
                .isPresent()) {
            return "duplicateTel";
        }

        modelMapper.map(dto, user);
        adminUserRepository.save(user);

        return "success";
    }
}

