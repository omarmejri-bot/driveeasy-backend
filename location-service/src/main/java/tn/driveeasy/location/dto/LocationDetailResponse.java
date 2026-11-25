package tn.driveeasy.location.dto;

import tn.driveeasy.location.dto.RemoteDtos.ClientSummary;
import tn.driveeasy.location.dto.RemoteDtos.VehiculeSummary;

/**
 * Détail enrichi d'une location : agrège la location, le client et le véhicule.
 * Si un service distant est indisponible lors de la lecture, les informations
 * de base restent fournies et {@code donneesPartielles} vaut {@code true} (dégradation gracieuse).
 */
public record LocationDetailResponse(
        LocationResponse location,
        ClientSummary client,
        VehiculeSummary vehicule,
        boolean donneesPartielles) {
}
