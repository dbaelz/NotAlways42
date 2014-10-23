Not Always 42
================================
Not Always 42 is a puzzle game where you must guess a number between 1 and 5. You can play alone or against other players. The game was created as a demo for my [Google Play Game Services](https://developers.google.com/games/services/) talk on [Google DevFest Karlsruhe 2014](http://www.gdg-karlsruhe.de/devfest/2014). It currently supports Achievements, Leaderboards, Saved Games, Events/Quests and Turn-based Multiplayer. The app can also be found on the [Google Play Store](https://play.google.com/store/apps/details?id=de.dbaelz.na42).

Build
-------------
The projects can be build with the integrated Gradle wrapper. To set up this application a new project must be created in the Google Play Developer console. Don't forget to create the Achievements, Leaderboards and Events. Then replace the values ​​in the [gpgs.xml](https://github.com/dbaelz/NotAlways42/blob/master/app/src/main/res/values/gpgs.xml) with the new values. See this [Documentation](https://developers.google.com/games/services/console/enabling) for detailed configuration information.

Signing
-------------
To sign your Android app with gradle use the property [NotAlways42.signing](https://github.com/dbaelz/NotAlways42/blob/master/app/build.gradle#L30) and external config files for the keystore informations. [Tim Roes](https://github.com/timroes) wrote a very informative blog post about [handling signing configs](https://www.timroes.de/2013/09/22/handling-signing-configs-with-gradle/) from which the example below was derived.

Note that property _NotAlways42.signing_ has to point to the __folder__ of your keystores and not to the filename of the keystore file!

```groovy
android {
  signingConfigs {
    release {
      storeFile file(project.property("NotAlways42.signing") + "/release.keystore")
      storePassword "KEYSTORE_PASSWORD"
      keyAlias "KEY_ALIAS_RELEASE"
      keyPassword "KEY_PASSWORD"
    }

    debug {
      storeFile file(project.property("NotAlways42.signing") + "/debug.keystore")
      storePassword "android"
      keyAlias "androiddebugkey"
      keyPassword "android"
    }
  }
 
  buildTypes {
    release {
      signingConfig signingConfigs.release
    }

	debug {
      signingConfig signingConfigs.debug
    }
  }
}
```

License
-------------
Not Always42 is licensed under the [Apache 2 License](https://github.com/dbaelz/NotAlways42/blob/master/LICENSE).
