package sn.dakartechwallt.domain.valueobject;

/**
 * MODULE 2 — Enumération des catégories de produits/services.
 *
 * Fait partie du Ubiquitous Language du domaine Dakar-Tech Wallet.
 */
public enum ProductCategory {

    TRANSFERT_ARGENT("Transfert d'argent"),
    PAIEMENT_FACTURE("Paiement de facture"),
    ACHAT_CREDIT("Achat de crédit téléphonique"),
    FORFAIT_DATA("Forfait données mobiles"),
    ASSURANCE("Micro-assurance"),
    EPARGNE("Épargne & tontine numérique"),
    CASHOUT("Retrait espèces");

    private final String libelle;

    ProductCategory(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
