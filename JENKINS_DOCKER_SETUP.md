# 🚀 Jenkins Docker Setup - Pipeline Visualization

## ⚡ Quick Start (1 Command)

### **Linux/macOS:**
```bash
./start-jenkins.sh
```

### **Windows PowerShell:**
```powershell
.\start-jenkins.ps1
```

**That's it!** 🎉 Your Jenkins with complete pipeline visualization will be ready in ~3-5 minutes.

---

## 📋 What You Get

### **🎯 Complete Jenkins Setup:**
✅ **Jenkins LTS** avec interface web complète
✅ **Blue Ocean** pour visualisation moderne des pipelines
✅ **Tous les plugins** pré-installés et configurés
✅ **Utilisateur admin** configuré automatiquement
✅ **Outils globaux** (Maven, Node.js, Docker) configurés
✅ **Pipeline job** créé et prêt à utiliser
✅ **Docker-in-Docker** pour builds containerisés
✅ **Volumes persistants** pour données Jenkins

### **📊 Interfaces de Visualisation:**
- 🎨 **Blue Ocean** (Moderne): http://localhost:8080/blue
- 📈 **Dashboard classique**: http://localhost:8080
- 📊 **Build Monitor**: http://localhost:8080/view/Pipeline%20Monitor/
- 🔍 **Stage View**: Dans chaque job

---

## 🏗️ Architecture du Setup

```
┌─────────────────────────────────────────────────────────────┐
│                    JENKINS DOCKER STACK                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐               │
│  │  Jenkins Master │    │  Jenkins Agent  │               │
│  │                 │    │                 │               │
│  │ • Web UI        │◄──►│ • Docker builds │               │
│  │ • Blue Ocean    │    │ • Maven builds  │               │
│  │ • Job Scheduler │    │ • Node.js builds│               │
│  │ • Plugin Engine │    │ • Test execution│               │
│  └─────────────────┘    └─────────────────┘               │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                     PERSISTENT VOLUMES                     │
├─────────────────────────────────────────────────────────────┤
│ • jenkins_data        • maven_cache      • npm_cache      │
│ • docker_cache        • agent_workdir                      │
└─────────────────────────────────────────────────────────────┘
```

### **Composants inclus:**

| Service | Description | Port | Volume |
|---------|-------------|------|--------|
| **jenkins** | Master Jenkins avec UI | 8080 | jenkins_data |
| **jenkins-agent** | Agent pour exécution builds | 50000 | agent_workdir |
| **Config auto** | Configuration automatique | - | jenkins_data |

---

## 🚀 Démarrage Étape par Étape

### **1. Prérequis**
```bash
# Vérifier Docker
docker --version    # Doit afficher la version

# Vérifier Docker Compose
docker-compose --version    # Doit afficher la version

# Vérifier que Docker fonctionne
docker info    # Doit afficher les infos système
```

### **2. Lancement Automatique**

#### **Option A: Script automatique (Recommandé)**
```bash
# Linux/macOS
chmod +x start-jenkins.sh
./start-jenkins.sh

# Windows PowerShell
.\start-jenkins.ps1
```

#### **Option B: Commandes manuelles**
```bash
# Créer le réseau
docker network create microservices-network

# Construire et démarrer Jenkins
docker-compose -f docker-compose.jenkins.yml build
docker-compose -f docker-compose.jenkins.yml up -d

# Attendre que Jenkins démarre (3-5 minutes)
# Vérifier: curl http://localhost:8080/login
```

### **3. Connexion**
```
URL: http://localhost:8080
Utilisateur: admin
Mot de passe: admin123
```

---

## 🎨 Visualisation Pipeline

### **🌟 Blue Ocean (Interface Recommandée)**

**URL:** http://localhost:8080/blue

