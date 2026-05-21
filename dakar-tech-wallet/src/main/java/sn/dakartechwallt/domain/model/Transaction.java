package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;
import sn.dakartechwallt.domain.valueobject.TransactionStatus;
import sn.dakartechwallt.domain.valueobject.TransactionType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * MODULE 3 — Entité : Transaction
 *
 * Représente une opération financière enregistrée dans le système.
 * Possède son propre cycle de vie : EN_ATTENTE → VALIDEE | ECHOUEE → REMBOURSEE.
 *
 * Invariants :
 *  - Une transaction validée ne peut pas être re-validée.
 *  - Seule une transaction VALIDEE peut être remboursée.
 *  - Le montant et le type sont immuables après création.
 *  - L'identifiant du compte émetteur est toujours présent.
 *  - L'identifiant du compte destinataire est optionnel (ex: dépôt en espèces).
 */
public class Transaction {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                         .withZone(ZoneId.of("Africa/Dakar"));

    // ── Identité ──────────────────────────────────────────────────────────────
    private final UUID              id;
    private final Instant           dateCreation;

    // ── Données immuables ─────────────────────────────────────────────────────
    private final TransactionType   type;
    private final Money             montant;
    private final UUID              compteEmetteurId;
    private final UUID              compteDestinatairId;   // nullable
    private final String            reference;             // libellé libre

    // ── État mutable ──────────────────────────────────────────────────────────
    private TransactionStatus       statut;
    private Instant                 dateValidation;
    private String                  motifEchec;

    // ── Constructeur ─────────────────────────────────────────────────────────
    /**
     * @param type                 Nature de l'opération
     * @param montant              Montant (strictement positif)
     * @param compteEmetteurId     UUID du compte source (toujours requis)
     * @param compteDestinatairId  UUID du compte cible (null si non applicable)
     * @param reference            Libellé libre (ex: "Facture SENELEC mars 2025")
     */
    public Transaction(TransactionType type, Money montant,
                       UUID compteEmetteurId, UUID compteDestinatairId,
                       String reference) {
        Objects.requireNonNull(type,             "Le type de transaction est obligatoire");
        Objects.requireNonNull(montant,          "Le montant est obligatoire");
        Objects.requireNonNull(compteEmetteurId, "L'identifiant du compte émetteur est obligatoire");
        Objects.requireNonNull(reference,        "La référence est obligatoire");

        if (reference.isBlank()) throw new IllegalArgumentException("La référence ne peut pas être vide");

        this.id                  = UUID.randomUUID();
        this.dateCreation        = Instant.now();
        this.type                = type;
        this.montant             = montant;
        this.compteEmetteurId    = compteEmetteurId;
        this.compteDestinatairId = compteDestinatairId;   // peut être null
        this.reference           = reference.strip();
        this.statut              = TransactionStatus.EN_ATTENTE;
    }

    // ── Cycle de vie (Ubiquitous Language) ───────────────────────────────────

    /**
     * Valide la transaction (l'opération financière a bien eu lieu).
     */
    public void valider() {
        if (statut != TransactionStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                "Impossible de valider une transaction en statut '" + statut + "'."
            );
        }
        this.statut         = TransactionStatus.VALIDEE;
        this.dateValidation = Instant.now();
    }

    /**
     * Marque la transaction comme échouée avec un motif explicatif.
     */
    public void marquerEchouee(String motif) {
        Objects.requireNonNull(motif, "Le motif d'échec est obligatoire");
        if (statut != TransactionStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                "Impossible de marquer en échec une transaction en statut '" + statut + "'."
            );
        }
        this.statut     = TransactionStatus.ECHOUEE;
        this.motifEchec = motif.strip();
    }

    /**
     * Rembourse une transaction déjà validée.
     */
    public void rembourser() {
        if (statut != TransactionStatus.VALIDEE) {
            throw new IllegalStateException(
                "Seule une transaction VALIDEE peut être remboursée. Statut actuel : " + statut
            );
        }
        this.statut = TransactionStatus.REMBOURSEE;
    }

    // ── Requêtes ──────────────────────────────────────────────────────────────
    public boolean estValidee()    { return statut == TransactionStatus.VALIDEE;    }
    public boolean estEchouee()    { return statut == TransactionStatus.ECHOUEE;    }
    public boolean estRemboursee() { return statut == TransactionStatus.REMBOURSEE; }

    // ── Accesseurs ────────────────────────────────────────────────────────────
    public UUID              getId()                  { return id;                  }
    public Instant           getDateCreation()        { return dateCreation;        }
    public TransactionType   getType()                { return type;                }
    public Money             getMontant()             { return montant;             }
    public UUID              getCompteEmetteurId()    { return compteEmetteurId;    }
    public Optional<UUID>    getCompteDestinatairId() { return Optional.ofNullable(compteDestinatairId); }
    public String            getReference()           { return reference;           }
    public TransactionStatus getStatut()              { return statut;              }
    public Optional<Instant> getDateValidation()      { return Optional.ofNullable(dateValidation); }
    public Optional<String>  getMotifEchec()          { return Optional.ofNullable(motifEchec);     }

    // ── Égalité sur l'id ─────────────────────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction t)) return false;
        return Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format(
            "[%s] %s — %s | Réf: '%s' | Statut: %s",
            FORMATTER.format(dateCreation),
            type.getLibelle(),
            montant,
            reference,
            statut.getLibelle()
        );
    }
}
