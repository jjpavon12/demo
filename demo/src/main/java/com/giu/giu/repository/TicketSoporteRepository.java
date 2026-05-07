package com.giu.giu.repository;

import com.giu.giu.model.TicketSoporte;
import com.giu.giu.model.Usuario;
import com.giu.giu.model.EstadoSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {
    List<TicketSoporte> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    List<TicketSoporte> findAllByOrderByFechaCreacionDesc();
    List<TicketSoporte> findByEstadoOrderByFechaCreacionDesc(EstadoSoporte estado);
    List<TicketSoporte> findByEstadoAndTieneMensajesNuevosOrderByFechaCreacionDesc(EstadoSoporte estado, boolean tieneMensajesNuevos);
    List<TicketSoporte> findByUsuarioAndEstado(Usuario usuario, EstadoSoporte estado);
    Optional<TicketSoporte> findByIdAndUsuario(Long id, Usuario usuario);
    long countByEstado(EstadoSoporte estado);
    long countByTieneMensajesNuevosAndEstado(boolean tieneMensajesNuevos, EstadoSoporte estado);
}
