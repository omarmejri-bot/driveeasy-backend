package tn.driveeasy.location.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.driveeasy.common.BusinessRuleException;
import tn.driveeasy.common.ConflictException;
import tn.driveeasy.common.ResourceNotFoundException;
import tn.driveeasy.location.client.ClientServiceClient;
import tn.driveeasy.location.client.VehiculeServiceClient;
import tn.driveeasy.location.config.FideliteProperties;
import tn.driveeasy.location.dto.DepartRequest;
import tn.driveeasy.location.dto.LocationDetailResponse;
import tn.driveeasy.location.dto.LocationRequest;
import tn.driveeasy.location.dto.LocationResponse;
import tn.driveeasy.location.dto.PageResponse;
import tn.driveeasy.location.dto.RemoteDtos.CategorieUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.ClientSummary;
import tn.driveeasy.location.dto.RemoteDtos.EligibiliteDto;
import tn.driveeasy.location.dto.RemoteDtos.StatutVehiculeUpdateDto;
import tn.driveeasy.location.dto.RemoteDtos.VehiculeSummary;
import tn.driveeasy.location.dto.RetourRequest;
import tn.driveeasy.location.dto.SimulationResponse;
import tn.driveeasy.location.dto.StatistiquesResponse;
import tn.driveeasy.location.dto.StatistiquesResponse.TopVehiculeStat;
import tn.driveeasy.location.mapper.LocationMapper;
import tn.driveeasy.location.model.Location;
import tn.driveeasy.location.model.StatutLocation;
import tn.driveeasy.location.repository.LocationRepository;
import tn.driveeasy.location.service.TarificationService.DevisTarifaire;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrateur du cycle de vie des locations :
 * <ul>
 *   <li>contrôle d'éligibilité client + disponibilité véhicule (appels inter-services) ;</li>
 *   <li>calcul tarifaire et pénalités ;</li>
 *   <li>synchronisation du statut du véhicule (LOUE au départ, DISPONIBLE au retour) ;</li>
 *   <li>compensation en cas d'échec d'un appel inter-services (cohérence transactionnelle) ;</li>
 *   <li>promotion automatique de la catégorie de fidélité (FIDELE à 5, VIP à 15 locations terminées).</li>
 * </ul>
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final ClientServiceClient clientClient;
    private final VehiculeServiceClient vehiculeClient;
    private final TarificationService tarificationService;
    private final FideliteProperties fideliteProps;

    public LocationService(LocationRepository repository,
                           ClientServiceClient clientClient,
                           VehiculeServiceClient vehiculeClient,
                           TarificationService tarificationService,
                           FideliteProperties fideliteProps) {
        this.repository = repository;
        this.clientClient = clientClient;
        this.vehiculeClient = vehiculeClient;
        this.tarificationService = tarificationService;
        this.fideliteProps = fideliteProps;
    }

    /** Liste paginée avec filtres cumulables (statut, client, véhicule, période). */
    public PageResponse<LocationResponse> rechercher(StatutLocation statut, Long clientId, Long vehiculeId,
                                                     LocalDate dateDebut, LocalDate dateFin,
                                                     int page, int size) {
        List<LocationResponse> filtered = repository.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> dateDebut == null || !l.getDateFinPrevue().isBefore(dateDebut))
                .filter(l -> dateFin == null || !l.getDateDebut().isAfter(dateFin))
                .sorted(Comparator.comparing(Location::getId).reversed())
                .map(LocationMapper::toResponse)
                .toList();

        int safeSize = Math.max(1, size);
        int safePage = Math.max(0, page);
        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        int totalPages = (int) Math.ceil((double) filtered.size() / safeSize);

        return new PageResponse<>(filtered.subList(from, to), safePage, safeSize, filtered.size(), totalPages);
    }

    /**
     * Détail enrichi : agrège la location avec les données client-service et vehicule-service.
     * Si l'un des services est indisponible en lecture, on renvoie la location avec donneesPartielles=true.
     */
    public LocationDetailResponse getDetail(Long id) {
        Location location = findOrThrow(id);
        boolean partiel = false;
        ClientSummary client = null;
        VehiculeSummary vehicule = null;
        try {
            client = clientClient.getClient(location.getClientId());
        } catch (Exception ex) {
            log.warn("Lecture client {} impossible (dégradation gracieuse) : {}", location.getClientId(), ex.getMessage());
            partiel = true;
        }
        try {
            vehicule = vehiculeClient.getVehicule(location.getVehiculeId());
        } catch (Exception ex) {
            log.warn("Lecture véhicule {} impossible (dégradation gracieuse) : {}", location.getVehiculeId(), ex.getMessage());
            partiel = true;
        }
        return new LocationDetailResponse(LocationMapper.toResponse(location), client, vehicule, partiel);
    }

    /** Simulation tarifaire sans aucune écriture en mémoire. */
    public SimulationResponse simuler(LocationRequest req) {
        verifierDates(req.dateDebut(), req.dateFinPrevue());
        EligibiliteDto elig = clientClient.verifierEligibilite(req.clientId());
        VehiculeSummary vehicule = vehiculeClient.getVehicule(req.vehiculeId());

        boolean dispo = !"MAINTENANCE".equalsIgnoreCase(vehicule.statut())
                && !chevaucheLocationActive(req.vehiculeId(), req.dateDebut(), req.dateFinPrevue(), null);
        if ("LOUE".equalsIgnoreCase(vehicule.statut()) && !req.dateDebut().isAfter(LocalDate.now())) {
            dispo = false;
        }

        DevisTarifaire devis = tarificationService.calculerDevis(
                req.dateDebut(), req.dateFinPrevue(),
                vehicule.prixJournalier(), vehicule.categorie(), elig.categorieClient());

        return new SimulationResponse(
                req.clientId(), elig.nomComplet(), elig.categorieClient(), elig.eligible(),
                req.vehiculeId(), vehicule.marque() + " " + vehicule.modele(), vehicule.categorie(), dispo,
                req.dateDebut(), req.dateFinPrevue(),
                devis.nbJours(), devis.prixJournalierBase(), devis.coefficientCategorie(),
                devis.prixJournalierApplique(), devis.montantBrut(),
                devis.tauxRemise(), devis.montantRemise(), devis.montantTotal(),
                devis.penaliteParJourRetard());
    }

    /**
     * Création d'une réservation (synchronized pour éviter une double réservation concurrente).
     * L'enregistrement n'a lieu QUE si toutes les vérifications inter-services réussissent.
     */
    public synchronized LocationResponse creer(LocationRequest req) {
        verifierDates(req.dateDebut(), req.dateFinPrevue());

        // 1) Contrôle d'éligibilité auprès de client-service
        EligibiliteDto elig = clientClient.verifierEligibilite(req.clientId());
        if (!elig.eligible()) {
            throw new BusinessRuleException("Client non éligible à la location : " + String.join(", ", elig.motifs()));
        }

        // 2) Vérification du véhicule auprès de vehicule-service
        VehiculeSummary vehicule = vehiculeClient.getVehicule(req.vehiculeId());
        if ("MAINTENANCE".equalsIgnoreCase(vehicule.statut())) {
            throw new ConflictException("Le véhicule " + vehicule.immatriculation() + " est en maintenance");
        }
        if ("LOUE".equalsIgnoreCase(vehicule.statut()) && !req.dateDebut().isAfter(LocalDate.now())) {
            throw new ConflictException("Le véhicule " + vehicule.immatriculation() + " est actuellement loué");
        }

        // 3) Vérification d'absence de chevauchement sur la période
        if (chevaucheLocationActive(req.vehiculeId(), req.dateDebut(), req.dateFinPrevue(), null)) {
            throw new ConflictException("Le véhicule " + vehicule.immatriculation()
                    + " est déjà réservé ou loué sur la période du " + req.dateDebut() + " au " + req.dateFinPrevue());
        }

        // 4) Calcul et gel du tarif
        DevisTarifaire devis = tarificationService.calculerDevis(
                req.dateDebut(), req.dateFinPrevue(),
                vehicule.prixJournalier(), vehicule.categorie(), elig.categorieClient());

        Location location = new Location();
        location.setClientId(req.clientId());
        location.setVehiculeId(req.vehiculeId());
        location.setClientNomComplet(elig.nomComplet());
        location.setVehiculeLibelle(vehicule.marque() + " " + vehicule.modele());
        location.setVehiculeImmatriculation(vehicule.immatriculation());
        location.setDateDebut(req.dateDebut());
        location.setDateFinPrevue(req.dateFinPrevue());
        location.setKmDepart(vehicule.kilometrage());
        location.setPrixJournalierApplique(devis.prixJournalierBase());
        location.setCoefficientCategorie(devis.coefficientCategorie());
        location.setTauxRemise(devis.tauxRemise());
        location.setMontantRemise(devis.montantRemise());
        location.setMontantTotal(devis.montantTotal());
        location.setPenalite(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        location.setStatut(StatutLocation.RESERVEE);
        location.setDateCreation(LocalDateTime.now());

        Location saved = repository.save(location);
        log.info("Réservation créée : id={}, client={}, véhicule={}, montant={}",
                saved.getId(), saved.getClientId(), saved.getVehiculeId(), saved.getMontantTotal());
        return LocationMapper.toResponse(saved);
    }

    /**
     * Démarrage de la location (RESERVEE -> EN_COURS) + passage du véhicule à LOUE.
     * En cas d'échec après le PATCH véhicule, une action de compensation remet le véhicule DISPONIBLE.
     */
    public synchronized LocationResponse demarrer(Long id, DepartRequest request) {
        Location location = findOrThrow(id);
        if (location.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une location RESERVEE peut démarrer (statut actuel : " + location.getStatut() + ")");
        }

        VehiculeSummary vehicule = vehiculeClient.getVehicule(location.getVehiculeId());
        int kmDepart = (request != null && request.kmDepart() != null) ? request.kmDepart() : vehicule.kilometrage();
        if (kmDepart < vehicule.kilometrage()) {
            throw new BusinessRuleException("Le kilométrage de départ (" + kmDepart
                    + ") ne peut pas être inférieur au kilométrage du véhicule (" + vehicule.kilometrage() + " km)");
        }

        // 1) Mise à jour du statut du véhicule dans vehicule-service
        vehiculeClient.changerStatut(location.getVehiculeId(), new StatutVehiculeUpdateDto("LOUE", kmDepart));

        try {
            // 2) Mise à jour locale de la location
            location.setKmDepart(kmDepart);
            location.setStatut(StatutLocation.EN_COURS);
            repository.save(location);
            log.info("Location {} démarrée (kmDepart={})", id, kmDepart);
            return LocationMapper.toResponse(location);
        } catch (RuntimeException ex) {
            // Compensation : on remet le véhicule DISPONIBLE
            log.error("Échec local lors du démarrage de la location {} -> compensation véhicule DISPONIBLE", id, ex);
            compenserStatutVehicule(location.getVehiculeId(), "DISPONIBLE", vehicule.kilometrage());
            throw ex;
        }
    }

    /**
     * Retour du véhicule (EN_COURS -> TERMINEE) :
     * calcul des pénalités de retard, remise du véhicule à DISPONIBLE et mise à jour de la fidélité client.
     * Si vehicule-service échoue, la location reste EN_COURS (cohérente et réessayable).
     */
    public synchronized LocationResponse retourner(Long id, RetourRequest request) {
        Location location = findOrThrow(id);
        if (location.getStatut() != StatutLocation.EN_COURS) {
            throw new ConflictException("Seule une location EN_COURS peut être retournée (statut actuel : " + location.getStatut() + ")");
        }

        int kmDepart = location.getKmDepart() != null ? location.getKmDepart() : 0;
        if (request.kmRetour() < kmDepart) {
            throw new BusinessRuleException("Le kilométrage de retour (" + request.kmRetour()
                    + " km) doit être supérieur ou égal au kilométrage de départ (" + kmDepart + " km)");
        }

        LocalDate dateRetour = request.dateRetourEffectif() != null ? request.dateRetourEffectif() : LocalDate.now();
        if (dateRetour.isBefore(location.getDateDebut())) {
            throw new BusinessRuleException("La date de retour effectif (" + dateRetour
                    + ") ne peut pas être antérieure à la date de début (" + location.getDateDebut() + ")");
        }

        BigDecimal penalite = tarificationService.calculerPenalite(
                location.getDateFinPrevue(), dateRetour, location.getPrixJournalierApplique());

        // 1) Libération du véhicule dans vehicule-service (si cela échoue -> exception 503, location inchangée)
        vehiculeClient.changerStatut(location.getVehiculeId(),
                new StatutVehiculeUpdateDto("DISPONIBLE", request.kmRetour()));

        try {
            // 2) Clôture locale de la location
            location.setKmRetour(request.kmRetour());
            location.setDateRetourEffectif(dateRetour);
            location.setPenalite(penalite);
            location.setStatut(StatutLocation.TERMINEE);
            repository.save(location);
        } catch (RuntimeException ex) {
            compenserStatutVehicule(location.getVehiculeId(), "LOUE", kmDepart);
            throw ex;
        }

        // 3) Mise à jour automatique de la catégorie de fidélité du client (5 -> FIDELE, 15 -> VIP)
        mettreAJourFideliteClient(location.getClientId());

        log.info("Location {} terminée : kmRetour={}, pénalité={}", id, request.kmRetour(), penalite);
        return LocationMapper.toResponse(location);
    }

    /** Annulation d'une réservation (uniquement tant qu'elle est RESERVEE). */
    public synchronized LocationResponse annuler(Long id) {
        Location location = findOrThrow(id);
        if (location.getStatut() != StatutLocation.RESERVEE) {
            throw new ConflictException("Seule une réservation non démarrée (RESERVEE) peut être annulée (statut actuel : "
                    + location.getStatut() + ")");
        }
        location.setStatut(StatutLocation.ANNULEE);
        repository.save(location);
        log.info("Location {} annulée", id);
        return LocationMapper.toResponse(location);
    }

    /** Utilisé par vehicule-service (GET /vehicules/disponibles) pour exclure les véhicules occupés. */
    public List<Long> getVehiculesOccupes(LocalDate dateDebut, LocalDate dateFin) {
        return repository.findAll().stream()
                .filter(Location::isActive)
                .filter(l -> periodesChevauchent(l.getDateDebut(), l.getDateFinPrevue(), dateDebut, dateFin))
                .map(Location::getVehiculeId)
                .distinct()
                .toList();
    }

    /** Utilisé par client-service avant suppression d'un client. */
    public long countLocationsActivesParClient(Long clientId) {
        return repository.findAll().stream()
                .filter(Location::isActive)
                .filter(l -> l.getClientId().equals(clientId))
                .count();
    }

    /** Indicateurs pour le tableau de bord : CA, taux d'occupation, top véhicules. */
    public StatistiquesResponse statistiques() {
        List<Location> toutes = repository.findAll();
        YearMonth moisCourant = YearMonth.now();

        BigDecimal caTotal = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .map(Location::getMontantAPayer)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal caMois = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                .filter(l -> YearMonth.from(l.getDateDebut()).equals(moisCourant))
                .map(Location::getMontantAPayer)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPenalites = toutes.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .map(Location::getPenalite)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long enCours = toutes.stream().filter(l -> l.getStatut() == StatutLocation.EN_COURS).count();
        long reservees = toutes.stream().filter(l -> l.getStatut() == StatutLocation.RESERVEE).count();
        long terminees = toutes.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();
        long annulees = toutes.stream().filter(l -> l.getStatut() == StatutLocation.ANNULEE).count();

        Map<String, Long> parStatut = new LinkedHashMap<>();
        parStatut.put("RESERVEE", reservees);
        parStatut.put("EN_COURS", enCours);
        parStatut.put("TERMINEE", terminees);
        parStatut.put("ANNULEE", annulees);

        // Parc automobile (via vehicule-service ; repli sur les locations en cours si indisponible)
        long totalVeh = 0;
        long dispo = 0;
        long loues = enCours;
        long maint = 0;
        try {
            List<VehiculeSummary> parc = vehiculeClient.listerTous();
            totalVeh = parc.size();
            dispo = parc.stream().filter(v -> "DISPONIBLE".equalsIgnoreCase(v.statut())).count();
            loues = parc.stream().filter(v -> "LOUE".equalsIgnoreCase(v.statut())).count();
            maint = parc.stream().filter(v -> "MAINTENANCE".equalsIgnoreCase(v.statut())).count();
        } catch (Exception ex) {
            log.warn("Impossible de lire le parc pour les statistiques : {}", ex.getMessage());
        }

        long exploitables = Math.max(1, totalVeh - maint);
        double tauxOccupation = totalVeh > 0
                ? Math.round((loues * 10000.0) / exploitables) / 100.0
                : 0.0;

        // Top 5 des véhicules les plus loués (hors locations annulées)
        List<TopVehiculeStat> top = toutes.stream()
                .filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId))
                .entrySet().stream()
                .map(e -> {
                    List<Location> locs = e.getValue();
                    Location sample = locs.getFirst();
                    BigDecimal ca = locs.stream()
                            .filter(l -> l.getStatut() == StatutLocation.TERMINEE || l.getStatut() == StatutLocation.EN_COURS)
                            .map(Location::getMontantAPayer)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    return new TopVehiculeStat(e.getKey(), sample.getVehiculeLibelle(),
                            sample.getVehiculeImmatriculation(), locs.size(), ca);
                })
                .sorted(Comparator.comparingLong(TopVehiculeStat::nombreLocations).reversed()
                        .thenComparing(TopVehiculeStat::chiffreAffairesGenere, Comparator.reverseOrder()))
                .limit(5)
                .toList();

        return new StatistiquesResponse(caTotal, caMois, totalPenalites, tauxOccupation,
                totalVeh, dispo, loues, maint,
                toutes.size(), enCours, reservees, terminees, annulees,
                parStatut, top);
    }

    /**
     * Règle de fidélité (§5.2) :
     * un client passe automatiquement en FIDELE après 5 locations terminées, VIP après 15.
     */
    private void mettreAJourFideliteClient(Long clientId) {
        long nbTerminees = repository.findAll().stream()
                .filter(l -> l.getClientId().equals(clientId))
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .count();

        String nouvelleCategorie = null;
        if (nbTerminees >= fideliteProps.getSeuilVip()) {
            nouvelleCategorie = "VIP";
        } else if (nbTerminees >= fideliteProps.getSeuilFidele()) {
            nouvelleCategorie = "FIDELE";
        }
        if (nouvelleCategorie == null) {
            return;
        }
        try {
            ClientSummary client = clientClient.getClient(clientId);
            if (rangCategorie(nouvelleCategorie) > rangCategorie(client.categorieClient())) {
                clientClient.changerCategorie(clientId, new CategorieUpdateDto(nouvelleCategorie));
                log.info("Client {} promu en catégorie {} ({} locations terminées)",
                        clientId, nouvelleCategorie, nbTerminees);
            }
        } catch (Exception ex) {
            // La clôture de la location reste valide même si la promotion de fidélité échoue momentanément
            log.warn("Mise à jour de la catégorie de fidélité du client {} reportée : {}", clientId, ex.getMessage());
        }
    }

    private int rangCategorie(String cat) {
        if (cat == null) return 0;
        return switch (cat.toUpperCase()) {
            case "FIDELE" -> 1;
            case "VIP" -> 2;
            default -> 0;
        };
    }

    private void compenserStatutVehicule(Long vehiculeId, String statutCible, Integer km) {
        try {
            vehiculeClient.changerStatut(vehiculeId, new StatutVehiculeUpdateDto(statutCible, km));
        } catch (Exception compEx) {
            log.error("Échec de la compensation sur le véhicule {} : {}", vehiculeId, compEx.getMessage());
        }
    }

    private boolean chevaucheLocationActive(Long vehiculeId, LocalDate debut, LocalDate fin, Long excludeId) {
        return repository.findAll().stream()
                .filter(Location::isActive)
                .filter(l -> l.getVehiculeId().equals(vehiculeId))
                .filter(l -> !l.getId().equals(excludeId))
                .anyMatch(l -> periodesChevauchent(l.getDateDebut(), l.getDateFinPrevue(), debut, fin));
    }

    private static boolean periodesChevauchent(LocalDate d1, LocalDate f1, LocalDate d2, LocalDate f2) {
        return !d1.isAfter(f2) && !d2.isAfter(f1);
    }

    private static void verifierDates(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BusinessRuleException("La date de fin prévue (" + fin
                    + ") doit être postérieure ou égale à la date de début (" + debut + ")");
        }
    }

    private Location findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }
}
