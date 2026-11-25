package tn.driveeasy.vehicule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.driveeasy.common.BusinessRuleException;
import tn.driveeasy.common.ConflictException;
import tn.driveeasy.common.ResourceNotFoundException;
import tn.driveeasy.vehicule.client.LocationClient;
import tn.driveeasy.vehicule.dto.StatutUpdateRequest;
import tn.driveeasy.vehicule.dto.VehiculeRequest;
import tn.driveeasy.vehicule.dto.VehiculeResponse;
import tn.driveeasy.vehicule.mapper.VehiculeMapper;
import tn.driveeasy.vehicule.model.Carburant;
import tn.driveeasy.vehicule.model.CategorieVehicule;
import tn.driveeasy.vehicule.model.StatutVehicule;
import tn.driveeasy.vehicule.model.Vehicule;
import tn.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Couche métier du parc automobile : contient toutes les règles de gestion.
 */
@Service
public class VehiculeService {

    private static final Logger log = LoggerFactory.getLogger(VehiculeService.class);

    private final VehiculeRepository repository;
    private final LocationClient locationClient;

    public VehiculeService(VehiculeRepository repository, LocationClient locationClient) {
        this.repository = repository;
        this.locationClient = locationClient;
    }

    /** Liste filtrée (tous les filtres sont facultatifs et cumulables). */
    public List<VehiculeResponse> rechercher(CategorieVehicule categorie, Carburant carburant,
                                             StatutVehicule statut, BigDecimal prixMax) {
        return repository.findAll().stream()
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    public VehiculeResponse getById(Long id) {
        return VehiculeMapper.toResponse(findOrThrow(id));
    }

    /** Création : immatriculation unique (409) et année cohérente (400). */
    public synchronized VehiculeResponse creer(VehiculeRequest request) {
        verifierAnnee(request.annee());
        String immat = VehiculeMapper.normaliserImmatriculation(request.immatriculation());
        repository.findByImmatriculation(immat).ifPresent(v -> {
            throw new ConflictException("L'immatriculation " + immat + " existe déjà");
        });
        if (request.statut() == StatutVehicule.LOUE) {
            throw new BusinessRuleException("Un véhicule ne peut pas être créé au statut LOUE");
        }
        Vehicule saved = repository.save(VehiculeMapper.toEntity(request));
        log.info("Véhicule créé : id={}, immatriculation={}", saved.getId(), saved.getImmatriculation());
        return VehiculeMapper.toResponse(saved);
    }

    /** Modification complète. Le statut LOUE est piloté uniquement par location-service. */
    public synchronized VehiculeResponse modifier(Long id, VehiculeRequest request) {
        Vehicule vehicule = findOrThrow(id);
        verifierAnnee(request.annee());
        String immat = VehiculeMapper.normaliserImmatriculation(request.immatriculation());
        repository.findByImmatriculation(immat)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("L'immatriculation " + immat + " est déjà utilisée par un autre véhicule");
                });

        if (request.statut() != null && request.statut() != vehicule.getStatut()) {
            if (vehicule.getStatut() == StatutVehicule.LOUE || request.statut() == StatutVehicule.LOUE) {
                throw new ConflictException("Le statut LOUE est géré par le cycle de location (départ / retour)");
            }
            vehicule.setStatut(request.statut());
        }
        VehiculeMapper.updateEntity(vehicule, request);
        return VehiculeMapper.toResponse(repository.save(vehicule));
    }

    /** Suppression refusée si le véhicule est actuellement loué. */
    public void supprimer(Long id) {
        Vehicule vehicule = findOrThrow(id);
        if (vehicule.getStatut() == StatutVehicule.LOUE) {
            throw new ConflictException("Suppression impossible : le véhicule " + vehicule.getImmatriculation() + " est actuellement loué");
        }
        repository.deleteById(id);
        log.info("Véhicule supprimé : id={}", id);
    }

    /**
     * Changement de statut (appelé par location-service au départ / retour).
     * Transitions interdites : LOUE -> LOUE, MAINTENANCE -> LOUE, LOUE -> MAINTENANCE.
     */
    public synchronized VehiculeResponse changerStatut(Long id, StatutUpdateRequest request) {
        Vehicule vehicule = findOrThrow(id);
        StatutVehicule actuel = vehicule.getStatut();
        StatutVehicule cible = request.statut();

        if (cible == StatutVehicule.LOUE && actuel != StatutVehicule.DISPONIBLE) {
            throw new ConflictException("Le véhicule " + vehicule.getImmatriculation()
                    + " ne peut pas être loué (statut actuel : " + actuel + ")");
        }
        if (cible == StatutVehicule.MAINTENANCE && actuel == StatutVehicule.LOUE) {
            throw new ConflictException("Un véhicule loué ne peut pas passer en maintenance avant son retour");
        }
        if (request.kilometrage() != null) {
            if (request.kilometrage() < vehicule.getKilometrage()) {
                throw new BusinessRuleException("Le kilométrage ne peut pas diminuer (actuel : "
                        + vehicule.getKilometrage() + " km)");
            }
            vehicule.setKilometrage(request.kilometrage());
        }
        vehicule.setStatut(cible);
        log.info("Statut du véhicule {} : {} -> {}", id, actuel, cible);
        return VehiculeMapper.toResponse(repository.save(vehicule));
    }

    /**
     * Véhicules disponibles sur une période : ni en maintenance, ni réservés / loués
     * sur la période (information détenue par location-service, appel inter-services).
     */
    public List<VehiculeResponse> disponibles(LocalDate dateDebut, LocalDate dateFin, CategorieVehicule categorie) {
        if (dateFin.isBefore(dateDebut)) {
            throw new BusinessRuleException("La date de fin doit être postérieure ou égale à la date de début");
        }
        Set<Long> occupes = new HashSet<>(locationClient.getVehiculesOccupes(dateDebut, dateFin));
        return repository.findAll().stream()
                .filter(v -> v.getStatut() != StatutVehicule.MAINTENANCE)
                .filter(v -> !occupes.contains(v.getId()))
                .filter(v -> categorie == null || v.getCategorie() == categorie)
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    private Vehicule findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Véhicule", id));
    }

    private void verifierAnnee(int annee) {
        int anneeCourante = Year.now().getValue();
        if (annee > anneeCourante) {
            throw new BusinessRuleException("L'année (" + annee + ") ne peut pas dépasser l'année courante (" + anneeCourante + ")");
        }
    }
}
