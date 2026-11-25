package tn.driveeasy.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Propriétés métier de tarification, externalisées dans le Config Server
 * (config-repo/location-service.yml, préfixe driveeasy.tarification).
 * Rechargeables à chaud via POST /actuator/refresh.
 */
@ConfigurationProperties(prefix = "driveeasy.tarification")
public class TarificationProperties {

    /** Coefficient multiplicateur par catégorie de véhicule (ECONOMIQUE, SUV, LUXE...). */
    private Map<String, BigDecimal> coefficients = new HashMap<>();
    /** Remise accordée aux clients FIDELE (0.05 = 5 %). */
    private BigDecimal remiseFidele = new BigDecimal("0.05");
    /** Remise accordée aux clients VIP (0.10 = 10 %). */
    private BigDecimal remiseVip = new BigDecimal("0.10");
    /** Pénalité par jour de retard = coefficientPenalite x prix journalier. */
    private BigDecimal coefficientPenalite = new BigDecimal("1.5");

    /** Recherche insensible à la casse ; 1.0 si la catégorie n'est pas configurée. */
    public BigDecimal coefficientPour(String categorie) {
        if (categorie == null) {
            return BigDecimal.ONE;
        }
        return coefficients.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(categorie))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(BigDecimal.ONE);
    }

    public Map<String, BigDecimal> getCoefficients() { return coefficients; }
    public void setCoefficients(Map<String, BigDecimal> coefficients) { this.coefficients = coefficients; }

    public BigDecimal getRemiseFidele() { return remiseFidele; }
    public void setRemiseFidele(BigDecimal remiseFidele) { this.remiseFidele = remiseFidele; }

    public BigDecimal getRemiseVip() { return remiseVip; }
    public void setRemiseVip(BigDecimal remiseVip) { this.remiseVip = remiseVip; }

    public BigDecimal getCoefficientPenalite() { return coefficientPenalite; }
    public void setCoefficientPenalite(BigDecimal coefficientPenalite) { this.coefficientPenalite = coefficientPenalite; }
}
