package sn.dakartechwallt.domain.model;

import sn.dakartechwallt.domain.valueobject.Money;
import sn.dakartechwallt.domain.valueobject.ProductCategory;
import sn.dakartechwallt.domain.valueobject.Sku;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MODULE 2 — Agrégat : Catalog
 *
 * Gère l'ensemble des produits/services du portefeuille numérique.
 * C'est la racine d'agrégat (Aggregate Root) du module catalogue.
 *
 * Invariants :
 *  - Deux produits ne peuvent pas avoir le même SKU dans le catalogue.
 *  - Seuls les produits achetables peuvent être commandés.
 *  - Le catalogue expose des vues filtrées, jamais sa collection interne.
 */
public class Catalog {

    // ── État interne (jamais exposé directement) ──────────────────────────────
    private final Map<Sku, Product> produits = new LinkedHashMap<>();
    private final String            nom;

    public Catalog(String nom) {
        Objects.requireNonNull(nom, "Le nom du catalogue est obligatoire");
        if (nom.isBlank()) throw new IllegalArgumentException("Le nom du catalogue ne peut pas être vide");
        this.nom = nom.strip();
    }

    // ── Opérations métier ────────────────────────────────────────────────────

    /**
     * Référence un nouveau produit dans le catalogue.
     *
     * @throws IllegalArgumentException si le SKU est déjà utilisé
     */
    public void referencerProduit(Product produit) {
        Objects.requireNonNull(produit, "Le produit ne peut pas être null");
        if (produits.containsKey(produit.getSku())) {
            throw new IllegalArgumentException(
                "Un produit avec le SKU '" + produit.getSku() + "' existe déjà dans le catalogue."
            );
        }
        produits.put(produit.getSku(), produit);
    }

    /**
     * Retire (archive) un produit du catalogue actif.
     *
     * @throws NoSuchElementException si le SKU est introuvable
     */
    public void retirerProduit(Sku sku) {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        Product produit = trouverParSku(sku);
        produit.archiver();
    }

    /**
     * Applique une remise sur un produit identifié par son SKU.
     */
    public void appliquerRemiseSur(Sku sku, double pourcentage) {
        trouverParSku(sku).appliquerRemise(pourcentage);
    }

    /**
     * Met à jour le prix d'un produit.
     */
    public void modifierPrixDe(Sku sku, Money nouveauPrix) {
        trouverParSku(sku).modifierPrix(nouveauPrix);
    }

    // ── Requêtes (vues en lecture seule) ─────────────────────────────────────

    /** Retrouve un produit par son SKU (lance une exception s'il est introuvable). */
    public Product trouverParSku(Sku sku) {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        Product p = produits.get(sku);
        if (p == null) throw new NoSuchElementException("Produit introuvable pour le SKU : " + sku);
        return p;
    }

    /** Retourne uniquement les produits achetables. */
    public List<Product> produitsAchetables() {
        return produits.values().stream()
            .filter(Product::estAchetable)
            .collect(Collectors.toUnmodifiableList());
    }

    /** Filtre les produits achetables par catégorie. */
    public List<Product> produitsParCategorie(ProductCategory categorie) {
        Objects.requireNonNull(categorie, "La catégorie ne peut pas être null");
        return produits.values().stream()
            .filter(Product::estAchetable)
            .filter(p -> p.getCategorie() == categorie)
            .collect(Collectors.toUnmodifiableList());
    }

    /** Retourne tous les produits (y compris archivés) — usage interne/admin. */
    public List<Product> tousLesProduits() {
        return Collections.unmodifiableList(new ArrayList<>(produits.values()));
    }

    /** Nombre de produits achetables. */
    public int nombreDeProduitsActifs() {
        return (int) produits.values().stream().filter(Product::estAchetable).count();
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────
    public String getNom() { return nom; }

    @Override
    public String toString() {
        return String.format("Catalog{nom='%s', produits actifs=%d, total=%d}",
            nom, nombreDeProduitsActifs(), produits.size());
    }
}
