package com.sweetscoop.member.repository;
import com.sweetscoop.member.entity.Member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member, Integer> { 
	Optional<Member> findByPhoneNumber(String phoneNumber);
	boolean existsByPhoneNumber(String phoneNumber);
}
