Alignement de séquences génomiques
==================================

Ce répertoire contient une application séquentielle de comparaison de séquences génomiques,
qu'il s'agira de paralléliser. Divers schémas de parallélisation étant envisageables, il
s'agira de les évaluer, et de proposer une solution paraissant la meilleure possible.

L'algorithme de comparaison de séquences retenu est un algorithme d'alignement global
pour 2 séquences (Needleman & Wunsch - 1970). Cet algorithme, ainsi qu'un minimum de
contexte applicatif est présenté avec une perspective "informatique" dans les planches 1
à 22 du fichier 'Comparaison de séquences.pdf', contenu dans le répertoire 'documentation'.
Cet algorithme permet de calculer un score de similarité entre 2 séquences. Pour cela, une
table à deux dimensions est construite récursivement, et le résultat final est donné par
l'élément inférieur droit de la table.

L'application proprement dite consiste à rechercher dans une base de séquences quelle est
la séquence la plus proche d'une séquence cible. Pour cela, l'algorithme de comparaison
va être appliqué pour la séquence cible à chacune des séquences de la base, et la (une)
séquence la plus proche sera retenue.

Le contenu de cette archive est le suivant

Répertoire "bases"
--------------------
contient une base de séquences (BD1) et un ensemble de (5) séquences cibles de test.
Il s'agira donc de choisir une séquence de test, et de rechercher la séquence de BD1 la plus
proche de cette séquence.

Pour la petite histoire, BD1 contient à peu près toutesles séquences protéiques "naturelles"
 connues, de longueur inférieure à 200 bases (à l'exception des 5 séquences de test, qui en ont été
extraites)

Notez que l'application développée doit être capable de fonctionner avec des bases nettement
plus volumineuses. Cependant, vous pouvez supposer que vous aurez toujours suffisamment de 
mémoire pour conserver et traiter simultanément une centaine séquences .

Répertoire documentation
------------------------
- 'Comparaison de séquences.pdf' : présentation et illustration de l'algorithme de comparaison
de séquences. A lire nécessairement.
- Sous-répertoire 'Culture générale' : divers documents, dont la lecture n'est pas utile
pour le développement de l'application, mais qui peuvent intéresser les curieux.
	* présentation des algorithmes d'alignement de séquences d'un point de vue plus biologique
	* introduction à la bioinformatique
	* une perspective sur l'utilisation de la programmation dynamique en bioinformatique (en anglais)
	
Répertoire code
---------------
- `Sequence.java` représentation et opérations sur les séquences protéiques contenues dans
les bases de données. Une Séquence est un 5-uplet, dont les deux premières composantes constituent
un identifiant normalisé, la troisième est une description informelle de la molécule ; la
quatrième composante donne la composition de la séquence sous la forme d'une suite de bases. Chaque
base est identifiée par une lettre  majuscule ; la dernière composante est la taille de
la séquence (nombre de bases de la chaîne). La classe `Sequence` fournit 
	* les accesseurs sur ces composantes
	* les scores associés aux opérations envisagées lors de la comparaison de séquences :
	(non)correspondance, insertion suppression. La version proposée est minimaliste (scores
	constants), mais il est possible d'être plus fin (voir la documentation fournie dans ce cas).
	* une opération permettant d'afficher la séquence.
	
- `AccesBD.java` interface d'accès aux bases de séquences. L'opération `lier()` permet
d'associer un fichier aux opérations d'accès. Les opérations d'accès sont la lecture d'une
séquence identifiée par son indice dans la base, et un itérateur qui permet de parcourir
la base séquentiuellement. L'application principale fournit un exemple d'accès à une base.

- `BDSeq.jar` contient une classe `BDSequences` qui implémente `AccèsBD`. Le constructeur
de cette classe ne prend pas de paramètre et est initialement associé à une base vide, 
conformément à ce qui est requis par `AccèsBD`. L'application principale fournit un exemple 
d'accès à une base via la classe `BDSequences`. Notez que l'archive doit être intégrée au
classpath, à la compilation comme à l'exécution.

- `AlignementSeq.java` fournit une version séquentielle de l'application de comparaison,
qu'il faut paralléliser. La méthode `similitude(...)` réalise la comparaison de séquences
proprement dite. Le travail de comparaison aux différentes séquences d'une base est effectué
par deux méthodes :
	* `AMono()` qui travaille sur les séquences directement chargées en mémoire centrale.
	Cette version permet de définir précisément l'algorithmique de l'application, si besoin
	est, mais n'est pas adaptée au modèle Linda, ou les traitements communiquent via un
	espace de tuple.
	* `AMonoLinda()` qui est une transposition de l'algorithme précédent dans le modèle Linda.
	**C'est cet algorithme dont il faut étudier la parallélisation**
	
Enfin la méthode `main` réalise un test basique des méthodes `AMono()` et `AMonoLinda()`.

	* Compilation (en supposant le répertoire `Alignement` inclus dans le répertoire `applications`) :	
	`javac -cp .:../../../..:BDSeq.jar *.java`
	* Exécution (sur les bases fournies) :
	`java -cp .:../../../..:BDSeq.jar AlignementSeq ../bases/BD1 ../bases/seqTest`