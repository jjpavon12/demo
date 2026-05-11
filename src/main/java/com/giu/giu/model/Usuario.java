package com.giu.giu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Rol rolSolicitado;

    @Column
    private LocalDateTime notificacionesVistasHasta;

    @Column
    private LocalDateTime notifCiudadanoEstadosVistaHasta;

    @Column
    private LocalDateTime notifOperadorNuevasVistaHasta;

    @Column
    private LocalDateTime notifOperadorCambiosVistaHasta;

    @Column
    private LocalDateTime notifTecnicoAsignadasVistaHasta;

    @Column
    private LocalDateTime notifTecnicoExtensionesVistaHasta;

    @Column
    private LocalDateTime notifTecnicoLimiteVistaHasta;

    public Usuario() {
    }

    public Usuario(String email, String password, Rol rol, boolean activo) {
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Rol getRolSolicitado() {
        return rolSolicitado;
    }

    public void setRolSolicitado(Rol rolSolicitado) {
        this.rolSolicitado = rolSolicitado;
    }

    public LocalDateTime getNotificacionesVistasHasta() {
        return notificacionesVistasHasta;
    }

    public void setNotificacionesVistasHasta(LocalDateTime notificacionesVistasHasta) {
        this.notificacionesVistasHasta = notificacionesVistasHasta;
    }

    public LocalDateTime getNotifCiudadanoEstadosVistaHasta() { return notifCiudadanoEstadosVistaHasta; }
    public void setNotifCiudadanoEstadosVistaHasta(LocalDateTime value) { this.notifCiudadanoEstadosVistaHasta = value; }
    public LocalDateTime getNotifOperadorNuevasVistaHasta() { return notifOperadorNuevasVistaHasta; }
    public void setNotifOperadorNuevasVistaHasta(LocalDateTime value) { this.notifOperadorNuevasVistaHasta = value; }
    public LocalDateTime getNotifOperadorCambiosVistaHasta() { return notifOperadorCambiosVistaHasta; }
    public void setNotifOperadorCambiosVistaHasta(LocalDateTime value) { this.notifOperadorCambiosVistaHasta = value; }
    public LocalDateTime getNotifTecnicoAsignadasVistaHasta() { return notifTecnicoAsignadasVistaHasta; }
    public void setNotifTecnicoAsignadasVistaHasta(LocalDateTime value) { this.notifTecnicoAsignadasVistaHasta = value; }
    public LocalDateTime getNotifTecnicoExtensionesVistaHasta() { return notifTecnicoExtensionesVistaHasta; }
    public void setNotifTecnicoExtensionesVistaHasta(LocalDateTime value) { this.notifTecnicoExtensionesVistaHasta = value; }
    public LocalDateTime getNotifTecnicoLimiteVistaHasta() { return notifTecnicoLimiteVistaHasta; }
    public void setNotifTecnicoLimiteVistaHasta(LocalDateTime value) { this.notifTecnicoLimiteVistaHasta = value; }
}
