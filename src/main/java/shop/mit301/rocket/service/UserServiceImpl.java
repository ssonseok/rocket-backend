package shop.mit301.rocket.service;

import shop.mit301.rocket.domain.User;
import shop.mit301.rocket.dto.UserRegisterDTO;
import shop.mit301.rocket.repository.UserRepository;
import shop.mit301.rocket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public String registerUser(UserRegisterDTO dto) {
        // ID 중복
        if(userRepository.existsById(dto.getUserId())) {
            return "duplicateId";
        }
        // 이메일 중복
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return "duplicateEmail";
        }
        // 전화번호 중복
        if(userRepository.findByTel(dto.getTel()).isPresent()) {
            return "duplicateTel";
        }

        // 중복 없으면 저장
        User user = modelMapper.map(dto, User.class);
        user.setUser_id(dto.getUserId()); // 반드시 ID 세팅
        user.setPermission((byte) dto.getPermission());
        userRepository.save(user);

        return "success";
    }
}

