package com.scaler.EcomUserService.repository;

import com.scaler.EcomUserService.model.Session;
import com.scaler.EcomUserService.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTokenAndUser_Id(String token, Long userId);
    Optional<List<Session>> findByUser_idAndSessionStatus(Long userId , SessionStatus sessionStatus);
}