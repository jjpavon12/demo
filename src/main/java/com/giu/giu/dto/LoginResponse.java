package com.giu.giu.dto;

import com.giu.giu.model.Rol;

public class LoginResponse {
    private Long id;
    private String email;
    private Rol rol;
    private String mensaje;

    public LoginResponse() {
    }

    public LoginResponse(Long id, String email, Rol rol, String mensaje) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.mensaje = mensaje;
    }

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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
