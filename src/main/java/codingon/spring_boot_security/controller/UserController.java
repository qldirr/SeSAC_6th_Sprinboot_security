package codingon.spring_boot_security.controller;

import codingon.spring_boot_security.dto.ResponseDTO;
import codingon.spring_boot_security.dto.UserDTO;
import codingon.spring_boot_security.entity.UserEntity;
import codingon.spring_boot_security.security.TokenProvider;
import codingon.spring_boot_security.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;

    // [after] jwt token 적용 후
    @Autowired
    private TokenProvider tokenProvider;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            // 요청 본문을 이용해 저장할 사용자 만들기
            UserEntity user = UserEntity.builder()
                    .email(userDTO.getEmail())
                    .username(userDTO.getUsername())
                    .password(userDTO.getPassword())
                    .build();

            // 서비스를 이용해 레포지터리에 사용자 저장
            UserEntity registerdUser = userService.create(user);
            UserDTO responsedUserDTO = UserDTO.builder()
                    .email(registerdUser.getEmail())
                    .id(registerdUser.getId())
                    .username(registerdUser.getUsername())
                    .build();

            // 사용자 정보는 항상 1개이므로 리스트로 만들어 응답하는 ResponseDTO를 이용하지 않고 그냥 UserDTO 리턴
            return ResponseEntity.ok().body(responsedUserDTO);

        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(responseDTO);

        }
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
        UserEntity user = userService.getByCredentials(
                userDTO.getEmail(),
                userDTO.getPassword()
        );

        if (user != null) {
            // [after] jwt token 적용 후
            final String token = tokenProvider.create(user);   // jwt token 생성
            final UserDTO responsedUserDTO = userDTO.builder()
                    .email(user.getEmail())
                    .id(user.getId())
                    .token(token)    // jwt 토큰을 token 필드에 저장
                    .build();

            return ResponseEntity.ok().body(responsedUserDTO);
            // 리턴값에 token 이 들어감
//            {
//                "token": "eyJhbG...",
//                "email": "aaa@test.com",
//                 "username": null,
//                 "password": null,
//                 "id": 2
//            }


            // [before] jwt token 적용 전
//            final UserDTO responsedUserDTO = userDTO.builder()
//                    .email(user.getEmail())
//                    .id(user.getId())
//                    .build();
//
//            return ResponseEntity.ok().body(responsedUserDTO);
        } else {
            // 로그인 검사 실패! (해당 유저가 존재하지 않았으므로)
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed.")
                    .build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }
}
