package com.giu.giu.repository;

import com.giu.giu.model.RespuestaSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaSoporteRepository extends JpaRepository<RespuestaSoporte, Long> {
    List<RespuestaSoporte> findByTicketIdOrderByFechaCreacionAsc(Long ticketId);
}
