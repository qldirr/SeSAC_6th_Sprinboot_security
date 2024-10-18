package codingon.spring_boot_security.controller;

import codingon.spring_boot_security.dto.ResponseDTO;
import codingon.spring_boot_security.dto.TodoDTO;
import codingon.spring_boot_security.entity.TodoEntity;
import codingon.spring_boot_security.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/todo")
public class TodoController {
    @Autowired
    TodoService todoService;

    // ResponseEntity
    // - 해당 객체를 이용해 상태코드, 응답 본문 등을 설정해 클라이언트 '응답'
    // 메서드
    // - ok() : success
    // - headers() : 응답 헤더 설정
    // - body() : 응답 본문 설정

    // create todo
    @PostMapping
    public ResponseEntity<?> createTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto) {
        // @AuthenticationPrincipal
        // - 현재 인증된 사용자 정보에 접근할 수 있게 함
        // - Spring Security 는 security context 에서 현재 인증된 사용자의 principal 을 가져옴
        // 우리 코드) jwtAuthenticationFilter 클래스에서 userId 를 바탕으로 인증 객체 생성했음
        try {
            // 임시 유저 하드코딩
//            String temporaryUserId = "temporary-user";

            // (1) dto to entity
            TodoEntity entity = TodoDTO.toEntity(dto);

            // (2) 생성 당시에는 id null로 초기화
            entity.setId(null);

            // (3) 임시 유저 아이디 설정
//            entity.setUserId(temporaryUserId);
            entity.setUserId(userId);

            // (4) 서비스 계층을 이용해 todo 엔티티 생성
            List<TodoEntity> entities = todoService.create(entity);

            // (5) 리턴된 엔티티 리스트(추가된 행 하나)를 TodoDTO 변환
            List<TodoDTO> dtos = new ArrayList<>();
            for (TodoEntity tEntity : entities) {
                TodoDTO tDTO = new TodoDTO(tEntity);
                dtos.add(tDTO);
            }

            // (6) 변환된 todoDTO 리스트를 이용해 ResponseDTO 초기화
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();

            // (7) ResponseDTO 를 클라이언트에게 응답
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            // (8) 예외 발생 시 ResponseDTO 의 data 필드 대신, error 필드에 메세지 넣어서 리턴
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    // read todo
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(@AuthenticationPrincipal String userId) {
        // 임시 유저 하드코딩
//        String temporaryUserId = "temporary-user";

        // (1) 서비스 계층의 retrieve() 메서드를 사용해 '해당 유저의 투두 목록'을 가져오기
        List<TodoEntity> todoEntities = todoService.retrieve(userId);

        // (2) 리턴된 엔티티리스트를 todoDTO 리스트로 변환
        List<TodoDTO> todoDTOS = new ArrayList<>();
        for (TodoEntity entity : todoEntities) {
            TodoDTO dto = new TodoDTO(entity);
            todoDTOS.add(dto);
        }

        // (3) 변환된 TodoDTO 리스트를 이용해 ResponseDTO 초기화
        ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(todoDTOS).build();

        // (4) ResponseDTO 리턴
        return ResponseEntity.ok().body(response);
    }

    // update todo
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal String userId, TodoDTO dto, @PathVariable Long id) {
        try {

            TodoEntity entity = TodoDTO.toEntity(dto);

            Optional<TodoEntity> existingTodo = todoService.getTodo(id);

            // 작성자가 작성한 todo 만 수정 가능하도록
            if (!existingTodo.get().getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(ResponseDTO.<TodoDTO>builder().error("You are not allowed to update this Todo.").build());
            }

            List<TodoEntity> todoEntity = todoService.update(entity);

            List<TodoDTO> todoDTOS = new ArrayList<>();
            for (TodoEntity todo : todoEntity) {
                TodoDTO todoDTO = new TodoDTO(todo);
                todoDTOS.add(todoDTO);
            }
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(todoDTOS).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}
