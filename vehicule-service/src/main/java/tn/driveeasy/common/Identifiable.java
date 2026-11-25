package tn.driveeasy.common;

/** Contrat minimal d'une entité stockée en mémoire : un identifiant technique. */
public interface Identifiable {

    Long getId();

    void setId(Long id);
}
