package com.social.report.util;
import com.social.report.dto.MenageEligibleDto;
import com.social.report.dto.ProgrammeEligibiliteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class PdfGenerator {

    public byte[] generateEligibiliteReport(ProgrammeEligibiliteDto programme) {
        log.info("Génération PDF d'éligibilité pour programme: {}", programme.getNomProgramme());

        // TODO: Implémenter la génération PDF avec une bibliothèque comme iText ou OpenPDF
        // Pour l'instant, on retourne un message simple

        StringBuilder sb = new StringBuilder();
        sb.append("=== RAPPORT D'ÉLIGIBILITÉ ===\n\n");
        sb.append("Programme: ").append(programme.getNomProgramme()).append("\n");
        sb.append("Description: ").append(programme.getDescription()).append("\n");
        sb.append("Date génération: ").append(programme.getDateGeneration()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Total éligibles: ").append(programme.getTotalMenagesEligibles()).append("\n\n");

        if (programme.getMenagesEligibles() != null) {
            sb.append("MÉNAGES ÉLIGIBLES:\n");
            sb.append("-----------------\n");
            for (MenageEligibleDto m : programme.getMenagesEligibles()) {
                sb.append("- ").append(m.getCodeMenage())
                        .append(" | ").append(m.getChefNom())
                        .append(" | Score: ").append(m.getScore())
                        .append(" | ").append(m.getRegion())
                        .append("\n");
            }
        }

        return sb.toString().getBytes();
    }

    public byte[] generateMenagesReport(Object data) {
        log.info("Génération PDF standard");
        // TODO: Implémenter
        return "Rapport PDF".getBytes();
    }
}