package com.giu.giu.controller;

import com.giu.giu.model.EstadoSoporte;
import com.giu.giu.model.Rol;
import com.giu.giu.model.TicketSoporte;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.TicketSoporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    @Autowired
    private TicketSoporteService ticketService;

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUsuario();
        }
        return null;
    }

    // ==========================================================
    //  USUARIO (CIUDADANO / OPERADOR / TECNICO)
    // ==========================================================

    @GetMapping("/enviar")
    public String mostrarFormularioSoporte(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        if (usuario.getRol() == Rol.ADMINISTRADOR) return "redirect:/dashboard/home";

        model.addAttribute("usuario", usuario);
        return "enviar-soporte";
    }

    @PostMapping("/enviar")
    public String crearTicket(@RequestParam String asunto,
                              @RequestParam String mensaje,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        if (usuario.getRol() == Rol.ADMINISTRADOR) return "redirect:/dashboard/home";

        if (asunto == null || asunto.trim().isEmpty() || mensaje == null || mensaje.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor completa todos los campos");
            return "redirect:/soporte/enviar";
        }

        try {
            TicketSoporte t = ticketService.crearTicket(usuario, asunto.trim(), mensaje.trim());
            redirectAttributes.addFlashAttribute("success", "Tu ticket ha sido creado. El administrador te responderá pronto.");
            return "redirect:/soporte/mensaje/" + t.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el ticket: " + e.getMessage());
            return "redirect:/soporte/enviar";
        }
    }

    @GetMapping("/mis-mensajes")
    public String verMisMensajes(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        if (usuario.getRol() == Rol.ADMINISTRADOR) return "redirect:/dashboard/home";

        List<TicketSoporte> tickets = ticketService.obtenerTicketsUsuario(usuario);

        long pendientes  = tickets.stream().filter(t -> t.getEstado() == EstadoSoporte.PENDIENTE).count();
        long respondidos = tickets.stream().filter(t -> t.getEstado() == EstadoSoporte.RESPONDIDO).count();

        model.addAttribute("usuario", usuario);
        model.addAttribute("mensajes", tickets);
        model.addAttribute("totalMensajes", tickets.size());
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("respondidos", respondidos);
        return "mi-soporte";
    }

    @GetMapping("/mensaje/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        Optional<TicketSoporte> ticketOpt = ticketService.obtenerTicket(id, usuario);
        if (!ticketOpt.isPresent()) {
            return "redirect:/soporte/mis-mensajes";
        }

        // Al abrir el ticket, marcarlo como leído (limpia la notificación)
        if (ticketOpt.get().isTieneMensajesNuevos()) {
            ticketService.marcarComoLeido(id);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("mensaje", ticketOpt.get());
        return "detalle-soporte";
    }

    @PostMapping("/mensaje/{id}/responder")
    public String responderUsuario(@PathVariable Long id,
                                   @RequestParam String mensaje,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        Optional<TicketSoporte> ticketOpt = ticketService.obtenerTicket(id, usuario);
        if (!ticketOpt.isPresent()) {
            return "redirect:/soporte/mis-mensajes";
        }

        if (mensaje == null || mensaje.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El mensaje no puede estar vacío");
            return "redirect:/soporte/mensaje/" + id;
        }

        try {
            ticketService.agregarRespuestaUsuario(id, usuario, mensaje.trim());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar el mensaje: " + e.getMessage());
        }
        return "redirect:/soporte/mensaje/" + id;
    }

    @PostMapping("/mensaje/{id}/cerrar")
    public String cerrarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        Optional<TicketSoporte> ticketOpt = ticketService.obtenerTicket(id, usuario);
        if (!ticketOpt.isPresent()) {
            return "redirect:/soporte/mis-mensajes";
        }

        try {
            ticketService.cerrarTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket cerrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar el ticket: " + e.getMessage());
        }
        return "redirect:/soporte/mensaje/" + id;
    }

    // ==========================================================
    //  ADMINISTRADOR
    // ==========================================================

    @GetMapping("/admin/mensajes")
    public String adminListaTickets(@RequestParam(defaultValue = "TODOS") String filtro, Model model) {
        Usuario admin = getUsuarioAutenticado();
        if (admin == null || admin.getRol() != Rol.ADMINISTRADOR) {
            return "redirect:/login";
        }

        List<TicketSoporte> tickets;
        if ("PENDIENTE".equals(filtro)) {
            tickets = ticketService.obtenerTicketsPorEstado(EstadoSoporte.PENDIENTE);
        } else if ("RESPONDIDO".equals(filtro)) {
            tickets = ticketService.obtenerTicketsPorEstado(EstadoSoporte.RESPONDIDO);
        } else if ("CERRADO".equals(filtro)) {
            tickets = ticketService.obtenerTicketsPorEstado(EstadoSoporte.CERRADO);
        } else {
            tickets = ticketService.obtenerTodosTickets();
        }

        model.addAttribute("usuario", admin);
        model.addAttribute("mensajes", tickets);
        model.addAttribute("filtroActual", filtro);
        model.addAttribute("totalMensajes", ticketService.obtenerTodosTickets().size());
        model.addAttribute("pendientes", ticketService.contarTicketsPorEstado(EstadoSoporte.PENDIENTE));
        return "soporte-admin";
    }

    @GetMapping("/admin/mensaje/{id}")
    public String adminDetalle(@PathVariable Long id, Model model) {
        Usuario admin = getUsuarioAutenticado();
        if (admin == null || admin.getRol() != Rol.ADMINISTRADOR) {
            return "redirect:/login";
        }

        Optional<TicketSoporte> ticketOpt = ticketService.obtenerTicket(id);
        if (!ticketOpt.isPresent()) {
            return "redirect:/soporte/admin/mensajes";
        }

        model.addAttribute("usuario", admin);
        model.addAttribute("ticket", ticketOpt.get());
        return "detalle-soporte-admin";
    }

    @PostMapping("/admin/responder/{id}")
    public String adminResponder(@PathVariable Long id,
                                 @RequestParam String respuesta,
                                 RedirectAttributes redirectAttributes) {
        Usuario admin = getUsuarioAutenticado();
        if (admin == null || admin.getRol() != Rol.ADMINISTRADOR) {
            return "redirect:/login";
        }

        if (respuesta == null || respuesta.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La respuesta no puede estar vacía");
            return "redirect:/soporte/admin/mensaje/" + id;
        }

        try {
            ticketService.agregarRespuestaAdmin(id, admin, respuesta.trim());
            redirectAttributes.addFlashAttribute("success", "Respuesta enviada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al responder: " + e.getMessage());
        }
        return "redirect:/soporte/admin/mensaje/" + id;
    }

    @PostMapping("/admin/cerrar/{id}")
    public String adminCerrar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario admin = getUsuarioAutenticado();
        if (admin == null || admin.getRol() != Rol.ADMINISTRADOR) {
            return "redirect:/login";
        }

        try {
            ticketService.cerrarTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket cerrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar el ticket: " + e.getMessage());
        }
        return "redirect:/soporte/admin/mensajes";
    }

    @PostMapping("/admin/eliminar/{id}")
    public String adminEliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario admin = getUsuarioAutenticado();
        if (admin == null || admin.getRol() != Rol.ADMINISTRADOR) {
            return "redirect:/login";
        }

        try {
            ticketService.eliminarTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el ticket: " + e.getMessage());
        }
        return "redirect:/soporte/admin/mensajes";
    }
}
