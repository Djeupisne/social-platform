package com.social.auth.entity;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;

        // Récupérer l'utilisateur connecté
        String username = "SYSTEM";
        String userEmail = "system@localhost";

        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal != null && principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            username = userDetails.getUsername();
            // Vous pouvez ajouter d'autres infos si nécessaire
        }

        revision.setUsername(username);
        revision.setUserEmail(userEmail);

        // Récupérer l'IP et l'action
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String method = request.getMethod();
            String path = request.getRequestURI();
            revision.setAction(method + " " + path);
            revision.setIpAddress(request.getRemoteAddr());

        } catch (IllegalStateException e) {
            revision.setAction("SYSTEM");
            revision.setIpAddress("0.0.0.0");
        }
    }
}