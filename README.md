# PLM-NOTHELLO

Une implemantation d'Othello en utilisant de la programmation par contraintes avec OR-Tools.

## Build instructions

Pour compiler et lancer le projet, il faut utiliser la run configuration incluse dans le .idea, ou lancer la commande suivante :

```bash
-c 'mvn clean package && java -jar target/nothello-1.0-SNAPSHOT.jar'
```

Il faut s'assurer d'utiliser la JDK 21 et nous avons eu quelques soucis avec des distributions différentes. Celle de Microsoft fonctionne bien.