**Fonctionnalités:**
```
┌─────────────────────────────────────────────────────────┐
│                   🎨 BLUE OCEAN                        │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Pipeline: transport-microservices-pipeline            │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐          │
│  │ Setup  │─│ Build  │─│ Test   │─│ Deploy │          │
│  │   ✅   │ │   🔄   │ │   ⏸️   │ │   ⏸️   │          │
│  └────────┘ └────────┘ └────────┘ └────────┘          │
│                                                         │
│  📊 Progress: 45% | ⏱️ Duration: 8m 23s               │
│  🧪 Tests: 1,247 ✅ | 📈 Coverage: 87%                │
│                                                         │
│  [View Logs] [Artifacts] [Changes] [Tests]             │
└─────────────────────────────────────────────────────────┘
```

**Avantages Blue Ocean:**
- ✅ **Interface moderne** et intuitive
- ✅ **Visualisation en temps réel** des stages
- ✅ **Navigation facile** entre les builds
- ✅ **Logs intégrés** avec recherche
- ✅ **Vue par branche** Git
- ✅ **Éditeur visuel** de pipeline

### **📊 Dashboard Classique**

**URL:** http://localhost:8080

**Vues disponibles:**
```
┌─────────────────────────────────────────┐
│           📊 JENKINS DASHBOARD          │
├─────────────────────────────────────────┤
│                                         │
│ 🏠 Microservices Dashboard (Défaut)    │
│ 📋 All Pipelines                       │
│ 🖥️ Pipeline Monitor                    │
│ 📈 Build History                       │
│                                         │
│ Jobs:                                   │
│ ├─ transport-microservices-pipeline    │
│ │  Status: ✅ SUCCESS #42             │
│ │  Duration: 12m 34s                  │
│ │  Coverage: 87%                      │
│ │  Last run: 5 minutes ago            │
│ │                                     │
│ └─ [Build Now] [Configure] [Workspace] │
└─────────────────────────────────────────┘
```

### **📈 Stage View**

Dans chaque job, onglet "Stage View":
```
Transport Microservices Pipeline - Build #42
┌──────────────────────────────────────────────────────────────┐
│ Stage 1  │ Stage 2  │ Stage 3  │ Stage 4  │ Stage 5        │
│ Checkout │ Setup    │ Quality  │ Build    │ Test           │
│    ✅   │    ✅   │    ✅   │    ✅   │    ✅            │
│   30s    │   45s    │  2m 15s  │  4m 30s  │  3m 20s        │
└──────────────────────────────────────────────────────────────┘
│ Stage 6  │ Stage 7  │ Stage 8  │ Stage 9  │ Stage 10       │
│ Docker   │ Security │ E2E      │ Registry │ Deploy         │
│    ✅   │    ✅   │    ✅   │    ✅   │    ✅            │
│  3m 45s  │  1m 30s  │  2m 45s  │   45s    │  1m 20s        │
└──────────────────────────────────────────────────────────────┘

Total Duration: 12m 34s | Success Rate: 96% | Test Coverage: 87%
```

---

## 🔧 Gestion Jenkins

### **Commandes Utiles**

#### **Contrôle des services:**
```bash
# Démarrer Jenkins
./start-jenkins.sh start

# Arrêter Jenkins
./start-jenkins.sh stop

# Redémarrer Jenkins
./start-jenkins.sh restart

# Voir les logs
./start-jenkins.sh logs

# Statut des containers
./start-jenkins.sh status
```

#### **Gestion des données:**
```bash
# Nettoyer complètement (ATTENTION: supprime tout!)
./start-jenkins.sh clean

# Sauvegarder les données
docker run --rm -v jenkins_microservices_data:/data -v $(pwd):/backup alpine tar czf /backup/jenkins-backup.tar.gz /data

# Restaurer les données
docker run --rm -v jenkins_microservices_data:/data -v $(pwd):/backup alpine tar xzf /backup/jenkins-backup.tar.gz
```

#### **Debug et maintenance:**
```bash
# Accéder au container Jenkins
docker exec -it jenkins-microservices bash

# Voir les logs détaillés
docker-compose -f docker-compose.jenkins.yml logs -f jenkins

# Vérifier les volumes
docker volume ls | grep jenkins

# Voir l'utilisation des ressources
docker stats jenkins-microservices
```

---

## 📦 Configuration Incluse

