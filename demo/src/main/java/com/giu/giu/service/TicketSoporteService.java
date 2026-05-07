package com.giu.giu.service;

import com.giu.giu.model.TicketSoporte;
import com.giu.giu.model.RespuestaSoporte;
import com.giu.giu.model.Usuario;
import com.giu.giu.model.EstadoSoporte;
import com.giu.giu.model.TipoRemitente;
import com.giu.giu.repository.TicketSoporteRepository;
import com.giu.giu.repository.RespuestaSoporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketSoporteService {

    @Autowired
    private TicketSoporteRepository ticketRepository;

    @Autowired
    private RespuestaSoporteRepository respuestaRepository;

    /**
     * Crear un nuevo ticket de soporte
     */
    public TicketSoporte crearTicket(Usuario usuario, String asunto, String mensajeInicial) {
        TicketSoporte ticket = new TicketSoporte(usuario, asunto);
        ticket = ticketRepository.save(ticket);
        
        // Agregar el mensaje inicial del usuario
        RespuestaSoporte respuesta = new RespuestaSoporte(ticket, TipoRemitente.USUARIO, usuario, mensajeInicial);
        respuestaRepository.save(respuesta);
        
        return ticket;
    }

    /**
     * Obtener todos los tickets de un usuario
     */
    public List<TicketSoporte> obtenerTicketsUsuario(Usuario usuario) {
        return ticketRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    /**
     * Obtener todos los tickets (solo para admin)
     */
    public List<TicketSoporte> obtenerTodosTickets() {
        return ticketRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Obtener tickets por estado
     */
    public List<TicketSoporte> obtenerTicketsPorEstado(EstadoSoporte estado) {
        return ticketRepository.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    /**
     * Obtener tickets pendientes con mensajes nuevos
     */
    public List<TicketSoporte> obtenerTicketsPendientesNuevos() {
        return ticketRepository.findByEstadoAndTieneMensajesNuevosOrderByFechaCreacionDesc(EstadoSoporte.PENDIENTE, true);
    }

    /**
     * Obtener un ticket específico por ID y usuario
     */
    public Optional<TicketSoporte> obtenerTicket(Long id, Usuario usuario) {
        return ticketRepository.findByIdAndUsuario(id, usuario);
    }

    /**
     * Obtener un ticket específico por ID
     */
    public Optional<TicketSoporte> obtenerTicket(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Agregar respuesta del usuario
     */
    public TicketSoporte agregarRespuestaUsuario(Long ticketId, Usuario usuario, String contenido) {
        Optional<TicketSoporte> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            TicketSoporte ticket = ticketOpt.get();
            
            // Si el ticket estaba cerrado, reabrirlo
            if (ticket.getEstado() == EstadoSoporte.CERRADO) {
                ticket.setEstado(EstadoSoporte.PENDIENTE);
            }
            
            // Agregar respuesta
            RespuestaSoporte respuesta = new RespuestaSoporte(ticket, TipoRemitente.USUARIO, usuario, contenido);
            respuestaRepository.save(respuesta);
            
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket no encontrado");
    }

    /**
     * Agregar respuesta del administrador
     */
    public TicketSoporte agregarRespuestaAdmin(Long ticketId, Usuario admin, String contenido) {
        Optional<TicketSoporte> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            TicketSoporte ticket = ticketOpt.get();
            
            // Agregar respuesta del admin
            RespuestaSoporte respuesta = new RespuestaSoporte(ticket, TipoRemitente.ADMINISTRADOR, admin, contenido);
            respuestaRepository.save(respuesta);
            
            // Marcar como respondido y activar notificación
            ticket.setEstado(EstadoSoporte.RESPONDIDO);
            ticket.setTieneMensajesNuevos(true);
            
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket no encontrado");
    }

    /**
     * Marcar ticket como leído (sin mensajes nuevos)
     */
    public TicketSoporte marcarComoLeido(Long id) {
        Optional<TicketSoporte> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            TicketSoporte ticket = ticketOpt.get();
            ticket.setTieneMensajesNuevos(false);
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket no encontrado");
    }

    /**
     * Cerrar un ticket
     */
    public TicketSoporte cerrarTicket(Long id) {
        Optional<TicketSoporte> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            TicketSoporte ticket = ticketOpt.get();
            ticket.setEstado(EstadoSoporte.CERRADO);
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setTieneMensajesNuevos(false);
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket no encontrado");
    }

    /**
     * Contar tickets pendientes con mensajes nuevos
     */
    public long contarTicketsPendientesNuevos() {
        return ticketRepository.countByTieneMensajesNuevosAndEstado(true, EstadoSoporte.PENDIENTE);
    }

    /**
     * Contar tickets por estado
     */
    public long contarTicketsPorEstado(EstadoSoporte estado) {
        return ticketRepository.countByEstado(estado);
    }

    /**
     * Eliminar un ticket
     */
    public void eliminarTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
