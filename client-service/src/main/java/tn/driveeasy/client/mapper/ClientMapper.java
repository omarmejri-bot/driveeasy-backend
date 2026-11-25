package tn.driveeasy.client.mapper;

import tn.driveeasy.client.dto.ClientRequest;
import tn.driveeasy.client.dto.ClientResponse;
import tn.driveeasy.client.model.Client;

import java.time.LocalDate;
import java.time.Period;

/** Conversion entité <-> DTO et champs calculés. */
public final class ClientMapper {

    private ClientMapper() {
    }

    public static ClientResponse toResponse(Client c) {
        return new ClientResponse(c.getId(), c.getCin(), c.getNom(), c.getPrenom(), c.getDateNaissance(),
                age(c), c.getEmail(), c.getTelephone(), c.getAdresse(), c.getNumeroPermis(),
                c.getDateExpirationPermis(), permisValide(c), c.getCategorieClient(), c.getDateInscription());
    }

    public static void updateEntity(Client c, ClientRequest r) {
        c.setCin(r.cin().trim());
        c.setNom(r.nom().trim());
        c.setPrenom(r.prenom().trim());
        c.setDateNaissance(r.dateNaissance());
        c.setEmail(r.email().trim().toLowerCase());
        c.setTelephone(r.telephone().trim());
        c.setAdresse(r.adresse());
        c.setNumeroPermis(r.numeroPermis().trim().toUpperCase());
        c.setDateExpirationPermis(r.dateExpirationPermis());
    }

    /** Âge en années révolues à la date du jour. */
    public static int age(Client c) {
        return Period.between(c.getDateNaissance(), LocalDate.now()).getYears();
    }

    /** Le permis est valide s'il expire aujourd'hui ou plus tard. */
    public static boolean permisValide(Client c) {
        return !c.getDateExpirationPermis().isBefore(LocalDate.now());
    }
}
