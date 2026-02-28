package com.social.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j  // ✅ AJOUTÉ - permet d'utiliser log.info(), log.error()
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Code de vérification - Plateforme Sociale Togo");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de vérification est : " + otp + "\n\n" +
                        "Ce code expire dans 5 minutes.\n\n" +
                        "Plateforme Sociale Togo"
        );
        mailSender.send(message);
        log.info("Email OTP envoyé à {}", to);
    }

    public void sendTwoFactorCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Code de vérification 2FA");
            message.setText("Votre code est: " + code);
            mailSender.send(message);
            log.info("Email 2FA envoyé à {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email à {}: {}", to, e.getMessage());
            // En développement, on log juste le code
            log.info("Code 2FA pour {}: {}", to, code);
        }
    }

    public void sendWelcomeEmail(String to, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bienvenue - Plateforme Sociale Togo");
        message.setText(
                "Bonjour " + fullName + ",\n\n" +
                        "Votre compte a été créé avec succès.\n\n" +
                        "Plateforme Sociale Togo"
        );
        mailSender.send(message);
        log.info("Email de bienvenue envoyé à {}", to);
    }
}