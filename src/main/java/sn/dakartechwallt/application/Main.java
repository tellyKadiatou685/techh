package sn.dakartechwallt.application;

import sn.dakartechwallt.domain.model.*;
import sn.dakartechwallt.domain.valueobject.*;

import java.util.List;

/**
 * CLASSE PRINCIPALE — Tests de robustesse des 3 modules
 *
 * Module 1 : Value Objects (Money, PhoneNumber) + Account de base
 * Module 2 : Catalog (Sku, Product, Catalog)
 * Module 3 : Transactions & Historique (Transaction, TransactionHistory)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         DAKAR-TECH WALLET — Tests tous modules (1,2,3)       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // ── MODULE 1 ──────────────────────────────────────────────────────────
        System.out.println("\n\n════════════════ MODULE 1 : Value Objects & Account ════════════════");

        testerMoneyValide();
        testerMontantNegatif();
        testerMontantNul();
        testerDeviseDifferente();
        testerPhoneNumberValides();
        testerPhoneNumberInvalides();
        testerCreationAccount();
        testerDebitDepasseSolde();
        testerTransfertBasique();
        testerCompteDesactive();

        // ── MODULE 2 ──────────────────────────────────────────────────────────
        System.out.println("\n\n════════════════ MODULE 2 : Catalog (SKU, Product, Catalog) ════════════════");

        testerSkuValide();
        testerSkuInvalide();
        testerCatalogAjoutProduit();
        testerCatalogSkuDuplique();
        testerCatalogRemise();
        testerCatalogFiltreCategorie();
        testerProduitArchive();
        testerCatalogRetrait();

        // ── MODULE 3 ──────────────────────────────────────────────────────────
        System.out.println("\n\n════════════════ MODULE 3 : Transactions & Historique ════════════════");

        testerTransactionCycleDeVie();
        testerRemboursementInvalide();
        testerHistoriqueComplet();
        testerPaiementService();
        testerTransfertAvecTrace();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              Tous les scénarios exécutés avec succès ✔        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE 1 — Value Objects & Account
    // ═══════════════════════════════════════════════════════════════════════════

    static void testerMoneyValide() {
        titre("M1-1. Money valide");
        try {
            Money m = Money.de(5000, "XOF");
            Money m2 = Money.de(2000, "XOF");
            Money total = m.add(m2);
            succes("Créé : " + m + " | Après add : " + total);
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerMontantNegatif() {
        titre("M1-2. Montant négatif → doit échouer");
        try {
            Money.de(-200, "XOF");
            echec("Aurait dû lever une exception !");
        } catch (IllegalArgumentException e) { echecAttendu(e.getMessage()); }
    }

    static void testerMontantNul() {
        titre("M1-3. Montant nul → doit échouer");
        try {
            Money.de(0, "XOF");
            echec("Aurait dû lever une exception !");
        } catch (IllegalArgumentException e) { echecAttendu(e.getMessage()); }
    }

    static void testerDeviseDifferente() {
        titre("M1-4. Addition de devises différentes → doit échouer");
        try {
            Money xof = Money.de(5000, "XOF");
            Money eur = Money.de(10, "EUR");
            xof.add(eur);
            echec("Aurait dû lever une exception !");
        } catch (IllegalArgumentException e) { echecAttendu(e.getMessage()); }
    }

    static void testerPhoneNumberValides() {
        titre("M1-5. Numéros sénégalais valides");
        String[] nums = {"+221771234567", "781234567", "70 123 45 67", "+221-76-000-00-01"};
        for (String n : nums) {
            try {
                PhoneNumber p = PhoneNumber.de(n);
                succes(n + " → " + p + " [" + p.operateur() + "]");
            } catch (Exception e) { echec(n + " : " + e.getMessage()); }
        }
    }

    static void testerPhoneNumberInvalides() {
        titre("M1-6. Numéros invalides → doivent échouer");
        String[] nums = {"0612345678", "+33612345678", "75123456", "123"};
        for (String n : nums) {
            try {
                PhoneNumber.de(n);
                echec("'" + n + "' aurait dû être rejeté !");
            } catch (IllegalArgumentException e) { echecAttendu("'" + n + "' rejeté"); }
        }
    }

    static void testerCreationAccount() {
        titre("M1-7. Création d'un compte et opérations de base");
        try {
            Account a = creerCompteAmine();
            a.crediter(Money.de(3000, "XOF"));
            a.debiter(Money.de(1000, "XOF"));
            succes("Compte : " + a);
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerDebitDepasseSolde() {
        titre("M1-8. Débit > solde → doit échouer");
        try {
            creerCompteAmine().debiter(Money.de(99999, "XOF"));
            echec("Aurait dû lever InsufficientFundsException !");
        } catch (InsufficientFundsException e) { echecAttendu(e.getMessage()); }
    }

    static void testerTransfertBasique() {
        titre("M1-9. Transfert normal entre deux comptes");
        try {
            Account amine   = creerCompteAmine();
            Account mariama = creerCompteMariama();
            amine.transfererVers(mariama, Money.de(4000, "XOF"));
            succes("Amine : " + amine.getSolde() + " | Mariama : " + mariama.getSolde());
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerCompteDesactive() {
        titre("M1-10. Opération sur compte désactivé → doit échouer");
        try {
            Account a = creerCompteAmine();
            a.desactiver();
            a.crediter(Money.de(100, "XOF"));
            echec("Aurait dû lever IllegalStateException !");
        } catch (IllegalStateException e) { echecAttendu(e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE 2 — Catalog
    // ═══════════════════════════════════════════════════════════════════════════

    static void testerSkuValide() {
        titre("M2-1. SKU valides");
        String[] skus = {"WAVE-SIM-001", "OM-DATA-5G", "DKT-EPARGNE"};
        for (String s : skus) {
            try {
                Sku sku = Sku.de(s);
                succes(sku + " [préfixe: " + sku.prefixe() + "]");
            } catch (Exception e) { echec(s + " : " + e.getMessage()); }
        }
    }

    static void testerSkuInvalide() {
        titre("M2-2. SKU invalides → doivent échouer");
        String[] skus = {"ab", "SKU avec espace", "-DEBUT", "TROP-LONG-SKU-CODE-EXCEEDING-LIMIT-HERE"};
        for (String s : skus) {
            try {
                Sku.de(s);
                echec("'" + s + "' aurait dû être rejeté !");
            } catch (IllegalArgumentException e) { echecAttendu("'" + s + "' rejeté"); }
        }
    }

    static void testerCatalogAjoutProduit() {
        titre("M2-3. Référencement de produits dans le catalogue");
        try {
            Catalog cat = construireCatalog();
            succes("Catalogue : " + cat);
            cat.produitsAchetables().forEach(p ->
                System.out.println("   └─ " + p.getSku() + " | " + p.getNom() + " | " + p.getPrix())
            );
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerCatalogSkuDuplique() {
        titre("M2-4. SKU dupliqué dans le catalogue → doit échouer");
        try {
            Catalog cat = new Catalog("Test");
            Product p1 = new Product(Sku.de("WAVE-SIM-001"), "SIM Wave", "Carte SIM Wave",
                Money.de(500, "XOF"), ProductCategory.TRANSFERT_ARGENT);
            Product p2 = new Product(Sku.de("WAVE-SIM-001"), "SIM Wave Dup", "Dupliqué",
                Money.de(600, "XOF"), ProductCategory.TRANSFERT_ARGENT);
            cat.referencerProduit(p1);
            cat.referencerProduit(p2);
            echec("Aurait dû lever une exception !");
        } catch (IllegalArgumentException e) { echecAttendu(e.getMessage()); }
    }

    static void testerCatalogRemise() {
        titre("M2-5. Application d'une remise sur un produit");
        try {
            Catalog cat = construireCatalog();
            Sku sku = Sku.de("WAVE-SIM-001");
            Money avant = cat.trouverParSku(sku).getPrix();
            cat.appliquerRemiseSur(sku, 20.0);
            Money apres = cat.trouverParSku(sku).getPrix();
            succes("Prix avant : " + avant + " | Après remise 20% : " + apres);
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerCatalogFiltreCategorie() {
        titre("M2-6. Filtrage par catégorie");
        try {
            Catalog cat = construireCatalog();
            List<Product> data = cat.produitsParCategorie(ProductCategory.FORFAIT_DATA);
            succes("Produits FORFAIT_DATA trouvés : " + data.size());
            data.forEach(p -> System.out.println("   └─ " + p.getNom()));
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerProduitArchive() {
        titre("M2-7. Modification d'un produit archivé → doit échouer");
        try {
            Product p = new Product(Sku.de("OLD-PROD-01"), "Vieux produit", "Obsolète",
                Money.de(100, "XOF"), ProductCategory.CASHOUT);
            p.archiver();
            p.modifierPrix(Money.de(200, "XOF"));
            echec("Aurait dû lever une exception !");
        } catch (IllegalStateException e) { echecAttendu(e.getMessage()); }
    }

    static void testerCatalogRetrait() {
        titre("M2-8. Retrait (archivage) d'un produit du catalogue");
        try {
            Catalog cat = construireCatalog();
            int avant = cat.nombreDeProduitsActifs();
            cat.retirerProduit(Sku.de("WAVE-SIM-001"));
            int apres = cat.nombreDeProduitsActifs();
            succes("Produits actifs avant : " + avant + " | après retrait : " + apres);
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE 3 — Transactions & Historique
    // ═══════════════════════════════════════════════════════════════════════════

    static void testerTransactionCycleDeVie() {
        titre("M3-1. Cycle de vie d'une transaction (EN_ATTENTE → VALIDEE → REMBOURSEE)");
        try {
            Account a = creerCompteAmine();
            Transaction tx = new Transaction(
                TransactionType.DEPOT, Money.de(1000, "XOF"),
                a.getId(), null, "Test cycle de vie"
            );
            System.out.println("   Statut initial : " + tx.getStatut());
            tx.valider();
            System.out.println("   Après valider() : " + tx.getStatut());
            tx.rembourser();
            System.out.println("   Après rembourser() : " + tx.getStatut());
            succes("Cycle complet parcouru");
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerRemboursementInvalide() {
        titre("M3-2. Remboursement d'une transaction non validée → doit échouer");
        try {
            Account a = creerCompteAmine();
            Transaction tx = new Transaction(
                TransactionType.DEPOT, Money.de(500, "XOF"),
                a.getId(), null, "Test remboursement invalide"
            );
            tx.rembourser(); // doit échouer car EN_ATTENTE
            echec("Aurait dû lever une exception !");
        } catch (IllegalStateException e) { echecAttendu(e.getMessage()); }
    }

    static void testerHistoriqueComplet() {
        titre("M3-3. Historique complet après plusieurs opérations");
        try {
            Account amine = creerCompteAmine();
            amine.crediter(Money.de(5000, "XOF"), "Salaire mars 2025");
            amine.debiter(Money.de(2000, "XOF"), "Loyer");
            amine.payerService(Money.de(3500, "XOF"), "Facture SENELEC");

            TransactionHistory hist = amine.getHistorique();
            System.out.println("   Nombre de transactions : " + hist.nombreTotal());
            System.out.println("   Solde actuel           : " + amine.getSolde());

            Money credits = hist.totalCredits("XOF");
            Money debits  = hist.totalDebits("XOF");
            System.out.println("   Total crédités         : " + credits);
            System.out.println("   Total débités          : " + debits);

            hist.toutesLesTransactions().forEach(tx ->
                System.out.println("   └─ " + tx)
            );
            succes("Historique complet affiché");
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    static void testerPaiementService() {
        titre("M3-4. Paiement service avec fonds insuffisants → doit échouer");
        try {
            Account a = creerCompteAmine(); // 10 000 XOF
            a.payerService(Money.de(50000, "XOF"), "Abonnement satellite");
            echec("Aurait dû lever InsufficientFundsException !");
        } catch (InsufficientFundsException e) { echecAttendu(e.getMessage()); }
    }

    static void testerTransfertAvecTrace() {
        titre("M3-5. Transfert avec vérification des traces des deux côtés");
        try {
            Account amine   = creerCompteAmine();
            Account mariama = creerCompteMariama();
            amine.transfererVers(mariama, Money.de(3000, "XOF"), "Remboursement repas");

            System.out.println("   Historique Amine   (" + amine.getHistorique().nombreTotal() + " tx) :");
            amine.getHistorique().toutesLesTransactions()
                .forEach(tx -> System.out.println("   └─ " + tx));

            System.out.println("   Historique Mariama (" + mariama.getHistorique().nombreTotal() + " tx) :");
            mariama.getHistorique().toutesLesTransactions()
                .forEach(tx -> System.out.println("   └─ " + tx));

            succes("Amine: " + amine.getSolde() + " | Mariama: " + mariama.getSolde());
        } catch (Exception e) { echec("Inattendu : " + e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Fabriques de test
    // ═══════════════════════════════════════════════════════════════════════════

    static Account creerCompteAmine() {
        return new Account("Aminata Sow",
            PhoneNumber.de("+221 77 123 45 67"),
            Money.de(10_000, "XOF"));
    }

    static Account creerCompteMariama() {
        return new Account("Mariama Diallo",
            PhoneNumber.de("78 765 43 21"),
            Money.de(5_000, "XOF"));
    }

    static Catalog construireCatalog() {
        Catalog cat = new Catalog("Dakar-Tech Wallet Services");

        cat.referencerProduit(new Product(
            Sku.de("WAVE-SIM-001"), "SIM Wave Sénégal",
            "Carte SIM prête à l'emploi avec compte Wave activé",
            Money.de(500, "XOF"), ProductCategory.TRANSFERT_ARGENT));

        cat.referencerProduit(new Product(
            Sku.de("OM-DATA-5G"), "Forfait Orange 5G 10Go",
            "Forfait données mobiles 10 Go valable 30 jours",
            Money.de(4900, "XOF"), ProductCategory.FORFAIT_DATA));

        cat.referencerProduit(new Product(
            Sku.de("OM-DATA-2G"), "Forfait Orange 2G 2Go",
            "Forfait données mobiles 2 Go valable 7 jours",
            Money.de(1500, "XOF"), ProductCategory.FORFAIT_DATA));

        cat.referencerProduit(new Product(
            Sku.de("DKT-SENELEC"), "Paiement facture SENELEC",
            "Règlement de facture électricité SENELEC",
            Money.de(100, "XOF"), ProductCategory.PAIEMENT_FACTURE));

        cat.referencerProduit(new Product(
            Sku.de("DKT-EPARGNE"), "Tontine numérique",
            "Épargne communautaire digitale avec rendement mensuel",
            Money.de(1000, "XOF"), ProductCategory.EPARGNE));

        return cat;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers d'affichage
    // ═══════════════════════════════════════════════════════════════════════════

    static void titre(String s)       { System.out.println("\n── " + s + " ──"); }
    static void succes(String s)      { System.out.println("   ✅ " + s); }
    static void echec(String s)       { System.out.println("   ❌ ERREUR : " + s); }
    static void echecAttendu(String s){ System.out.println("   ✅ Échec attendu → " + s); }
}
