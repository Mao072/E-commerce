package com.esunbank.ecommerce.service;

import com.esunbank.ecommerce.dto.LoginRequest;
import com.esunbank.ecommerce.dto.LoginResponse;
import com.esunbank.ecommerce.dto.RegisterRequest;
import com.esunbank.ecommerce.entity.Member;
import com.esunbank.ecommerce.repository.MemberRepository;
import com.esunbank.ecommerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        String memberId = "M" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "USER";

        if (!role.equals("ADMIN") && !role.equals("USER")) {
            role = "USER";
        }

        Map<String, Object> result = memberRepository.register(memberId, request.getUsername(), encodedPassword, role);

        int resultCode = (int) result.get("result");
        String message = (String) result.get("message");

        if (resultCode != 1) {
            throw new RuntimeException(message);
        }
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(member.getUsername(), member.getMemberId(), member.getRole());

        return LoginResponse.builder()
                .token(token)
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .role(member.getRole())
                .build();
    }
}