### **🔌 Plugins Pré-installés**
```
✅ Blue Ocean (Interface moderne)
✅ Pipeline Stage View (Visualisation stages)
✅ Maven Plugin (Builds Java)
✅ NodeJS Plugin (Builds JavaScript)
✅ Docker Plugin (Containerisation)
✅ Kubernetes Plugin (Déploiement K8s)
✅ JUnit Plugin (Résultats tests)
✅ JaCoCo Plugin (Couverture code)
✅ Checkstyle Plugin (Qualité code)
✅ Email Extension (Notifications)
✅ Build Monitor View (Monitoring)
✅ Git Plugin (Source control)
```

### **🛠️ Outils Configurés**
```
✅ Maven 3.9.0 (Auto-installé)
✅ Node.js 18.x (Auto-installé)
✅ Docker (Intégré)
✅ Git (Système)
✅ kubectl (Pour Kubernetes)
```

### **👤 Utilisateur Admin**
```
Username: admin
Password: admin123

Permissions:
✅ Administration complète
✅ Création/modification jobs
✅ Gestion des plugins
✅ Configuration système
```

---

## 🔄 Pipeline Configuration

### **Job Pré-configuré**
```
Nom: transport-microservices-pipeline
Type: Pipeline multibranch
Source: Git (votre repository)
Jenkinsfile: ./Jenkinsfile
Branches: main, develop, feature/*
```

### **Paramètres du Pipeline**
```
🎯 DEPLOYMENT_ENVIRONMENT: staging/production/local
⚠️ SKIP_TESTS: false/true
🚀 DEPLOY_TO_K8S: true/false
🔒 RUN_SECURITY_SCANS: true/false
🐳 DOCKER_NAMESPACE: transport-microservices
📝 LOG_LEVEL: INFO/DEBUG/WARNING
```

### **Triggers Configurés**
```
✅ SCM Polling: toutes les 5 minutes
✅ Webhook GitHub: sur push
✅ Build nocturne: 2h du matin
✅ Build manuel: toujours disponible
```

---

## 📊 Monitoring et Métriques

### **Dashboard Principal**
```
┌─────────────────────────────────────────────────────┐
│               🏠 MICROSERVICES DASHBOARD           │
├─────────────────────────────────────────────────────┤
│                                                     │
│ 📊 Build Statistics:                               │
│    Success Rate: 96% (last 20 builds)              │
│    Average Duration: 12m 34s                       │
│    Builds Today: 8                                  │
│                                                     │
│ 🧪 Test Results:                                   │
│    Total Tests: 1,247                              │
│    Success Rate: 99.8%                             │
│    Coverage: 87%                                    │
│                                                     │
│ 🔒 Security Status:                                │
│    Critical Issues: 0                               │
│    High Issues: 3                                   │
│    Last Scan: 2 hours ago                          │
│                                                     │
│ 🚀 Deployment Status:                              │
│    Staging: ✅ Deployed (v1.2.3)                  │
│    Production: ✅ Deployed (v1.2.2)               │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### **Monitoring en Temps Réel**
```
🖥️ Large Screen Monitor: http://localhost:8080/view/Pipeline%20Monitor/

┌─────────────────────────────────────────────────────────┐
│                  📺 PIPELINE MONITOR                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ transport-microservices-pipeline                       │
│ ████████████████████████████████████████ 85% Complete  │
│ Stage: Security Scanning (2/8)                         │
│ ETA: 3m 15s                                            │
│                                                         │
│ Last 5 Builds:  ✅✅✅❌✅                             │
│ Duration Trend: 📈 +15s (avg last week)               │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🚨 Troubleshooting

### **Problèmes Courants**

#### **1. Jenkins ne démarre pas**
```bash
# Vérifier Docker
docker info

# Vérifier les ports
netstat -tulpn | grep 8080

# Voir les logs d'erreur
docker-compose -f docker-compose.jenkins.yml logs jenkins

# Solution:
./start-jenkins.sh stop
./start-jenkins.sh clean
./start-jenkins.sh start
```

#### **2. Page inaccessible après démarrage**
```bash
# Attendre plus longtemps (premier démarrage = 5-10 min)
curl -f http://localhost:8080/login

# Vérifier le statut des containers
./start-jenkins.sh status

# Redémarrer si nécessaire
./start-jenkins.sh restart
```

