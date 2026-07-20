package com.sweetscoop.admin.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sweetscoop.admin.dto.request.RegisterSaveDto;
import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RegisterService {


    private final BranchManagerRepository branchManagerRepository;


    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();



    public void register(RegisterSaveDto request) {


        // 아이디 중복 검사
        if(branchManagerRepository
                .findByLoginId(request.getLoginId())
                .isPresent()) {

            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }


        BranchManager manager = new BranchManager();
        
        String id = "BM_" + String.format("%03d",
                branchManagerRepository.count() + 1);
        
        manager.setId(id);

        manager.setLoginId(
                request.getLoginId()
        );


        // ⭐ 여기서 암호화
        manager.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        manager.setName(
                request.getName()
        );


        manager.setBranchId(
                request.getBranchId()
        );


        branchManagerRepository.save(manager);
    }
}