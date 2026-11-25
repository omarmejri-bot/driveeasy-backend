package tn.driveeasy.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import tn.driveeasy.client.dto.CategorieUpdateRequest;
import tn.driveeasy.client.dto.ClientRequest;
import tn.driveeasy.client.dto.ClientResponse;
import tn.driveeasy.client.dto.EligibiliteResponse;
import tn.driveeasy.client.model.CategorieClient;
import tn.driveeasy.client.service.ClientService;

import java.net.URI;
import java.util.List;

/**
 * API REST des clients.
 * Via la gateway : http://localhost:8080/api/clients/...
 */
@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Gestion des clients et de leur éligibilité")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Recherche par nom / CIN / email (paramètre q) et catégorie")
    public List<ClientResponse> rechercher(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) CategorieClient categorie) {
        return service.rechercher(q, categorie);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un client")
    public ClientResponse detail(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Création d'un client (201 + en-tête Location)")
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
        ClientResponse created = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modification d'un client")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Suppression (refusée si location active) - 204")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibilite")
    @Operation(summary = "Le client peut-il louer ? (âge ≥ 21 ans, permis valide)")
    public EligibiliteResponse eligibilite(@PathVariable Long id) {
        return service.eligibilite(id);
    }

    @PatchMapping("/{id}/categorie")
    @Operation(summary = "Mise à jour de la catégorie de fidélité")
    public ClientResponse changerCategorie(@PathVariable Long id, @Valid @RequestBody CategorieUpdateRequest request) {
        return service.changerCategorie(id, request);
    }
}