#### **3. Build fails - "No space left on device"**
```bash
# Nettoyer les images Docker inutiles
docker system prune -a

# Nettoyer les volumes Jenkins
./start-jenkins.sh clean
./start-jenkins.sh start
```

#### **4. Plugins manquants ou erreurs**
```bash
# Reconstruire l'image Jenkins
docker-compose -f docker-compose.jenkins.yml build --no-cache

# Vérifier les plugins installés dans Jenkins UI:
# Manage Jenkins > Manage Plugins > Installed
```

### **Logs et Debug**
```bash
# Logs Jenkins en temps réel
./start-jenkins.sh logs

# Logs de tous les services
docker-compose -f docker-compose.jenkins.yml logs -f

# Logs d'un container spécifique
docker logs jenkins-microservices -f

# Accès shell au container
docker exec -it jenkins-microservices bash

# Vérifier l'espace disque
docker system df
```

---

## 🔐 Sécurité

### **Configuration par Défaut**
```
✅ Utilisateur admin sécurisé
✅ CSRF protection activée
✅ Agent-to-master security enabled
✅ Scripts approval requis
✅ Credentials encrypted
```

### **Améliorer la Sécurité**
```bash
# Changer le mot de passe admin
# Via Jenkins UI: Manage Jenkins > Manage Users > admin

# Ajouter HTTPS (optionnel)
# Modifier docker-compose.yml pour inclure certificats SSL

# Configurer LDAP/Active Directory
# Via Jenkins UI: Manage Jenkins > Configure Global Security
```

---

## 📱 Accès Mobile & Remote

### **URLs d'Accès**
```
🖥️ Desktop:    http://localhost:8080
📱 Mobile:     http://localhost:8080 (responsive)
🌐 Remote:     http://[your-ip]:8080
☁️ Blue Ocean: http://localhost:8080/blue (optimal mobile)
```

### **API Access**
```bash
# Status API
curl http://admin:admin123@localhost:8080/api/json

# Trigger build
curl -X POST http://admin:admin123@localhost:8080/job/transport-microservices-pipeline/build

# Get build info
curl http://admin:admin123@localhost:8080/job/transport-microservices-pipeline/lastBuild/api/json
```

---

## ✅ Checklist de Vérification

### **Après Installation:**
```
□ Jenkins accessible: http://localhost:8080 ✅
□ Login admin/admin123 fonctionne ✅
□ Blue Ocean accessible: http://localhost:8080/blue ✅
□ Job transport-microservices-pipeline créé ✅
□ Plugins installés et actifs ✅
□ Outils globaux configurés ✅
□ Docker-in-Docker fonctionne ✅
□ Volumes persistants créés ✅
```

### **Test Pipeline:**
```
□ Build du pipeline réussit ✅
□ Stages parallèles s'exécutent ✅
□ Tests unitaires passent ✅
□ Images Docker se construisent ✅
□ Rapports générés (tests, coverage) ✅
□ Notifications configurées ✅
```

---

## 🚀 Next Steps

### **Après Setup Réussi:**

1. **🎨 Personnaliser Blue Ocean:**
   - Ajouter des favoris
   - Configurer les notifications
   - Créer des vues personnalisées

2. **📊 Améliorer le Monitoring:**
   - Ajouter Grafana/Prometheus
   - Configurer les alertes
   - Monitoring de performance

3. **🔗 Intégrations:**
   - GitHub webhooks
   - Slack notifications
   - JIRA integration
   - SonarQube quality gates

4. **🔒 Sécurité Avancée:**
   - HTTPS/SSL
   - LDAP authentication
   - Role-based access
   - Security scanning integration

---

## 🎉 Félicitations !

**Votre Jenkins est maintenant prêt pour une visualisation complète de votre pipeline SOA microservices !**

### **🎯 Accès Rapide:**
- **🎨 Interface moderne:** http://localhost:8080/blue
- **📊 Dashboard complet:** http://localhost:8080
- **🔧 Gestion:** `./start-jenkins.sh [command]`

**Happy Building! 🚀**