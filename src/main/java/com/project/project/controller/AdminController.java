package com.project.project.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.dto.Admin_user.AdminDTO;
import com.project.project.dto.Admin_user.UserDTO;
import com.project.project.dto.Admin_user.BoardDTO;
import com.project.project.dto.userServiceDto.LoginRequestDTO;
import com.project.project.entity.Admin;
import com.project.project.entity.BoardEntity;
import com.project.project.entity.User;
import com.project.project.repository.AdminRepository;
import com.project.project.repository.UserRepository;
import com.project.project.service.AdminService;
import com.project.project.service.BoardService;
import com.project.project.service.UserService;
import com.project.project.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final BoardService boardService;
    private final UserService userService;
    private final AdminService adminService;
    private final JWTUtil jwtUtil;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;


    //member-list
@GetMapping("/member")
public ResponseEntity<List<UserDTO>> admin_member() {
    // 모든 User 엔티티를 가져옴
    List<User> users = userService.findAll();

    // User 엔티티를 UserDTO로 변환
    List<UserDTO> userDTOS = users.stream()
            .map(user -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUsername(user.getUsername());
                userDTO.setPassword(user.getPassword());
                userDTO.setRole(user.getRole());
                userDTO.setStatus(user.getStatus());
                userDTO.setEmail(user.getEmail());
                //프로필 모달창을 위해..
                userDTO.setProfilePictureUrl(user.getProfilePictureUrl());
                userDTO.setBio(user.getBio());
                userDTO.setAdress(user.getAddress());
                //userDTO.setUserInterests(user.getUserInterests());
                //userDTO.setUserSkills(user.getUserSkills());

                userDTO.setOrganization(user.getOrganization());
                userDTO.setPostcode(user.getPostcode());
                userDTO.setPosition(user.getPosition());
                // 필요한 경우 다른 필드도 설정
                return userDTO;
            })
            .collect(Collectors.toList());

    return ResponseEntity.ok().body(userDTOS);
}

    @GetMapping("/board")
    public ResponseEntity<List<BoardDTO>> admin_board() {
        List<BoardEntity> boardList = boardService.findAllBoard();

        List<BoardDTO> boardDTOS = boardList.stream()
                .map(board -> {
                    BoardDTO boardDTO = new BoardDTO();
                    boardDTO.setId(board.getId());
                    boardDTO.setBoardContent(board.getBoardContents());
                    boardDTO.setBoardTitle(board.getBoardTitle());
                    boardDTO.setBoardPass(board.getBoardPass());
                    boardDTO.setBoardWriter(board.getBoardWriter());
                    return boardDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(boardDTOS); // 변환된 DTO 리스트를 반환
    }
    @GetMapping("/admin")
    public ResponseEntity<List<AdminDTO>> admin_admin() {
        List<Admin> admins = adminService.findAll();
        //role 엔티티를 새로 만들지 말지 고민중
        List<AdminDTO> adminDTOS = admins.stream()
                .map(admin ->{
                    AdminDTO adminDTO = new AdminDTO();
                    adminDTO.setId(admin.getId());
                    adminDTO.setUsername(admin.getUsername());
                    adminDTO.setPassword(admin.getPassword());
                    adminDTO.setRole(admin.getRole());
                    adminDTO.setStatus(admin.getStatus());
                    return adminDTO;
                }) //collectors대신 to list 씀
                .toList();

        return ResponseEntity.ok().body(adminDTOS);
    }

    @PutMapping("/member/{userId}")
    public ResponseEntity<Object> updateRole(@PathVariable Long userId, @RequestBody String roleJson) {
        // 역할 출력 확인용 로그
        System.out.println("userId: " + userId);
        System.out.println("roleJson: " + roleJson);
        String upper = roleJson.toUpperCase();

        System.out.println(upper);

        // 앞뒤 따옴표 제거
        if (upper.startsWith("\"") && upper.endsWith("\"")) {
            upper = upper.substring(1, upper.length() - 1);
        }

        System.out.println("roleJson: " + roleJson);

        ObjectMapper objectMapper = new ObjectMapper();
        String role;
        role = "ROLE_" + upper;
        System.out.println(role);

        // 유효한 역할인지 확인
        if (!"ROLE_USER".equals(role) && !"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.badRequest().body("유효하지 않은 역할 값입니다.");
        }

        // 사용자 역할 업데이트
        User updatedUser = userService.updateUserRole(userId, role);
        if (updatedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        // 응답 메시지를 JSON 형태로 반환
        Map<String, String> response = new HashMap<>();
        response.put("message", "역할이 성공적으로 업데이트되었습니다.");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{userId}/add-admin")
    public ResponseEntity<Admin> addAdmin(
            @PathVariable("userId") Long userId,
            @RequestBody UserDTO userDTO) {

        User user = userRepository.findById(userId).orElse(null);
        System.out.println("AdminController.addAdmin");

        Admin admin = new Admin();
        // UserDTO를 사용하여 Admin 엔티티를 설정

        admin.setRole(userDTO.getRole());
        admin.setUsername(user.getUsername());
        admin.setPassword(user.getPassword());
        admin.setEmail(user.getEmail());
        admin.setStatus("basic"); // 기본 상태 설정
        // 저장 및 응답
        adminService.addAdmin(admin);
        return ResponseEntity.ok(admin);
    }

    // Admin 삭제 요청 (DELETE)
    @DeleteMapping("/{userId}/remove-admin")
    public ResponseEntity<?> removeAdmin(@PathVariable Long userId) {
        try {
            // userId로 Admin 엔티티를 조회
            Optional<Admin> adminOptional = adminService.findById(userId);

            // 관리자가 존재하는지 확인
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();

                // 관리자 삭제
                adminService.deleteAdmin(admin);

                return ResponseEntity.ok("관리자 권한이 성공적으로 삭제되었습니다.");
            } else {
                // 관리자가 존재하지 않을 경우
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            // 예외 발생 시 오류 메시지 반환
            return ResponseEntity.badRequest().body("관리자 삭제 중 오류가 발생했습니다.");
        }
    }

    //로그인 시 권한(user-admin) 확인용
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequest) {
        // 실제 로그인 로직은 여기에 추가 (예: 사용자 인증 및 역할 확인)
        // 예제에서는 간단히 사용자 인증을 모의로 처리

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 더미 데이터 - 실제 애플리케이션에서는 데이터베이스와 인증 서비스가 필요합니다.
        if ("admin".equals(username) && "password".equals(password)) {
            return ResponseEntity.ok(createResponse("ROLE_ADMIN", "dummy-access-token"));
        } else if ("user".equals(username) && "password".equals(password)) {
            return ResponseEntity.ok(createResponse("ROLE_USER", "dummy-access-token"));
        } else {
            // 잘못된 로그인 시도
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("아이디와 비밀번호가 틀립니다."));
        }
    }

    private Map<String, Object> createResponse(String role, String accessToken) {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("role", role);
        response.put("access", accessToken);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", false);
        response.put("error", message);
        return response;
    }

}
