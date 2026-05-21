package sn.dakartechwallt.domain.valueobject;

/**
 * MODULE 3 — Cycle de vie d'une transaction.
 *
 *  EN_ATTENTE → VALIDEE
 *  EN_ATTENTE → ECHOUEE
 *  VALIDEE    → REMBOURSEE
 */
public enum TransactionStatus {

    EN_ATTENTE ("En attente"),
    VALIDEE    ("Validée"),
    ECHOUEE    ("Échouée"),
    REMBOURSEE ("Remboursée");

    private final String libelle;

    TransactionStatus(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
