# Domino OAUTH2 Authorization Server #

Ce projet vous permet de transformer votre serveur Domino en un serveur d'autorisation OAUTH2.
Les éléments fournis sont les suivants :

- Une base oauth2.nsf qui vous permet de déclarer les applications, et qui définit les deux endpoints oauth2 (authorize et token).
- Une application SpringBoot exemple qui héberge un service Rest trivial
- Une application exemple front, sous la forme d'une base Notes. Cette base authentifie l'utilisateur via OAUTH2, et fait des appels authentifiés au service Rest.

## Environnement attendu ##

Pour bien voir se dérouler la danse "OAUTH", associez un nom d'hôte différent à chaque serveur.

- Un serveur Tomcat : par exemple apis.mon-domaine.com
- Un serveur Domino 9.0.1 : par exemple accessible via deux noms d'hôtes 'login.mon-domaine.com' et 'front.mon-domaine.com'

## Déclaration des configurations SSO ##

Les jetons JWT générés seront signés en utilisant des clés SSO standard. Ainsi, elles restent stockées à un endroit sécurisé.
Vous devez commencer par déclarer deux configurations SSO. Notes bien qu'elles n'ont pas besoin d'être associées à un serveur car elles ne serviront pas à Ltpa.

Dans la vue "Servers" du NAB, utilisez l'action "Web/Create Web SSO Configuration" pour créer deux configurations SSO.

Dans chaque document, saisissez :

- Le nom de la configuration: Par exemple "AccessToken" pour la première, et "RefreshToken" pour la seconde.
- Le nom de l'organisation: Le nom de votre société
- DNS Domain: ".votre-domaine.com". Cette information ne sera pas utilisée
- Domino Server Names: Noms des serveurs qui autoriserons les utilisateurs à se logger.

## La base oauth2.nsf ##

### Installation ###

- Déployez la librarie XPage fournie sur votre serveur (même procédure que pour l'extlib)
- Copiez le fichier oauth2.nsf sur votre serveur.
- Vous devez associer le rôle [AppsManager] aux utilisateurs qui souhaiteront ajouter/modifier/supprimer des applications.
- Vous devez associer le rôle [AuthCodeManager] aux administrateurs (ce rôle donne accès à des données très sensibles !)
- Vous devez associer le rôle [SecretExtractor] aux administrateurs (ce rôle est encore plus sensible que le précédent !)
- Notez que default est lecteur. Restreignez ces droits à l'ensemble des utilisateurs qui pourront se logger via OAUTH2.
- Ces utilisateurs doivent aussi avoir le droit de créer des utilisateurs web dans le NAB.
- Signez la base avec l'ID du serveur
- Paramétrage : Ouvrez la base depuis un client Notes, et allez dans la vue "Params". Faites en sorte qu'il y ait un seul document dans cette vue. La valeur attendue pour chaque champ est décrite dans le masque.

### Pour déclarer des applications ###

Connectez vous avec votre navigateur à la XPage "applications.xsp" avec un compte utilisateur ayant le rôle [AppsManager].
L'interface parle d'elle même.

Quand vous déclarez une application, vous définissez :

- La liste des utilisateurs ayant le droit de se connecter dessus. C'est un simple champ lecteur. Utilisez des "," pour plusieurs valeurs.
- L'URL de redirection où le endpoint authorize envera le code autorisation. Si vous mettez en place l'application front Notes, pointez vers sa XPage "init.xsp".

A la fin, l'application sera déclarée dans la base, et un utilisateur nommé <Nom application>/APPLICATION/WEB sera déclaré dans le NAB.
Il aura un mot de passe http.

Notez bien le client-id et le secret qui est généré. Le secret ne pourra plus vous être fourni une fois la page fermée.

### Pour extraire les secrets Ltpa ###

Connectez vous avec votre navigateur à la XPage "secret.xsp" avec un compte utilisateur ayant le rôle [SecretExtractor].
Le secret Ltpa qui servira à signer les token d'accès est affiché. Vous devrez le reporter dans la conf de votre serveur Tomcat.

## La webapp SpringBoot ##

Cette webapp publie un service Rest qui retourne le nom de l'utilisateur courant. Elle doit être déployée sur un serveur Tomcat.

Elle attend un ensemble de variables Spring en paramétrage. Vous pouvez (par exemple) les définir via des variables d'environnement :

- jwt.secret = Le secret JWT. Vous pouvez l'extraire via la XPage 'secret.xsp' de la base oauth2.nsf (utilisez la variable d'environnement JWT_SECRET).
- cors.allowedOrigins = Les domaine qui seront autorisés à appeler vos services Rest via des requêtes CORS. "*" pour autoriser tous les domaines. (utilisez la variable d'environnement CORS_ALLOWEDORIGINS).
 
## L'application Notes front de démo ##

Attention : Si vous l'ouvrez dans Designer, vous devrez installer la library XPage pour pouvoir la compiler (comme l'extlib). Le simple fait de l'ouvrir avec un Designer ne contenant 
pas cette library rend la base non fonctionnelle !

- Copiez le NSF sur votre serveur
- Signez la base avec un utilisateur ayant le droit d'exécuter les agents non restreints (sans ça, les XPages ne s'executeront pas)
- Remarquez que sa LCA laisse l'utilisateur Anonyme se connecter.
- Avec un client Notes, allez dans la vue Params, et remplissez les champs. Les explications en face vous disent quoi mettre. C'est ici que vous devrez reporter le client-id et le secret de l'application déclarée précédemment.

## Testez ##

- Ouvrez la base de démo : http://serveur/sample.nsf/index.html
- Vous allez être redirigé vers l'écran de login par défaut de votre serveur Domino
- Cliquez sur le bouton qui emet une requête CORS vers le serveur Tomcat => Vous obtenez une alerte avec le nom de l'utilisateur courant.
