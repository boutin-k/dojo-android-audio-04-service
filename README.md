# Service

## But du TD
A l'état actuel du projet, la notification ne fonctionne correctement que lorsque l'application est au premier plan. Ce qui serait intéressant, ce serait de pouvoir contrôler la chanson aussi lorsque l'application est en arrière plan.
Nous pouvons résoudre ce problème en utilisant un [Service](https://developer.android.com/guide/components/services).

* Tu vas devoir ajouter un *Service* à ton projet MediaPlayer.
* Tu peux reprendre ton MediaPlayer ou alors tu peux récupérer une version [ici](https://github.com/WildCodeSchool/dojo-android-audio-notification).

## Etapes

### Créer  un Service
* Tu vas devoir créer un Service en mode *bind* avec la MainActivity.
* Déporte le code du lecteur audio dans le Service.

### Refaire fonctionner l'activity
* Tu vas devoir faire en sorte que les boutons PLAY, PAUSE, STOP ainsi que la SeekBar soient synchronisés avec le lecteur audio du service.

### Refaire fonctionner la notification
* Tu vas devoir modifier le premier BroadcastReceiver afin d'envoyer le clique des boutons PLAY, PAUSE et STOP de la notification directement vers le service. Tu auras surement besoin de la méthode [peekService](https://developer.android.com/reference/android/content/BroadcastReceiver.html#peekService(android.content.Context,%20android.content.Intent))
* La notification doit pouvoir contrôler la musique même si l'application n'est pas en foreground.

## Documentation
* [Using MediaPlayer in a service](https://developer.android.com/guide/topics/media/mediaplayer)
* [Android Services tutorials](http://www.vogella.com/tutorials/AndroidServices/article.html)
* [Service](https://developer.android.com/reference/android/app/Service)
