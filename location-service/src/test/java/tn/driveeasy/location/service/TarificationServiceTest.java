package tn.driveeasy.location.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.driveeasy.common.BusinessRuleException;
import tn.driveeasy.location.config.TarificationProperties;
import tn.driveeasy.location.service.TarificationService.DevisTarifaire;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests unitaires de la tarification (§5.3 du cahier des charges) :
 * vérifie la formule de prix, les coefficients catégorie, les remises fidélité et les pénalités.
 */
class TarificationServiceTest {

    private TarificationService service;

    @BeforeEach
    void setUp() {
        TarificationProperties props = new TarificationProperties();
        props.setCoefficients(Map.of(
                "ECONOMIQUE", new BigDecimal("1.00"),
                "COMPACTE", new BigDecimal("1.10"),
                "BERLINE", new BigDecimal("1.20"),
                "SUV", new BigDecimal("1.40"),
                "LUXE", new BigDecimal("1.80")
        ));
        props.setRemiseFidele(new BigDecimal("0.05"));
        props.setRemiseVip(new BigDecimal("0.10"));
        props.setCoefficientPenalite(new BigDecimal("1.5"));
        service = new TarificationService(props);
    }

    @Test
    @DisplayName("Client STANDARD + véhicule ECONOMIQUE : aucune remise, coefficient 1.0")
    void calculClientStandardEconomique() {
        DevisTarifaire devis = service.calculerDevis(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 14), // 4 jours
                new BigDecimal("100.00"),
                "ECONOMIQUE",
                "STANDARD");

        assertEquals(4L, devis.nbJours());
        assertEquals(new BigDecimal("400.00"), devis.montantBrut());
        assertEquals(new BigDecimal("0.00"), devis.montantRemise());
        assertEquals(new BigDecimal("400.00"), devis.montantTotal());
    }

    @Test
    @DisplayName("Client FIDELE (5 %) + véhicule SUV (coef 1.4)")
    void calculClientFideleSuv() {
        // 3 jours x 200 x 1.4 = 840.00 ; remise 5 % = 42.00 ; total = 798.00
        DevisTarifaire devis = service.calculerDevis(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 4),
                new BigDecimal("200.00"),
                "SUV",
                "FIDELE");

        assertEquals(new BigDecimal("840.00"), devis.montantBrut());
        assertEquals(new BigDecimal("42.00"), devis.montantRemise());
        assertEquals(new BigDecimal("798.00"), devis.montantTotal());
    }

    @Test
    @DisplayName("Client VIP (10 %) + véhicule LUXE (coef 1.8)")
    void calculClientVipLuxe() {
        // 2 jours x 300 x 1.8 = 1080.00 ; remise 10 % = 108.00 ; total = 972.00
        DevisTarifaire devis = service.calculerDevis(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                new BigDecimal("300.00"),
                "LUXE",
                "VIP");

        assertEquals(new BigDecimal("1080.00"), devis.montantBrut());
        assertEquals(new BigDecimal("108.00"), devis.montantRemise());
        assertEquals(new BigDecimal("972.00"), devis.montantTotal());
    }

    @Test
    @DisplayName("Pénalité de retard : 1,5 x prixJournalier x joursRetard")
    void calculPenaliteRetard() {
        BigDecimal penalite = service.calculerPenalite(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 13), // 3 jours de retard
                new BigDecimal("100.00"));

        assertEquals(new BigDecimal("450.00"), penalite);
    }

    @Test
    @DisplayName("Aucune pénalité si retour à la date prévue ou en avance")
    void aucunePenaliteSiRetourATemps() {
        BigDecimal penalite = service.calculerPenalite(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 10),
                new BigDecimal("100.00"));

        assertEquals(new BigDecimal("0.00"), penalite);
    }

    @Test
    @DisplayName("Date de fin antérieure à la date de début -> BusinessRuleException (400)")
    void datesIncoherentesLeventException() {
        assertThrows(BusinessRuleException.class, () -> service.calculerDevis(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 10),
                new BigDecimal("100.00"),
                "ECONOMIQUE",
                "STANDARD"));
    }
}
