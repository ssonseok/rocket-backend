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
    public User registerUser(UserRegisterDTO dto) throws Exception {

        // ID 중복 체크
        if(userRepository.existsById(dto.getId())) {
            throw new Exception("이미 존재하는 ID입니다.");
        }

        // 이메일 중복 체크
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        // 전화번호 중복 체크
        if(userRepository.findByTel(dto.getTel()).isPresent()) {
            throw new Exception("이미 존재하는 전화번호입니다.");
        }

        // DTO → Entity 변환
        User user = modelMapper.map(dto, User.class);

        // 비밀번호는 그대로 저장 (나중에 Security 적용 예정)
        return userRepository.save(user);
    }
}