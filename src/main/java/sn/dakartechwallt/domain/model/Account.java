package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;
import sn.dakartechwallt.domain.valueobject.PhoneNumber;
import sn.dakartechwallt.domain.valueobject.TransactionType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * MODULE 1 + 3 — Entité : Account (version enrichie avec historique)
 *
 * Représente le compte d'un utilisateur du portefeuille numérique.
 * Chaque opération financière génère automatiquement une Transaction
 * validée et l'enregistre dans l'historique du compte.
 *
 * Invariants :
 *  - Le solde ne peut jamais être négatif.
 *  - Aucun setter exposé : le solde ne se modifie que via des verbes métier.
 *  - L'objet est valide dès sa construction.
 *  - Toute opération réussie produit une trace dans l'historique.
 */
public class Account {

    // ── Identité (immuable) ───────────────────────────────────────────────────
    private final UUID               id;
    private final String             proprietaire;
    private final PhoneNumber        telephone;
    private final Instant            dateCreation;

    // ── État mutable encapsulé ────────────────────────────────────────────────
    private       Money              solde;
    private       boolean            actif;

    // ── Module 3 : historique ─────────────────────────────────────────────────
    private final TransactionHistory historique;

    // ── Constructeur ──────────────────────────────────────────────────────────
    public Account(String proprietaire, PhoneNumber telephone, Money soldeInitial) {
        Objects.requireNonNull(proprietaire, "Le nom du propriétaire est obligatoire");
        Objects.requireNonNull(telephone,    "Le numéro de téléphone est obligatoire");
        Objects.requireNonNull(soldeInitial, "Le solde initial est obligatoire");

        if (proprietaire.isBlank())
            throw new IllegalArgumentException("Le nom du propriétaire ne peut pas être vide");

        this.id           = UUID.randomUUID();
        this.proprietaire = proprietaire.strip();
        this.telephone    = telephone;
        this.solde        = soldeInitial;
        this.dateCreation = Instant.now();
        this.actif        = true;
        this.historique   = new TransactionHistory(this.id);

        // Trace d'ouverture de compte
        Transaction depot = new Transaction(
            TransactionType.DEPOT, soldeInitial, this.id, null, "Ouverture du compte"
        );
        depot.valider();
        historique.enregistrer(depot);
    }

    // ── Opérations métier (Ubiquitous Language) ───────────────────────────────

    /** Crédite le compte et trace l'opération avec un libellé. */
    public void crediter(Money montant, String reference) {
        Objects.requireNonNull(montant,   "Le montant de crédit ne peut pas être null");
        Objects.requireNonNull(reference, "La référence est obligatoire");
        verifierCompteActif();

        this.solde = this.solde.add(montant);

        Transaction tx = new Transaction(
            TransactionType.DEPOT, montant, this.id, null, reference
        );
        tx.valider();
        historique.enregistrer(tx);
    }

    /** Surcharge rétrocompatible sans libellé explicite. */
    public void crediter(Money montant) {
        crediter(montant, "Crédit manuel");
    }

    /** Débite le compte et trace l'opération. */
    public void debiter(Money montant, String reference) {
        Objects.requireNonNull(montant,   "Le montant de débit ne peut pas être null");
        Objects.requireNonNull(reference, "La référence est obligatoire");
        verifierCompteActif();

        if (!this.solde.estSuperieurOuEgalA(montant))
            throw new InsufficientFundsException(this.solde, montant);

        this.solde = this.solde.soustraire(montant);

        Transaction tx = new Transaction(
            TransactionType.RETRAIT, montant, this.id, null, reference
        );
        tx.valider();
        historique.enregistrer(tx);
    }

    /** Surcharge rétrocompatible. */
    public void debiter(Money montant) {
        debiter(montant, "Débit manuel");
    }

    /**
     * Transfert sécurisé avec traces des deux côtés.
     * Atomique : si le débit échoue, aucun crédit n'a lieu.
     */
    public void transfererVers(Account destinataire, Money montant, String reference) {
        Objects.requireNonNull(destinataire, "Le compte destinataire ne peut pas être null");
        Objects.requireNonNull(montant,      "Le montant du transfert ne peut pas être null");
        Objects.requireNonNull(reference,    "La référence est obligatoire");

        if (this.id.equals(destinataire.id))
            throw new IllegalArgumentException("Impossible de transférer vers le même compte");

        destinataire.verifierCompteActif();
        verifierCompteActif();

        if (!this.solde.estSuperieurOuEgalA(montant))
            throw new InsufficientFundsException(this.solde, montant);

        // Mutation : débit puis crédit
        this.solde         = this.solde.soustraire(montant);
        destinataire.solde = destinataire.solde.add(montant);

        // Trace émetteur
        Transaction txEnvoi = new Transaction(
            TransactionType.TRANSFERT_ENVOYE, montant,
            this.id, destinataire.id, reference
        );
        txEnvoi.valider();
        this.historique.enregistrer(txEnvoi);

        // Trace destinataire
        Transaction txRecu = new Transaction(
            TransactionType.TRANSFERT_RECU, montant,
            destinataire.id, this.id,
            "Reçu de " + this.proprietaire + " — " + reference
        );
        txRecu.valider();
        destinataire.historique.enregistrer(txRecu);
    }

    /** Surcharge sans libellé. */
    public void transfererVers(Account destinataire, Money montant) {
        transfererVers(destinataire, montant, "Transfert entre comptes");
    }

    /** Paiement d'un service (ex : facture, forfait data, Wave…). */
    public void payerService(Money montant, String nomService) {
        Objects.requireNonNull(montant,    "Le montant est obligatoire");
        Objects.requireNonNull(nomService, "Le nom du service est obligatoire");
        verifierCompteActif();

        if (!this.solde.estSuperieurOuEgalA(montant))
            throw new InsufficientFundsException(this.solde, montant);

        this.solde = this.solde.soustraire(montant);

        Transaction tx = new Transaction(
            TransactionType.PAIEMENT_SERVICE, montant,
            this.id, null, "Paiement : " + nomService
        );
        tx.valider();
        historique.enregistrer(tx);
    }

    /** Désactive définitivement le compte. */
    public void desactiver() {
        if (!this.actif) throw new IllegalStateException("Le compte est déjà désactivé");
        this.actif = false;
    }

    // ── Accesseurs (lecture seule, aucun setter) ──────────────────────────────
    public UUID               getId()           { return id;           }
    public String             getProprietaire() { return proprietaire; }
    public PhoneNumber        getTelephone()    { return telephone;    }
    public Money              getSolde()        { return solde;        }
    public Instant            getDateCreation() { return dateCreation; }
    public boolean            estActif()        { return actif;        }
    public TransactionHistory getHistorique()   { return historique;   }

    // ── Aide interne ──────────────────────────────────────────────────────────
    private void verifierCompteActif() {
        if (!this.actif) throw new IllegalStateException(
            "Le compte de " + proprietaire + " est désactivé. Aucune opération n'est possible."
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account a)) return false;
        return Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format(
            "Account{id=%s, propriétaire='%s', téléphone=%s, solde=%s, actif=%b, transactions=%d}",
            id.toString().substring(0, 8) + "…",
            proprietaire, telephone.auFormatInternational(),
            solde, actif, historique.nombreTotal()
        );
    }
}
