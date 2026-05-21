package sn.dakartechwallt.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * MODULE 2 — Value Object : SKU (Stock Keeping Unit)
 *
 * Identifiant unique d'un produit dans le catalogue.
 * Format : lettres majuscules, chiffres, tirets. Entre 3 et 20 caractères.
 * Exemples : "WAVE-SIM-001", "DKT-FORFAIT-5G", "OM-CASH-PLUS"
 *
 * Immuable. Auto-validant à la construction.
 */
public record Sku(String code) {

    private static final Pattern FORMAT_SKU =
        Pattern.compile("^[A-Z0-9][A-Z0-9\\-]{1,18}[A-Z0-9]$");

    public Sku {
        Objects.requireNonNull(code, "Le code SKU ne peut pas être null");
        String normalise = code.toUpperCase().strip();
        if (!FORMAT_SKU.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                "SKU invalide : '" + code + "'. "
                + "Format attendu : 3-20 caractères, lettres majuscules/chiffres/tirets, "
                + "commence et finit par un caractère alphanumérique."
            );
        }
        code = normalise;
    }

    /** Fabrique statique. */
    public static Sku de(String code) {
        return new Sku(code);
    }

    /** Premier segment du code (avant le premier tiret). */
    public String prefixe() {
        int idx = code.indexOf('-');
        return idx > 0 ? code.substring(0, idx) : code;
    }

    @Override
    public String toString() {
        return code;
    }
}
