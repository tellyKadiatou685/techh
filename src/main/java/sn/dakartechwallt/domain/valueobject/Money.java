package sn.dakartechwallt.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object représentant une somme d'argent avec sa devise.
 *
 * Règles métier :
 *  - Le montant doit être strictement positif à la création.
 *  - Deux Money ne peuvent être additionnés que s'ils partagent la même devise.
 *  - Immuable : toute opération retourne une nouvelle instance.
 */
public record Money(BigDecimal montant, String devise) {

    // ── Constructeur compact (validation à la "naissance") ──────────────────
    public Money {
        Objects.requireNonNull(montant, "Le montant ne peut pas être null");
        Objects.requireNonNull(devise,  "La devise ne peut pas être null");

        if (devise.isBlank()) {
            throw new IllegalArgumentException("La devise ne peut pas être vide");
        }
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Le montant doit être strictement positif. Valeur reçue : " + montant
            );
        }

        // Normalisation : 2 décimales, devise en majuscules
        montant = montant.setScale(2, RoundingMode.HALF_UP);
        devise  = devise.toUpperCase().strip();
    }

    // ── Fabrique statique (Ubiquitous Language) ─────────────────────────────
    public static Money de(BigDecimal montant, String devise) {
        return new Money(montant, devise);
    }

    public static Money de(double montant, String devise) {
        return new Money(BigDecimal.valueOf(montant), devise);
    }

    // ── Comportement riche ───────────────────────────────────────────────────

    /**
     * Additionne deux montants de même devise.
     *
     * @throws IllegalArgumentException si les devises diffèrent
     */
    public Money add(Money other) {
        Objects.requireNonNull(other, "L'autre montant ne peut pas être null");
        verifierDeviseCompatible(other);
        return new Money(this.montant.add(other.montant), this.devise);
    }

    /**
     * Soustrait un montant de même devise.
     *
     * @throws IllegalArgumentException si les devises diffèrent ou si le résultat est négatif
     */
    public Money soustraire(Money other) {
        Objects.requireNonNull(other, "Le montant à soustraire ne peut pas être null");
        verifierDeviseCompatible(other);

        BigDecimal resultat = this.montant.subtract(other.montant);
        if (resultat.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Impossible de soustraire " + other + " de " + this + " : résultat négatif ou nul"
            );
        }
        return new Money(resultat, this.devise);
    }

    /**
     * Indique si ce montant est supérieur ou égal à {@code other}.
     */
    public boolean estSuperieurOuEgalA(Money other) {
        Objects.requireNonNull(other, "Le montant de comparaison ne peut pas être null");
        verifierDeviseCompatible(other);
        return this.montant.compareTo(other.montant) >= 0;
    }

    // ── Aide interne ─────────────────────────────────────────────────────────
    private void verifierDeviseCompatible(Money other) {
        if (!this.devise.equalsIgnoreCase(other.devise)) {
            throw new IllegalArgumentException(
                "Opération impossible entre devises différentes : "
                + this.devise + " ≠ " + other.devise
            );
        }
    }

    // ── Affichage ────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return montant.toPlainString() + " " + devise;
    }
}
