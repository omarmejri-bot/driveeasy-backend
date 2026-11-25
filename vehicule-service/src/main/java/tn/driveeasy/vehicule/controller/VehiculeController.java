package tn.driveeasy.vehicule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.driveeasy.vehicule.dto.StatutUpdateRequest;
import tn.driveeasy.vehicule.dto.VehiculeRequest;
import tn.driveeasy.vehicule.dto.VehiculeResponse;
import tn.driveeasy.vehicule.model.Carburant;
import tn.driveeasy.vehicule.model.CategorieVehicule;
import tn.driveeasy.vehicule.model.StatutVehicule;
import tn.driveeasy.vehicule.service.VehiculeService;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * API REST du parc automobile.
 * Via la gateway : http://localhost:8080/api/vehicules/...
 */
@RestController
@RequestMapping("/vehicules")
@Tag(name = "Véhicules", description = "Gestion du parc automobile")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Liste des véhicules (filtres : catégorie, carburant, statut, prix max)")
    public List<VehiculeResponse> lister(@RequestParam(required = false) CategorieVehicule categorie,
                                         @RequestParam(required = false) Carburant carburant,
                                         @RequestParam(required = false) StatutVehicule statut,
                                         @RequestParam(required = false) BigDecimal prixMax) {
        return service.rechercher(categorie, carburant, statut, prixMax);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Véhicules disponibles sur une période")
    public List<VehiculeResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) CategorieVehicule categorie) {
        return service.disponibles(dateDebut, dateFin, categorie);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un véhicule")
    public VehiculeResponse detail(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Ajout d'un véhicule (201 + en-tête Location)")
    public ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest request) {
        VehiculeResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modification d'un véhicule")
    public VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Suppression (refusée si le véhicule est loué) - 204")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changement de statut (utilisé par location-service)")
    public VehiculeResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutUpdateRequest request) {
        return service.changerStatut(id, request);
    }
}
