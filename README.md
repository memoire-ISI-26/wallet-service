# wallet-service

Ce microservice est responsable de la **gestion des comptes financiers (portefeuilles virtuels)**, de la tenue des soldes (solde principal et crédit d'appel) et de l'enregistrement de l'historique des transactions.

## ⚙️ Rôle et Fonctionnalités

- **Gestion des Comptes** (`Account`) :
  - Chaque compte est rattaché à un identifiant utilisateur et à un numéro de téléphone unique (msisdn).
  - Gère deux balances distinctes : `solde` (portefeuille principal) et `callCredit` (crédit de communication téléphonique).
- **Opérations Financières** :
  - **Dépôt (`/deposit`)** : Crédite le portefeuille principal.
  - **Retrait (`/withdraw`)** : Débite le portefeuille principal.
  - **Transfert (`/transfer`)** : Permet le transfert de fonds de portefeuille à portefeuille entre deux clients.
  - **Achat (`/purchase`)** : Gère les débits financiers liés aux achats de pass ou crédit. Il prend en compte le moyen de paiement (`WALLET` pour le portefeuille ou `CREDIT` pour le crédit de communication).
- **Historique** (`Transaction`) : Journalisation de tous les mouvements financiers pour audit et consultation par l'utilisateur.

---

## 🔌 Configuration et Endpoints

- **Port par défaut** : `8301`
- **Base de données** : MySQL (`wallet_service_db`), configurée via JPA/Hibernate.
- **Technologie** : Spring Boot, JPA, Netflix Eureka Client

### Endpoints principaux :

#### 1. Comptes (`/accounts`)
* `POST /accounts` : Crée un nouveau compte (appelé en interne par `user-service` à l'inscription).
* `GET /accounts/number/{number}` : Récupère les détails du compte pour un numéro donné.
* `GET /accounts/number/{number}/balance` : Retourne le solde principal d'un compte (Format brut : `double`).

#### 2. Transactions (`/transactions`)
* `POST /transactions/transfer` : Effectue un virement d'un expéditeur vers un destinataire.
* `POST /transactions/deposit` : Effectue un dépôt sur un compte.
* `POST /transactions/withdraw` : Effectue un retrait depuis un compte.
* `POST /transactions/purchase` : Débite un compte suite à un achat (ex: Achat de pass ou crédit). Gère la validation des soldes suffisants.
* `GET /transactions/history/{number}` : Liste l'historique de toutes les transactions (transferts, recharges, achats) liées à ce numéro.

---

## 🔒 Règles de Gestion Financière

1. **Vérification de Solde** : Toute tentative de débit (retrait, transfert, achat) lance une exception `InsufficentAmountException` si le solde du mode de paiement sélectionné (`WALLET` ou `CREDIT`) est inférieur au montant requis.
2. **Double Écriture** : Pour un transfert, le débit chez l'expéditeur et le crédit chez le destinataire sont traités au sein d'une même transaction de base de données pour garantir la cohérence des données.
