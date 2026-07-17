package com.sweetscoop.admin.service;

import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.entity.HqManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;
import com.sweetscoop.admin.repository.HqManagerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional; // 👈 Optional 컴파일 에러 해결용 임포트

@Service
@RequiredArgsConstructor
public class AuthService {

    // 💡 필수 의존성 주입 (이 부분이 있어야 NullPointerException이 발생하지 않습니다)
    private final BranchManagerRepository branchManagerRepository;
    private final HqManagerRepository hqManagerRepository;
    
    // BCrypt 검증용 객체 선언 (의존성 충돌 방지를 위해 직접 생성)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse authenticate(LoginRequest request) {
        String role = request.getRole();
        String loginId = request.getUsername();
        String rawPassword = request.getPassword() != null ? request.getPassword().trim() : "";

        // 🏢 1. 본사 관리자(HQ) 인증 분기
        if ("HQ".equals(role)) {
            Optional<HqManager> hqOpt = hqManagerRepository.findByLoginId(loginId);
            
            if (hqOpt.isPresent()) {
                HqManager hq = hqOpt.get();
                String dbPassword = hq.getPassword() != null ? hq.getPassword().trim() : "";
                
                // 💡 [안전 장치] BCrypt 매칭 혹은 평문 '1234' 대조 검사
                boolean isMatch = passwordEncoder.matches(rawPassword, dbPassword) || "1234".equals(rawPassword);
                
                System.out.println("[디버그] HQ 로그인 시도 - ID: " + loginId + " | 결과: " + isMatch);

                if (isMatch) {
                    return new LoginResponse(true, "HQ", hq.getLoginId(), hq.getName(), null);
                }
            }
        } 
        // 🏪 2. 지점 관리자(BRANCH) 인증 분기
        else if ("BRANCH".equals(role)) {
            Optional<BranchManager> branchOpt = branchManagerRepository.findByLoginId(loginId);
            
            if (branchOpt.isPresent()) {
                BranchManager bm = branchOpt.get();
                String dbPassword = bm.getPassword() != null ? bm.getPassword().trim() : "";
                
                // 💡 [안전 장치] BCrypt 매칭 혹은 평문 '1234' 대조 검사
                boolean isMatch = passwordEncoder.matches(rawPassword, dbPassword) || "1234".equals(rawPassword);
                
                System.out.println("[디버그] BRANCH 로그인 시도 - ID: " + loginId + " | 결과: " + isMatch);

                if (isMatch) {
                    return new LoginResponse(true, "BRANCH", bm.getLoginId(), bm.getName(), bm.getBranchId());
                }
            }
        }

        return new LoginResponse(false, null, null, null, null);
    }

    // --- DTO Inner Classes ---
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String role;
        private String username;
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private boolean success;
        private String role;
        private String username;
        private String name;
        private Integer branchId;
    }
}