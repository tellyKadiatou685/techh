package sn.dakartechwallt.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object représentant un numéro de téléphone sénégalais.
 *
 * Formats acceptés :
 *  - International : +221 7X XXX XX XX
 *  - Local         : 7X XXX XX XX  (avec 70, 76, 77, 78)
 *
 * Immuable et auto-validant à la construction.
 */
public record PhoneNumber(String valeur) {

    /**
     * Accepte : +221770000000 | +221780000000 | 770000000 | 780000000
     * Préfixes valides : 70, 76, 77, 78
     */
    private static final Pattern FORMAT_SENEGALAIS = Pattern.compile(
        "^(\\+221)?(7[0678]\\d{7})$"
    );

    // ── Constructeur compact ─────────────────────────────────────────────────
    public PhoneNumber {
        Objects.requireNonNull(valeur, "Le numéro de téléphone ne peut pas être null");

        String normalise = normaliser(valeur);

        if (!FORMAT_SENEGALAIS.matcher(normalise).matches()) {
            throw new IllegalArgumentException(
                "Numéro sénégalais invalide : '" + valeur + "'. "
                + "Formats attendus : +221 7X XXX XX XX ou 7X XXX XX XX (70, 76, 77, 78)"
            );
        }

        valeur = normalise;
    }

    // ── Fabrique statique ────────────────────────────────────────────────────
    public static PhoneNumber de(String numero) {
        return new PhoneNumber(numero);
    }

    // ── Comportement riche ───────────────────────────────────────────────────

    /**
     * Retourne le numéro au format international (+221XXXXXXXXX).
     */
    public String auFormatInternational() {
        if (valeur.startsWith("+221")) {
            return valeur;
        }
        return "+221" + valeur;
    }

    /**
     * Retourne le numéro au format local (sans indicatif pays).
     */
    public String auFormatLocal() {
        if (valeur.startsWith("+221")) {
            return valeur.substring(4);
        }
        return valeur;
    }

    /**
     * Retourne l'opérateur supposé d'après le préfixe.
     */
    public String operateur() {
        String local = auFormatLocal();
        return switch (local.substring(0, 2)) {
            case "70" -> "Expresso";
            case "76" -> "Free";
            case "77" -> "Orange";
            case "78" -> "Wave / Touba Mobile";
            default   -> "Inconnu";
        };
    }

    // ── Aide interne ─────────────────────────────────────────────────────────
    private static String normaliser(String brut) {
        // Supprime espaces, tirets, points
        return brut.replaceAll("[\\s\\-.]", "");
    }

    // ── Affichage ────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        String local = auFormatLocal();
        // Formate : 77 XXX XX XX
        return local.substring(0, 2) + " "
             + local.substring(2, 5) + " "
             + local.substring(5, 7) + " "
             + local.substring(7, 9);
    }
}
