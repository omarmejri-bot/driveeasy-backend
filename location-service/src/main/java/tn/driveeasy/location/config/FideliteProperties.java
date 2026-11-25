package tn.driveeasy.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seuils de fidélité externalisés (driveeasy.fidelite) :
 * FIDELE après 5 locations terminées, VIP après 15.
 */
@ConfigurationProperties(prefix = "driveeasy.fidelite")
public class FideliteProperties {

    private int seuilFidele = 5;
    private int seuilVip = 15;

    public int getSeuilFidele() { return seuilFidele; }
    public void setSeuilFidele(int seuilFidele) { this.seuilFidele = seuilFidele; }

    public int getSeuilVip() { return seuilVip; }
    public void setSeuilVip(int seuilVip) { this.seuilVip = seuilVip; }
}
