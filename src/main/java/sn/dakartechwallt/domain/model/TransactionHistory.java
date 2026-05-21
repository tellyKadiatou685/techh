package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;
import sn.dakartechwallt.domain.valueobject.TransactionStatus;
import sn.dakartechwallt.domain.valueobject.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MODULE 3 — Objet de domaine : TransactionHistory
 *
 * Agrège l'historique des transactions d'un compte et offre des comportements
 * riches d'analyse : solde recalculé, filtres, totaux, détection d'anomalies.
 *
 * La liste interne est strictement encapsulée (jamais exposée directement).
 */
public class TransactionHistory {

    private final UUID              compteId;
    private final List<Transaction> transactions = new ArrayList<>();

    public TransactionHistory(UUID compteId) {
        Objects.requireNonNull(compteId, "L'identifiant du compte est obligatoire");
        this.compteId = compteId;
    }

    // ── Enregistrement ────────────────────────────────────────────────────────

    /**
     * Enregistre une transaction dans l'historique.
     * Seules les transactions validées ou en attente sont acceptées.
     */
    public void enregistrer(Transaction transaction) {
        Objects.requireNonNull(transaction, "La transaction ne peut pas être null");
        if (transaction.estEchouee()) {
            throw new IllegalArgumentException(
                "Seules les transactions validées ou en attente peuvent être enregistrées dans l'historique."
            );
        }
        transactions.add(transaction);
    }

    // ── Comportements riches d'analyse ────────────────────────────────────────

    /**
     * Calcule le total crédité (toutes transactions validées de type CREDIT).
     */
    public Money totalCredits(String devise) {
        Objects.requireNonNull(devise, "La devise est obligatoire");
        BigDecimal total = transactions.stream()
            .filter(Transaction::estValidee)
            .filter(t -> t.getType().estCredit())
            .filter(t -> t.getMontant().devise().equalsIgnoreCase(devise))
            .map(t -> t.getMontant().montant())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(BigDecimal.ZERO) > 0
            ? Money.de(total, devise)
            : null;   // aucun crédit enregistré
    }

    /**
     * Calcule le total débité (toutes transactions validées de type DEBIT).
     */
    public Money totalDebits(String devise) {
        Objects.requireNonNull(devise, "La devise est obligatoire");
        BigDecimal total = transactions.stream()
            .filter(Transaction::estValidee)
            .filter(t -> t.getType().estDebit())
            .filter(t -> t.getMontant().devise().equalsIgnoreCase(devise))
            .map(t -> t.getMontant().montant())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(BigDecimal.ZERO) > 0
            ? Money.de(total, devise)
            : null;
    }

    /**
     * Filtre les transactions par type.
     */
    public List<Transaction> parType(TransactionType type) {
        Objects.requireNonNull(type, "Le type est obligatoire");
        return transactions.stream()
            .filter(t -> t.getType() == type)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Filtre les transactions par statut.
     */
    public List<Transaction> parStatut(TransactionStatus statut) {
        Objects.requireNonNull(statut, "Le statut est obligatoire");
        return transactions.stream()
            .filter(t -> t.getStatut() == statut)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Retourne les transactions dans une fenêtre temporelle (bornes incluses).
     */
    public List<Transaction> parPeriode(Instant debut, Instant fin) {
        Objects.requireNonNull(debut, "La date de début est obligatoire");
        Objects.requireNonNull(fin,   "La date de fin est obligatoire");
        if (debut.isAfter(fin)) throw new IllegalArgumentException("La date de début doit être antérieure à la fin");

        return transactions.stream()
            .filter(t -> !t.getDateCreation().isBefore(debut) && !t.getDateCreation().isAfter(fin))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Retourne la dernière transaction enregistrée, si elle existe.
     */
    public Optional<Transaction> derniere() {
        if (transactions.isEmpty()) return Optional.empty();
        return Optional.of(transactions.get(transactions.size() - 1));
    }

    /**
     * Retourne la transaction la plus élevée en montant parmi les validées.
     */
    public Optional<Transaction> plusGrandeOperation() {
        return transactions.stream()
            .filter(Transaction::estValidee)
            .max(Comparator.comparing(t -> t.getMontant().montant()));
    }

    /**
     * Détecte des transactions en attente depuis plus d'un certain nombre de secondes.
     * (Utile pour détecter des opérations bloquées.)
     */
    public List<Transaction> transactionsEnAttenteTropLongtemps(long seuilSecondes) {
        Instant seuil = Instant.now().minusSeconds(seuilSecondes);
        return transactions.stream()
            .filter(t -> t.getStatut() == sn.dakartechwallt.domain.valueobject.TransactionStatus.EN_ATTENTE)
            .filter(t -> t.getDateCreation().isBefore(seuil))
            .collect(Collectors.toUnmodifiableList());
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────
    public UUID getCompteId()  { return compteId; }
    public int  nombreTotal()  { return transactions.size(); }

    /** Vue non modifiable de l'historique complet (dans l'ordre d'insertion). */
    public List<Transaction> toutesLesTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public String toString() {
        return String.format("TransactionHistory{compteId=%s, total=%d transactions}",
            compteId.toString().substring(0, 8) + "…", transactions.size());
    }
}
