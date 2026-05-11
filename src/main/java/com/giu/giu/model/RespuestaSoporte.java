package com.giu.giu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuestas_soporte")
public class RespuestaSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketSoporte ticket;

    // Quién escribió: usuario o admin
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRemitente tipoRemitente;

    // El autor de la respuesta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 3000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructores
    public RespuestaSoporte() {
    }

    public RespuestaSoporte(TicketSoporte ticket, TipoRemitente tipoRemitente, Usuario usuario, String contenido) {
        this.ticket = ticket;
        this.tipoRemitente = tipoRemitente;
        this.usuario = usuario;
        this.contenido = contenido;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TicketSoporte getTicket() {
        return ticket;
    }

    public void setTicket(TicketSoporte ticket) {
        this.ticket = ticket;
    }

    public TipoRemitente getTipoRemitente() {
        return tipoRemitente;
    }

    public void setTipoRemitente(TipoRemitente tipoRemitente) {
        this.tipoRemitente = tipoRemitente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
