# API
See [ClientInterface](api/src/main/java/net/runelite/ClientInterface.txt) for examples on how to directly use the api.\
See [interfaces](api/src/main/java/net/runelite/api) for the implemented interfaces added through patching. The api is very small because most uses are covered by the rl api.\
An up-to-date jar file of the api can be found [here](https://github.com/Bloodspawns/c0603cb96187d5c295173c5c90d3b389671964dab55056f913c3d86c3333300b/releases/download/1.0/bluelite-api.jar).

## Socket
The base [Socket](https://github.com/c13-c/Socket) plugin is included in this repository as well as in the client to avoid dependency issues.

## Including this api into your gradle project
Inside the gradle file of the submodule, for example module plugins (plugins.gradle.kts), add the following:
```
repositories {
   ivy {
       url = uri("https://github.com/")
       patternLayout {
           artifact("/[organisation]/c0603cb96187d5c295173c5c90d3b389671964dab55056f913c3d86c3333300b/releases/download/[revision]/[module].[ext]")
       }
       metadataSources { artifact() }
   }
   maven(url = "https://repo.runelite.net/net/runelite/runelite-api/maven-metadata.xml") // used to fetch runelite dependencies
}
```
Then add `compileOnly("Bloodspawns:bluelite-api:1.0")` to `dependencies`. If the dependency does not update you can force it to be downloaded again on next gradle sync by deleting the current one.
To do this, in intellij, in the project window expand External Libraries and look for `Bloodspawns:bluelite-api:1.0` then expand that and delete the jar file. Next use gradle sync to download the newest version of the jar file again.
**Do not include any of the api files in your plugin jar since this can cause trouble by over shadowing the api class files managed by the client.**