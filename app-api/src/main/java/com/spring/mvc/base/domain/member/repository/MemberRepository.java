package com.spring.mvc.base.domain.member.repository;

import com.spring.mvc.base.domain.member.entity.Member;
import com.spring.mvc.base.domain.member.entity.MemberStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByStatus(MemberStatus status);

    Optional<Member> findByIdAndStatus(Long id, MemberStatus status);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
