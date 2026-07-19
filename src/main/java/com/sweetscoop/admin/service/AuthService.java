package com.sweetscoop.admin.service;

import java.util.Optional; // 👈 Optional 컴파일 에러 해결용 임포트

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sweetscoop.admin.dto.request.LoginRequestDto;
import com.sweetscoop.admin.dto.response.LoginResponse;
import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.entity.HqManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;
import com.sweetscoop.admin.repository.HqManagerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    // 💡 필수 의존성 주입 (이 부분이 있어야 NullPointerException이 발생하지 않습니다)
    private final BranchManagerRepository branchManagerRepository;
    private final HqManagerRepository hqManagerRepository;
    
    // BCrypt 검증용 객체 선언 (의존성 충돌 방지를 위해 직접 생성)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse authenticate(LoginRequestDto request) {
    	
    	String loginId = request.getUsername();
    	String password = request.getPassword();
    	
    	Optional<HqManager> hq = hqManagerRepository.findByLoginId(loginId);
    	
    	if (hq.isPresent()) {
			if (passwordEncoder.matches(password, hq.get().getPassword())) {
				return new LoginResponse(
					true,
	                "HQ",
	                hq.get().getLoginId(),
	                hq.get().getName(),
	                null
				);
			}
		}
    	
    	Optional<BranchManager> branch = 
    			branchManagerRepository.findByLoginId(loginId);
    	
    	if (branch.isPresent()) {
    		System.out.println("입력 비밀번호 : " + password);
    	    System.out.println("DB 해시 : " + branch.get().getPassword());

    	    boolean result = passwordEncoder.matches(
    	            password,
    	            branch.get().getPassword()
    	    );

    	    System.out.println("BCrypt 결과 : " + result);


    	    if(result) {

    	        return new LoginResponse(
    	            true,
    	            "BRANCH",
    	            branch.get().getLoginId(),
    	            branch.get().getName(),
    	            branch.get().getBranchId()
    	        );
    	    }
		}
    	return new LoginResponse(false,null,null,null,null);
    }
    
}