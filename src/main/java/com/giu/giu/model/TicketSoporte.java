package com.giu.giu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets_soporte")
public class TicketSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSoporte estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaCierre;

    // Notificación: ¿tiene mensajes sin leer?
    @Column(nullable = false)
    private boolean tieneMensajesNuevos = false;

    // Chat bidireccional
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("fechaCreacion ASC")
    private List<RespuestaSoporte> respuestas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoSoporte.PENDIENTE;
        }
    }

    // Constructores
    public TicketSoporte() {
    }

    public TicketSoporte(Usuario usuario, String asunto) {
        this.usuario = usuario;
        this.asunto = asunto;
        this.estado = EstadoSoporte.PENDIENTE;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public EstadoSoporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoSoporte estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public boolean isTieneMensajesNuevos() {
        return tieneMensajesNuevos;
    }

    public void setTieneMensajesNuevos(boolean tieneMensajesNuevos) {
        this.tieneMensajesNuevos = tieneMensajesNuevos;
    }

    public List<RespuestaSoporte> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<RespuestaSoporte> respuestas) {
        this.respuestas = respuestas;
    }

    public void agregarRespuesta(RespuestaSoporte respuesta) {
        respuesta.setTicket(this);
        this.respuestas.add(respuesta);
    }

    // Método auxiliar para obtener la última respuesta
    public RespuestaSoporte getUltimaRespuesta() {
        if (respuestas.isEmpty()) {
            return null;
        }
        return respuestas.get(respuestas.size() - 1);
    }
}
