package com.giu.giu.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_extension")
public class SolicitudExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(length = 1000)
    private String comentarioAdicional;

    @Column(nullable = false)
    private LocalDate fechaSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(length = 1000)
    private String motivoDecision;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
    }

    public SolicitudExtension() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Incidencia getIncidencia() { return incidencia; }
    public void setIncidencia(Incidencia incidencia) { this.incidencia = incidencia; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getComentarioAdicional() { return comentarioAdicional; }
    public void setComentarioAdicional(String comentarioAdicional) { this.comentarioAdicional = comentarioAdicional; }

    public LocalDate getFechaSolicitada() { return fechaSolicitada; }
    public void setFechaSolicitada(LocalDate fechaSolicitada) { this.fechaSolicitada = fechaSolicitada; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getMotivoDecision() { return motivoDecision; }
    public void setMotivoDecision(String motivoDecision) { this.motivoDecision = motivoDecision; }
}
