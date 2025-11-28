package com.example.toke.services;

// ... imports para DTOs, Pedido, etc. ...
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.example.toke.dto.PedidoDetalleDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class EmailService {

    private final TemplateEngine templateEngine;
    private final PdfGenerationService pdfService;
    
    @Value("${resend.api.key}")
    private String resendApiKey;
    
    @Value("${resend.from.email}")
    private String fromEmail;

    public EmailService(TemplateEngine templateEngine, PdfGenerationService pdfService) {
        this.templateEngine = templateEngine;
        this.pdfService = pdfService;
    }

    @Async
    public void enviarBoletaPorCorreo(PedidoDetalleDTO pedido, String emailCliente) {
        try {
            // 1. Genera el contenido HTML del correo
            Context context = new Context();
            context.setVariable("pedido", pedido);
            String htmlBody = templateEngine.process("email/confirmacion-pedido", context);
            
            // 2. Genera el PDF de la boleta
            byte[] boletaPdf = pdfService.generarBoletaPdf(pedido);
            String boletaBase64 = Base64.getEncoder().encodeToString(boletaPdf);

            // 3. Construye el cuerpo JSON para la API de Resend
            String jsonPayload = String.format("{"
                + "\"from\": \"%s\","
                + "\"to\": [\"%s\"],"
                + "\"subject\": \"Confirmación de tu pedido en Toke Inca #%d\","
                + "\"html\": \"%s\","
                + "\"attachments\": [{"
                + "    \"filename\": \"Boleta-Pedido-%d.pdf\","
                + "    \"content\": \"%s\""
                + "}]"
                + "}",
                fromEmail,
                emailCliente,
                pedido.getId(),
                escapeJson(htmlBody), // Escapamos el HTML para que sea un JSON válido
                pedido.getId(),
                boletaBase64);

            // 4. Realiza la llamada a la API de Resend
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Correo de confirmación enviado exitosamente a " + emailCliente);
            } else {
                System.err.println("Error al enviar correo con Resend. Status: " + response.statusCode() + ", Body: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al enviar el correo de confirmación: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    // Pequeña utilidad para escapar caracteres en el HTML para el JSON
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}