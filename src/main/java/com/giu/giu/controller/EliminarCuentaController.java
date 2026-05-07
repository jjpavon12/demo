package com.giu.giu.controller;

import com.giu.giu.config.DatabaseInitializer;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mi-cuenta")
public class EliminarCuentaController {

    private final UsuarioService usuarioService;

    public EliminarCuentaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/eliminar")
    public String mostrar(Model model) {
        Usuario usuario = getUsuario();
        if (usuario == null) return "redirect:/login";
        if (usuario.getRol() == Rol.ADMINISTRADOR) return "redirect:/dashboard/home";
        if (DatabaseInitializer.EMAIL_ANONIMO.equals(usuario.getEmail())) return "redirect:/dashboard/home";
        model.addAttribute("usuario", usuario);
        return "eliminar-cuenta";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String password,
                           @RequestParam(required = false) String aceptar,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        Usuario usuario = getUsuario();
        if (usuario == null) return "redirect:/login";
        if (usuario.getRol() == Rol.ADMINISTRADOR) return "redirect:/dashboard/home";
        if (DatabaseInitializer.EMAIL_ANONIMO.equals(usuario.getEmail())) return "redirect:/dashboard/home";

        if (!"on".equals(aceptar)) {
            redirectAttributes.addFlashAttribute("error", "Debes marcar la casilla de confirmación para continuar.");
            return "redirect:/mi-cuenta/eliminar";
        }

        String errorMsg;
        try {
            errorMsg = usuarioService.eliminarCuentaPropia(usuario.getId(), password);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la cuenta. Inténtalo de nuevo.");
            return "redirect:/mi-cuenta/eliminar";
        }
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/mi-cuenta/eliminar";
        }

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        return "redirect:/?cuentaEliminada=true";
    }

    private Usuario getUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            return ud.getUsuario();
        }
        return null;
    }
}
