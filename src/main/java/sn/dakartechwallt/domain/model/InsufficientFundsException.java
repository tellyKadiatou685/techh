package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;

/**
 * Exception métier levée lorsqu'un compte n'a pas les fonds suffisants
 * pour honorer une opération de débit ou de transfert.
 */
public class InsufficientFundsException extends RuntimeException {

    private final Money soldeDisponible;
    private final Money montantDemande;

    public InsufficientFundsException(Money soldeDisponible, Money montantDemande) {
        super(String.format(
            "Fonds insuffisants : solde disponible = %s | montant demandé = %s",
            soldeDisponible, montantDemande
        ));
        this.soldeDisponible = soldeDisponible;
        this.montantDemande  = montantDemande;
    }

    public Money getSoldeDisponible() { return soldeDisponible; }
    public Money getMontantDemande()  { return montantDemande;  }
}
