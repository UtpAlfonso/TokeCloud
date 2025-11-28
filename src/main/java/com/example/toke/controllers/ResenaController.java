package com.example.toke.controllers;

import com.example.toke.dto.ResenaDTO;
import com.example.toke.services.ResenaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResenaController {

    private final ResenaService resenaService;

    @Autowired
    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @PostMapping("/producto/reseña/nueva")
    public String guardarResena(@Valid @ModelAttribute("nuevaResena") ResenaDTO resenaDTO,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        String urlRedireccion = "redirect:/producto/" + resenaDTO.getProductoId();

        if (result.hasErrors()) {
            // Guarda los errores en Flash Attributes para mostrarlos después de la redirección
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.nuevaResena", result);
            redirectAttributes.addFlashAttribute("nuevaResena", resenaDTO);
            return urlRedireccion;
        }

        try {
            resenaService.crearResena(resenaDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("reseñaExito", "¡Gracias! Tu reseña ha sido publicada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reseñaError", "Error al guardar tu reseña: " + e.getMessage());
        }
        
        return urlRedireccion;
    }
}