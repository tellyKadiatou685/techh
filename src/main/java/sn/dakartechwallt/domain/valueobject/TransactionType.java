package sn.dakartechwallt.domain.valueobject;

/**
 * MODULE 3 — Enumération des types de transactions.
 *
 * Chaque type indique le sens du flux financier (CREDIT augmente le solde,
 * DEBIT le diminue) et le libellé métier affiché dans l'historique.
 */
public enum TransactionType {

    DEPOT          ("Dépôt",            Sens.CREDIT),
    RETRAIT        ("Retrait",           Sens.DEBIT),
    TRANSFERT_ENVOYE  ("Transfert envoyé",  Sens.DEBIT),
    TRANSFERT_RECU    ("Transfert reçu",    Sens.CREDIT),
    PAIEMENT_SERVICE  ("Paiement service",  Sens.DEBIT),
    REMBOURSEMENT     ("Remboursement",     Sens.CREDIT),
    FRAIS             ("Frais de service",  Sens.DEBIT);

    /** Sens du flux financier. */
    public enum Sens { CREDIT, DEBIT }

    private final String libelle;
    private final Sens   sens;

    TransactionType(String libelle, Sens sens) {
        this.libelle = libelle;
        this.sens    = sens;
    }

    public String getLibelle() { return libelle; }
    public Sens   getSens()    { return sens;    }

    public boolean estCredit() { return sens == Sens.CREDIT; }
    public boolean estDebit()  { return sens == Sens.DEBIT;  }

    @Override
    public String toString() { return libelle; }
}
