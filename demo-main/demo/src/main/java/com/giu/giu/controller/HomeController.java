package com.giu.giu.controller;

import com.giu.giu.dto.LoginResponse;
import com.giu.giu.model.Rol;
import com.giu.giu.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final UsuarioService usuarioService;

    public HomeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "registered", required = false) String registered,
                        @RequestParam(value = "pending", required = false) String pending,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "pendiente", required = false) String pendiente,
                        Model model) {
        if (registered != null) {
            model.addAttribute("registeredMessage", "Usuario registrado correctamente. Inicia sesión.");
        }
        if (pendiente != null) {
            model.addAttribute("pendienteMessage", "Cuenta creada. Un administrador debe aprobarla antes de que puedas iniciar sesión.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("permitirAdminInicial", !usuarioService.existeAdministrador());
        return "register";
    }

    @PostMapping("/registro")
    public String registroSubmit(@RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam(defaultValue = "CIUDADANO") Rol rol,
                                 Model model) {

        LoginResponse response = usuarioService.registrar(email, password, rol);
        if (response.getId() != null) {
            // Si requiere aprobación, mostrar mensaje diferente
            if (rol == Rol.OPERADOR || rol == Rol.TECNICO) {
                return "redirect:/login?pendiente=true";
            }
            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("permitirAdminInicial", !usuarioService.existeAdministrador());
            model.addAttribute("error", response.getMensaje());
            return "register";
        }
    }
}
