package tn.driveeasy.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.driveeasy.location.dto.DepartRequest;
import tn.driveeasy.location.dto.LocationDetailResponse;
import tn.driveeasy.location.dto.LocationRequest;
import tn.driveeasy.location.dto.LocationResponse;
import tn.driveeasy.location.dto.PageResponse;
import tn.driveeasy.location.dto.RetourRequest;
import tn.driveeasy.location.dto.SimulationResponse;
import tn.driveeasy.location.dto.StatistiquesResponse;
import tn.driveeasy.location.model.StatutLocation;
import tn.driveeasy.location.service.LocationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * API REST du cycle de vie des locations.
 * Via la gateway : http://localhost:8080/api/locations/...
 */
@RestController
@RequestMapping("/locations")
@Tag(name = "Locations", description = "Cycle de vie des locations et statistiques")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Liste paginée des locations (filtres : statut, client, véhicule, période)")
    public PageResponse<LocationResponse> lister(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.rechercher(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    @GetMapping("/statistiques")
    @Operation(summary = "Indicateurs : chiffre d'affaires, taux d'occupation, top véhicules")
    public StatistiquesResponse statistiques() {
        return service.statistiques();
    }

    @GetMapping("/vehicules-occupes")
    @Operation(summary = "Identifiants des véhicules occupés sur une période (appelé par vehicule-service)")
    public List<Long> vehiculesOccupes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.getVehiculesOccupes(dateDebut, dateFin);
    }

    @GetMapping("/clients/{clientId}/actives/count")
    @Operation(summary = "Nombre de locations actives d'un client (appelé par client-service)")
    public Long countLocationsActives(@PathVariable Long clientId) {
        return service.countLocationsActivesParClient(clientId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail enrichi d'une location (client + véhicule agrégés)")
    public LocationDetailResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @PostMapping("/simulation")
    @Operation(summary = "Simulation du prix d'une location sans création")
    public SimulationResponse simuler(@Valid @RequestBody LocationRequest request) {
        return service.simuler(request);
    }

    @PostMapping
    @Operation(summary = "Création d'une réservation (vérifications métier inter-services) - 201")
    public ResponseEntity<LocationResponse> creer(@Valid @RequestBody LocationRequest request) {
        LocationResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/depart")
    @Operation(summary = "Démarrage de la location (statut EN_COURS, véhicule LOUE)")
    public LocationResponse demarrer(@PathVariable Long id,
                                     @Valid @RequestBody(required = false) DepartRequest request) {
        return service.demarrer(id, request);
    }

    @PatchMapping("/{id}/retour")
    @Operation(summary = "Retour du véhicule : kmRetour, calcul final, pénalités et fidélité")
    public LocationResponse retourner(@PathVariable Long id, @Valid @RequestBody RetourRequest request) {
        return service.retourner(id, request);
    }

    @PatchMapping("/{id}/annulation")
    @Operation(summary = "Annulation d'une réservation (RESERVEE -> ANNULEE)")
    public LocationResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}
