package codingon.spring_boot_security.service;

import codingon.spring_boot_security.entity.UserEntity;
import codingon.spring_boot_security.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // 회원가입(사용자 생성)
    public UserEntity create(final UserEntity userEntity) {
        // 유효성 검사1) userEntity 혹은 email 이 null 인 경우 예외 던짐
        if (userEntity == null || userEntity.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }

        final String email = userEntity.getEmail();

        // 유효성 검사2) 이메일이 이미 조재하는 경우 예외를 던짐 (email 필드는 유니크해야 하므로)
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists {}", email);
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(userEntity); // UserEntity 를 DB 에 저장
    }

    // 인증: 이메일과 비밀번호로 사용자 조회
//    public UserEntity getByCredentials(final String email, final String password) {
//        // DB 에서 해당 email, password 가 일치하는 유저가 있는지를 조회
//        return userRepository.findByEmailAndPassword(email, password);
//    }

    // [after] 패스워드 암호화 적용 후
    public UserEntity getByCredentials(final String email, final String password, final PasswordEncoder encoder){
        final UserEntity originalUser = userRepository.findByEmail(email);

        if (originalUser != null && encoder.matches(password, originalUser.getPassword())){
            // password - 클라이언트가 입력한 비번
            // originalUser.getPassword() - DB 에 저장된 비번
            return originalUser;
        }

        return null;
    }
}
