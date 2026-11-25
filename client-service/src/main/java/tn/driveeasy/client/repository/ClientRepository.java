package tn.driveeasy.client.repository;

import org.springframework.stereotype.Repository;
import tn.driveeasy.client.model.Client;
import tn.driveeasy.common.InMemoryRepository;

import java.util.Optional;

/** Dépôt en mémoire des clients. */
@Repository
public class ClientRepository extends InMemoryRepository<Client> {

    public Optional<Client> findByCin(String cin) {
        return store.values().stream().filter(c -> c.getCin().equals(cin)).findFirst();
    }

    public Optional<Client> findByEmail(String email) {
        return store.values().stream().filter(c -> c.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        return store.values().stream().filter(c -> c.getNumeroPermis().equalsIgnoreCase(numeroPermis)).findFirst();
    }
}
