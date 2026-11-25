package tn.driveeasy.vehicule.repository;

import org.springframework.stereotype.Repository;
import tn.driveeasy.common.InMemoryRepository;
import tn.driveeasy.vehicule.model.Vehicule;

import java.util.Optional;

/** Dépôt en mémoire des véhicules. */
@Repository
public class VehiculeRepository extends InMemoryRepository<Vehicule> {

    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        return store.values().stream()
                .filter(v -> v.getImmatriculation().equalsIgnoreCase(immatriculation))
                .findFirst();
    }
}
