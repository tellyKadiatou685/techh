# 🏦 Dakar-Tech Wallet — Domain-Driven Design (Modules 1, 2, 3)

Noyau métier d'un portefeuille numérique sénégalais (type Wave / Orange Money),
conçu selon les principes DDD : Modèles Riches, Value Objects immuables,
Encapsulation totale et Ubiquitous Language.

---

## 📁 Architecture

```
src/main/java/sn/dakartechwallt/
├── domain/
│   ├── valueobject/                  ← Objets immuables (records Java)
│   │   ├── Money.java                  MODULE 1 — Montant + devise
│   │   ├── PhoneNumber.java            MODULE 1 — Numéro sénégalais validé
│   │   ├── Sku.java                    MODULE 2 — Identifiant produit
│   │   ├── ProductCategory.java        MODULE 2 — Catégories de services
│   │   ├── TransactionType.java        MODULE 3 — Types d'opération (DEPOT, RETRAIT…)
│   │   └── TransactionStatus.java      MODULE 3 — Cycle de vie (EN_ATTENTE→VALIDEE…)
│   └── model/                        ← Entités & agrégats
│       ├── Account.java                MODULE 1+3 — Compte enrichi avec historique
│       ├── InsufficientFundsException  MODULE 1 — Exception métier
│       ├── Product.java                MODULE 2 — Produit/service du catalogue
│       ├── Catalog.java                MODULE 2 — Agrégat racine du catalogue
│       ├── Transaction.java            MODULE 3 — Opération financière tracée
│       └── TransactionHistory.java     MODULE 3 — Historique avec requêtes riches
└── application/
    └── Main.java                     ← 25 scénarios de tests (tous modules)
```

---

## ✅ Règles d'or respectées

| Contrainte              | Implémentation                                              |
|-------------------------|-------------------------------------------------------------|
| **Pas de setters**      | Verbes métier : `crediter()`, `debiter()`, `transfererVers()`, `appliquerRemise()`, `valider()`, `rembourser()` |
| **Immuabilité**         | `Money`, `PhoneNumber`, `Sku` sont des `record` Java 21     |
| **Encapsulation totale**| Tous les champs `private`, validés dès le constructeur      |
| **Logique décentralisée**| Calculs et validations portés par les objets eux-mêmes     |
| **Exception métier**    | `InsufficientFundsException` typée avec contexte complet    |

---

## 🗂️ Les 3 modules en détail

### Module 1 — Value Objects & Account
- `Money` : montant + devise, add/soustraire avec vérification de devise
- `PhoneNumber` : validation regex des formats sénégalais (70, 76, 77, 78)
- `Account` : entité riche avec crediter/debiter/transfererVers/payerService

### Module 2 — Catalog
- `Sku` : identifiant produit alphanumérique normalisé
- `ProductCategory` : enum du langage ubiquitaire (TRANSFERT_ARGENT, FORFAIT_DATA…)
- `Product` : entité avec remise, suspension, archivage
- `Catalog` : agrégat racine avec référencement, filtres par catégorie, requêtes

### Module 3 — Transactions & Historique
- `TransactionType` : enum avec sens financier (CREDIT/DEBIT)
- `TransactionStatus` : cycle de vie EN_ATTENTE → VALIDEE → REMBOURSEE
- `Transaction` : entité avec cycle de vie complet et métadonnées
- `TransactionHistory` : historique avec totalCredits/totalDebits/filtres/détection anomalies
- `Account` enrichi : chaque opération génère automatiquement une trace

---

## 🚀 Compilation & exécution

```bash
# Compiler
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Exécuter (25 scénarios)
java -cp out sn.dakartechwallt.application.Main
```

---

## 🧪 Scénarios testés (25 au total)

| Module | # | Scénario | Résultat |
|--------|---|----------|----------|
| M1 | 1 | Money valide + add | ✅ |
| M1 | 2 | Montant négatif | ❌ attendu |
| M1 | 3 | Montant nul | ❌ attendu |
| M1 | 4 | Addition devises différentes | ❌ attendu |
| M1 | 5 | Numéros sénégalais valides | ✅ |
| M1 | 6 | Numéros invalides | ❌ attendu |
| M1 | 7 | Création compte + opérations | ✅ |
| M1 | 8 | Débit > solde | ❌ attendu |
| M1 | 9 | Transfert normal | ✅ |
| M1 | 10| Compte désactivé | ❌ attendu |
| M2 | 1 | SKU valides | ✅ |
| M2 | 2 | SKU invalides | ❌ attendu |
| M2 | 3 | Référencement produits | ✅ |
| M2 | 4 | SKU dupliqué | ❌ attendu |
| M2 | 5 | Remise produit | ✅ |
| M2 | 6 | Filtre par catégorie | ✅ |
| M2 | 7 | Modification produit archivé | ❌ attendu |
| M2 | 8 | Retrait du catalogue | ✅ |
| M3 | 1 | Cycle de vie transaction | ✅ |
| M3 | 2 | Remboursement invalide | ❌ attendu |
| M3 | 3 | Historique + totaux | ✅ |
| M3 | 4 | Paiement service insuffisant | ❌ attendu |
| M3 | 5 | Transfert + traces bilatérales | ✅ |
