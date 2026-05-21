package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;
import sn.dakartechwallt.domain.valueobject.ProductCategory;
import sn.dakartechwallt.domain.valueobject.Sku;

import java.util.Objects;
import java.util.UUID;

/**
 * MODULE 2 — Entité : Product
 *
 * Représente un produit ou service proposé dans le catalogue Dakar-Tech Wallet.
 * Chaque produit a une identité unique (UUID) et un SKU métier.
 *
 * Invariants :
 *  - Le prix ne peut jamais être négatif.
 *  - Un produit archivé ne peut plus être mis en vente.
 *  - Aucun setter exposé : les changements passent par des verbes métier.
 */
public class Product {

    // ── Identité ─────────────────────────────────────────────────────────────
    private final UUID            id;
    private final Sku             sku;

    // ── État métier ───────────────────────────────────────────────────────────
    private       String          nom;
    private       String          description;
    private       Money           prix;
    private       ProductCategory categorie;
    private       boolean         disponible;
    private       boolean         archive;

    // ── Constructeur : l'objet doit être valide dès la naissance ─────────────
    public Product(Sku sku, String nom, String description,
                   Money prix, ProductCategory categorie) {
        Objects.requireNonNull(sku,         "Le SKU est obligatoire");
        Objects.requireNonNull(nom,         "Le nom du produit est obligatoire");
        Objects.requireNonNull(description, "La description est obligatoire");
        Objects.requireNonNull(prix,        "Le prix est obligatoire");
        Objects.requireNonNull(categorie,   "La catégorie est obligatoire");

        if (nom.isBlank())         throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (description.isBlank()) throw new IllegalArgumentException("La description ne peut pas être vide");

        this.id          = UUID.randomUUID();
        this.sku         = sku;
        this.nom         = nom.strip();
        this.description = description.strip();
        this.prix        = prix;
        this.categorie   = categorie;
        this.disponible  = true;
        this.archive     = false;
    }

    // ── Opérations métier (Ubiquitous Language) ──────────────────────────────

    /**
     * Applique une remise en pourcentage sur le prix du produit.
     *
     * @param pourcentage entre 0 (exclu) et 100 (exclu)
     */
    public void appliquerRemise(double pourcentage) {
        verifierNonArchive();
        if (pourcentage <= 0 || pourcentage >= 100) {
            throw new IllegalArgumentException(
                "La remise doit être comprise entre 0% et 100% (exclu). Reçu : " + pourcentage + "%"
            );
        }
        double facteur = 1.0 - (pourcentage / 100.0);
        this.prix = Money.de(
            this.prix.montant().multiply(java.math.BigDecimal.valueOf(facteur)),
            this.prix.devise()
        );
    }

    /**
     * Met à jour le prix du produit.
     */
    public void modifierPrix(Money nouveauPrix) {
        Objects.requireNonNull(nouveauPrix, "Le nouveau prix ne peut pas être null");
        verifierNonArchive();
        this.prix = nouveauPrix;
    }

    /**
     * Suspend temporairement la disponibilité du produit.
     */
    public void suspendre() {
        verifierNonArchive();
        if (!this.disponible) throw new IllegalStateException("Le produit est déjà suspendu");
        this.disponible = false;
    }

    /**
     * Remet le produit en vente.
     */
    public void remettreEnVente() {
        verifierNonArchive();
        if (this.disponible) throw new IllegalStateException("Le produit est déjà disponible");
        this.disponible = true;
    }

    /**
     * Archive définitivement le produit (irréversible).
     */
    public void archiver() {
        if (this.archive) throw new IllegalStateException("Le produit est déjà archivé");
        this.disponible = false;
        this.archive    = true;
    }

    /**
     * Indique si le produit est achetable (disponible et non archivé).
     */
    public boolean estAchetable() {
        return disponible && !archive;
    }

    // ── Aide interne ─────────────────────────────────────────────────────────
    private void verifierNonArchive() {
        if (this.archive) {
            throw new IllegalStateException(
                "Impossible de modifier le produit archivé : " + sku
            );
        }
    }

    // ── Accesseurs (lecture seule) ────────────────────────────────────────────
    public UUID            getId()          { return id;          }
    public Sku             getSku()         { return sku;         }
    public String          getNom()         { return nom;         }
    public String          getDescription() { return description; }
    public Money           getPrix()        { return prix;        }
    public ProductCategory getCategorie()   { return categorie;   }
    public boolean         estDisponible()  { return disponible;  }
    public boolean         estArchive()     { return archive;     }

    // ── Égalité basée sur le SKU ──────────────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product p)) return false;
        return Objects.equals(sku, p.sku);
    }

    @Override
    public int hashCode() { return Objects.hash(sku); }

    @Override
    public String toString() {
        return String.format("Product{sku=%s, nom='%s', prix=%s, catégorie=%s, disponible=%b}",
            sku, nom, prix, categorie, disponible);
    }
}
