package com.project.project.service;

import com.project.project.dto.UserRegistrationDto;
import com.project.project.dto.UserUpdateDto;
import com.project.project.entity.User;
import com.project.project.exception.EmailAlreadyInUseException;
import com.project.project.exception.PasswordMismatchException;
import com.project.project.exception.UserNotFoundException;
import com.project.project.repository.UserRepository;
import com.project.project.util.JWTUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JWTUtil jwtUtil
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    @Transactional
    public User signup(UserRegistrationDto userRegistrationDto){
        // 이메일 중복 체크
        if (userRepository.findByEmail(userRegistrationDto.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("이미 사용중인 이메일입니다.");

        }

        if (!userRegistrationDto.getPassword().equals(userRegistrationDto.getConfirmPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");

        }

        User user = new User();
        user.setUsername(userRegistrationDto.getName());
        user.setEmail(userRegistrationDto.getEmail());
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));
        user.setProfilePictureUrl(null);
        user.setBio(null);
        user.setUserSkills(null);
        user.setStatus("ACTIVE");
        user.setUserInterests(null);
        user.setAddress(userRegistrationDto.getAddress());
        user.setPostcode(userRegistrationDto.getPostcode());
        user.SetUserRole("ROLE_USER");
        return userRepository.save(user);
    }



    @Transactional
    public User updateUser(String email, UserUpdateDto userUpdateDto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.setUsername(userUpdateDto.getUsername());
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()){
            if(!userUpdateDto.getPassword().equals(userUpdateDto.getConfirmPassword())){
                throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        user.setProfilePictureUrl(userUpdateDto.getProfilePictureUrl());
        user.setBio(userUpdateDto.getBio());
        user.setUserSkills(null);
        user.setUserInterests(null);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email){
        if (!userRepository.existsUserByEmail(email)){
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteUserByEmail(email);
    }

}
