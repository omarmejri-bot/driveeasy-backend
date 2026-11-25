package tn.driveeasy.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.driveeasy.client.client.LocationClient;
import tn.driveeasy.client.dto.CategorieUpdateRequest;
import tn.driveeasy.client.dto.ClientRequest;
import tn.driveeasy.client.dto.ClientResponse;
import tn.driveeasy.client.dto.EligibiliteResponse;
import tn.driveeasy.client.mapper.ClientMapper;
import tn.driveeasy.client.model.CategorieClient;
import tn.driveeasy.client.model.Client;
import tn.driveeasy.client.repository.ClientRepository;
import tn.driveeasy.common.ConflictException;
import tn.driveeasy.common.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Couche métier des clients : unicité (CIN, email, permis), éligibilité, fidélité.
 */
@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository repository;
    private final LocationClient locationClient;
    /** Âge minimum pour louer : propriété métier externalisée dans le Config Server. */
    private final int ageMinimum;

    public ClientService(ClientRepository repository, LocationClient locationClient,
                         @Value("${driveeasy.eligibilite.age-minimum:21}") int ageMinimum) {
        this.repository = repository;
        this.locationClient = locationClient;
        this.ageMinimum = ageMinimum;
    }

    /** Recherche plein texte sur nom, prénom, CIN ou email (+ filtre catégorie facultatif). */
    public List<ClientResponse> rechercher(String q, CategorieClient categorie) {
        String terme = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(c -> terme.isEmpty()
                        || c.getNom().toLowerCase(Locale.ROOT).contains(terme)
                        || c.getPrenom().toLowerCase(Locale.ROOT).contains(terme)
                        || c.getCin().contains(terme)
                        || c.getEmail().toLowerCase(Locale.ROOT).contains(terme))
                .filter(c -> categorie == null || c.getCategorieClient() == categorie)
                .map(ClientMapper::toResponse)
                .toList();
    }

    public ClientResponse getById(Long id) {
        return ClientMapper.toResponse(findOrThrow(id));
    }

    public synchronized ClientResponse creer(ClientRequest request) {
        verifierUnicite(request, null);
        Client client = new Client();
        ClientMapper.updateEntity(client, request);
        client.setCategorieClient(request.categorieClient() != null ? request.categorieClient() : CategorieClient.STANDARD);
        client.setDateInscription(LocalDate.now());
        Client saved = repository.save(client);
        log.info("Client créé : id={}, cin={}", saved.getId(), saved.getCin());
        return ClientMapper.toResponse(saved);
    }

    public synchronized ClientResponse modifier(Long id, ClientRequest request) {
        Client client = findOrThrow(id);
        verifierUnicite(request, id);
        ClientMapper.updateEntity(client, request);
        if (request.categorieClient() != null) {
            client.setCategorieClient(request.categorieClient());
        }
        return ClientMapper.toResponse(repository.save(client));
    }

    /** Suppression refusée si le client a une location active (appel à location-service). */
    public void supprimer(Long id) {
        Client client = findOrThrow(id);
        Long actives = locationClient.countLocationsActives(id);
        if (actives != null && actives > 0) {
            throw new ConflictException("Suppression impossible : " + client.getPrenom() + " " + client.getNom()
                    + " a " + actives + " location(s) active(s)");
        }
        repository.deleteById(id);
        log.info("Client supprimé : id={}", id);
    }

    /** Éligible = âge ≥ âge minimum (21 ans) ET permis non expiré. */
    public EligibiliteResponse eligibilite(Long id) {
        Client client = findOrThrow(id);
        int age = ClientMapper.age(client);
        boolean permisValide = ClientMapper.permisValide(client);
        List<String> motifs = new ArrayList<>();
        if (age < ageMinimum) {
            motifs.add("Âge insuffisant : " + age + " ans (minimum " + ageMinimum + " ans)");
        }
        if (!permisValide) {
            motifs.add("Permis de conduire expiré depuis le " + client.getDateExpirationPermis());
        }
        return new EligibiliteResponse(client.getId(), client.getPrenom() + " " + client.getNom(),
                motifs.isEmpty(), age, permisValide, client.getCategorieClient(), motifs);
    }

    /** Mise à jour de la catégorie de fidélité (appelée par location-service après un retour). */
    public ClientResponse changerCategorie(Long id, CategorieUpdateRequest request) {
        Client client = findOrThrow(id);
        CategorieClient ancienne = client.getCategorieClient();
        client.setCategorieClient(request.categorieClient());
        log.info("Catégorie du client {} : {} -> {}", id, ancienne, request.categorieClient());
        return ClientMapper.toResponse(repository.save(client));
    }

    private void verifierUnicite(ClientRequest r, Long idCourant) {
        repository.findByCin(r.cin().trim())
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("Le CIN " + r.cin() + " est déjà utilisé"); });
        repository.findByEmail(r.email().trim())
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("L'email " + r.email() + " est déjà utilisé"); });
        repository.findByNumeroPermis(r.numeroPermis().trim())
                .filter(c -> !c.getId().equals(idCourant))
                .ifPresent(c -> { throw new ConflictException("Le numéro de permis " + r.numeroPermis() + " est déjà utilisé"); });
    }

    private Client findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }
}
