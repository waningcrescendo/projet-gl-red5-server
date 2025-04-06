
# Modifications effectuées

Les modifications ont été effectuées dans le package common, car c’est celui que nous avions analysé en priorité dans la partie 1 du projet. De plus, comme c’est l’une des seules qui possède des tests, j’ai préféré faire mes modifications dessus afin de pouvoir vérifier que rien ne casse.

## Modification 1 : Modification de tests pour qu'ils passent
**Lien du commit :** [e85e945](https://github.com/waningcrescendo/projet-gl-red5-server/commit/e85e945bda73e8f0ae5112deda803842a8ddeb58) (ligne 205 dans le commit, on ne voit pas bien car il y a eu des modifications de format en même temps)

Dans le package common, certains tests ne passaient pas. Le test testVectorRoundTrip dans la classe AMF3IOTest ne passait pas parce qu’il tentait de désérialiser directement l’objet Vector<String>. J’ai modifié le test pour qu'il fasse une conversion manuelle, en passant  d’abord par une List de String intermédiaire, pour ensuite utiliser cette liste pour créer l’objet Vector<String>.

**Lien du commit :** [2407d337](https://github.com/waningcrescendo/projet-gl-red5-server/commit/2407d337579f492d93334edbb27274dd21fee548)

Le test testConvertArrayListToSet dans la classe ConversionUtilsTest engendrait une erreur parce qu’il utilisait la méthode ConversionUtils.convert pour convertir une ArrayList en Set. J’ai remplacé la conversion par un constructeur de HashSet qui prend la liste directement en paramètre.

```bash
mvn -Dtest=org.red5.server.net.rtmp.TestRTMPConnection -DfailIfNoTests=false test
```

## Modification 2 : Renommer des variables qui ne respectent pas le pascalcase
**Lien des commits :**
- [33f3d931](https://github.com/waningcrescendo/projet-gl-red5-server/commit/33f3d9312f335c17cddaf892285412ec4c4bf774)
- [9836b2d7](https://github.com/waningcrescendo/projet-gl-red5-server/commit/9836b2d79b526c78be6a6b788f7910380196cd51)

Certaines variables et énumérations ne respectaient pas la convention de nommage de Java. Dans la classe IProviderService, l’énum était appelée INPUT_TYPE, mais les majuscules et le tiret du huit sont pour les variables statiques. J’ai remplacé toutes les occurences par InputType. L’enum était aussi utilisée dans d’autres parties du code, j’ai renommé toutes les occurrences.

Dans la classe PlayEngine, la variable _val ne respectait pas la convention :
```java
 long _val = ParserUtils.parseInteger(inputStream, (int) getSize());
    long val = _val / NANO_MULTIPLIER + DELAY;
    super.setValue(_val);
    value = new Date(val);
```
Je l’ai remplacée par rawValue, et comme je trouvais que la seconde variable val n’était pas assez explicite je l’ai remplacée aussi par adjustedValue.

Dans la classe RTMPHandshake, j’ai remplacé la variable num_rounds (convention python) par numRounds.


## Modification 3 : Suppression du code mort
**Lien du commit :** [a046cbb](https://github.com/waningcrescendo/projet-gl-red5-server/commit/a046cbb7442188f55bdfe37be9f764f9598801f2)

Dans la classe RTMPProtocolDecoder, il y avait beaucoup de code commenté, des traces de log ou des conditions plus utilisées. J’ai supprimé ces lignes, ce qui rend le code plus lisible.

## Modification 4 : Réorganisation du fichier
**Lien du commit :** [f539a1c](https://github.com/waningcrescendo/projet-gl-red5-server/commit/f539a1c8fd67782cf0cedf0844570be1a8ceba9a)

Toujours dans le fichier RTMPProtocolDecoder, les méthodes n’étaient pas dans l’ordre public - protected - private. J’ai réarrangé les méthodes.

## Modification 5 : Nombre magique
**Lien du commit :** [ae65f4c](https://github.com/waningcrescendo/projet-gl-red5-server/commit/ae65f4c3eafeb287b8084a752718a3d1bd287d78?w=1)

Dans la classe RTMPProcotolEncoder, j’ai remplacé des nombres magiques (comme 4 ou 18) écrits en dur dans le code par des constantes nommées, comme SKIP_BYTES, LENGTH_FIELD_SIZE, ou MAX_HEADER_SIZE. Ces valeurs étaient utilisées à plusieurs endroits dans la classe RTMPProtocolEncoder, mais sans explication claire, ce qui rendait le code difficile à comprendre. En créant des constantes avec des noms explicites et des commentaires, le code devient plus lisible et plus facile à maintenir. Par exemple, au lieu de voir out.skip(4);, on voit maintenant out.skip(SKIP_BYTES);, ce qui indique clairement l’intention. Même si certaines constantes ont la même valeur, leur nom donne du sens à leur usage.

## Modification 6 : Réduction lignes méthode
**Lien du commit :** [1fe9f4f](https://github.com/waningcrescendo/projet-gl-red5-server/commit/1fe9f4f059bbd24eb8ac93f4832859d57f1c4669)

Dans la classe RTMPProtocolDecoder, la méthode decodePacket était très longue et difficile à comprendre.

J’ai réalisé une refactorisation de la méthode pour améliorer la lisibilité, la modularité et la maintenabilité du code. Il y avait de nombreux blocs de code effectuant différentes tâches : lecture des en-têtes, gestion des erreurs, construction des paquets, lecture des chunks, décodage du message et mise à jour de l’état RTMP. 

Pour résoudre cela, j’ai extrait plusieurs parties fonctionnelles de la méthode vers des méthodes, chacune avec une responsabilité bien définie :
- handleInvalidHeader gère les cas où l'en-tête est invalide ou vide, en fermant proprement le canal concerné et en loggant l'erreur
- getPacketForChannel récupère un paquet existant ou en crée un nouveau pour le canal donné, centralisant ainsi cette logique
- readChunk lit un morceau de données du buffer d'entrée et l’écrit dans le buffer du paquet, en tenant compte de la taille de chunk configurée
- processDecodedMessage traite le message décodé en mettant à jour l'état RTMP et gère les types de message particuliers 

Ce refactoring rend la méthode decodePacket beaucoup plus concise et lisible, car elle suit désormais un flux d’étapes clairement identifiées, tout en déléguant les détails d’implémentation à des méthodes bien nommées. En plus de rendre le code plus facile à comprendre, cette approche facilite également les tests unitaires et le débogage, puisque chaque méthode peut être testée indépendamment. 


## Modification 7 : Remplacement d’un return -1 par une exception  
**Lien du commit :** [73e6c409](https://github.com/waningcrescendo/projet-gl-red5-server/commit/73e6c4098ecfa7db845f56bce6694add3113ede2)

Dans la classe RTMPUtils, j’ai remplacé la valeur de retour -1 dans le cas par défaut par une exception explicite IllegalArgumentException. Avant, lorsqu’un marqueur de taille d’en-tête inconnu était passé en paramètre, la méthode retournait simplement -1. Cela pouvait masquer des erreurs ou provoquer des comportements imprévisibles ailleurs dans le programme, surtout si cette valeur était utilisée sans vérification.

## Modification 8 : Ajout de tests  
**Lien des commits :**  
- [39cafaed](https://github.com/waningcrescendo/projet-gl-red5-server/commit/39cafaed5ed13dea73e84898edf5222c4a8ef8e1)  

Après la modification précédente, je voulais lancer les tests associés à la classe pour vérifier que cela passait encore, mais j’ai vu qu’aucun test n’avait été implémenté. J'ai donc implémenté deux tests unitaires. Le premier test testCompareTimestamps vérifie que la méthode compareTimestamps compare correctement deux valeurs de timestamps et renvoie les résultats attendus dans différents cas de figure. Le second test, testDiffTimestamps, vérifie que la méthode diffTimestamps calcule correctement la différence entre deux timestamps. Ces tests garantissent que les modifications apportées à la classe n'ont pas introduit de régressions et que les méthodes manipulant les timestamps continuent de fonctionner comme prévu. 

- [a34ede15](https://github.com/waningcrescendo/projet-gl-red5-server/commit/a34ede157a0714e3cc7f81a9c0f74dcd5cb34755)

J'ai remarqué qu'il n'y avait pas beaucoup de tests dans la classe RTMPProtocolDecoder, donc j'en ai ajouté plusieurs pour couvrir des cas importants. J'ai ajouté un test pour vérifier le comportement du décodeur avec un buffer vide, un autre pour tester un paquet corrompu et enfin un dernier pour tester des données AMF mal formées. Ces tests permettent de s'assurer que le décodeur gère correctement ces situations sans planter et qu'il renvoie des résultats appropriés même en cas d'erreur dans les données.


## Modification 9 : Suppression de duplication de code dans méthode  
**Lien du commit :** [b493839](https://github.com/waningcrescendo/projet-gl-red5-server/commit/b4938398254354738e0d17939accdfe90e9d05dd)

Dans la classe RTMPHandler, j'ai refactorisé le code pour centraliser la gestion des erreurs liées aux appels de service. Avant, chaque bloc où une erreur se produisait lors d'une connexion ou d'un appel de service répétait la même logique pour gérer l'erreur, c'est-à-dire définir le statut de l'appel, créer un StatusObject et envoyer la réponse. Cela rendait le code assez difficile à lire.

J'ai donc créé une méthode privée, setCallError(), qui centralise cette logique. Cette méthode prend plusieurs paramètres : l'appel de service (IServiceCall), le statut à affecter à l'appel, un code de statut, une description de l'erreur et un canal de communication (Channel). Elle met à jour le statut de l'appel, crée un objet StatusObject avec le code de statut fourni, et si nécessaire, envoie le résultat sur le canal. Cela permet de simplifier le code en réduisant la duplication et d'améliorer sa lisibilité et sa maintenabilité.

Ce refactoring rend le code plus propre et plus modulaire en centralisant la gestion des erreurs, ce qui facilite sa maintenance à long terme.


## Modification 10 : Suppression d’un paramètre inutile  
**Lien du commit :** [3f97941](https://github.com/waningcrescendo/projet-gl-red5-server/commit/3f9794123d780dc5a475f65f833b8b4af02f9fff)

Dans `StreamService`, le paramètre `transition` n’était pas utilisé dans la méthode `play2`, je l’ai donc supprimé ainsi que sa JavaDoc.

## Modification 11 : Ajout d’une super classe pour supprimer des méthodes dupliquées  
**Lien du commit :** [ef4d607](https://github.com/waningcrescendo/projet-gl-red5-server/commit/ef4d6077c03335beabcfc8f89af55144ba7f41fd)

La classe Aggregate gérait elle-même la logique des buffers, de la sérialisation et désérialisation et de la libération des ressources. Cette approche entraînait une duplication de code dans différentes classes comme AudioData et VideoData, ce qui compliquait la maintenance et augmentait le risque d'erreurs. En plus, la classe était trop volumineuse et difficile à étendre ou à modifier sans impacter d'autres parties du code.

Il y avait de la répétition de code, la gestion des buffers et de la sérialisation étant dupliquée dans plusieurs classes, chaque modification nécessitait de réajuster plusieurs parties du code, ce qui devenait complexe et source d'incohérences.

J'ai introduit une classe abstraite BaseStreamData pour centraliser la gestion des buffers, de la sérialisation et de la libération des ressources. Aggregate et les autres classes spécifiques héritent désormais de cette classe de base, ce qui leur permet de se concentrer uniquement sur leur logique propre, comme la découpe des données.


Cette refactorisation a rendu le code plus modulaire et réutilisable. La gestion des ressources est maintenant centralisée, réduisant la duplication et facilitant les modifications futures. Le code est ainsi plus lisible, maintenable et moins sujet aux erreurs, car toute modification dans la gestion des buffers ou de la sérialisation se fait désormais à un seul endroit.

## Remarques  
J'ai tenté de refactoriser la classe RTMPconnection que nous avions mentionnée dans le rapport mais la tâche s'est avérée trop complexe et, par manque de temps, je n'ai pas pu mener à bien une refactorisation complète. Plutôt que de risquer d'introduire des erreurs en modifiant trop de choses à la fois, j'ai préféré faire pour des petites et moyennes modifications ciblées, en simplifiant certaines parties du code et en améliorant la lisibilité et la gestion des ressources, pour ne pas perturber l'ensemble du système.

De plus, comme ce projet n’a pas beaucoup de tests, j’avais peur de modifier la classe et que tout s’écroule derrière.


