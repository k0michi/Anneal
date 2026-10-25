# Anneal

Anneal is yet another approach to running Minecraft Forge 1.7.10 on modern Java (Java 9 and later).

Instead of shipping a modified Forge, Anneal is a small jar of patched classes that is put ahead of the official,
unmodified Forge 1.7.10-10.13.4.1614 universal jar on the classpath. The installer fetches the official Forge jar from
Forge's maven. Along with the patched classes, Anneal swaps LaunchWrapper for
[RetroFuturaBootstrap](https://github.com/GTNewHorizons/RetroFuturaBootstrap) and LWJGL 2 for
[DuaLWJGL](https://github.com/k0michi/dualwjgl), which runs LWJGL 2 code on LWJGL 3.

> [!WARNING]
> Anneal is experimental. Expect crashes and broken mods, and back up your worlds.

## Goals

- Run plain Forge 1.7.10 on Java 9 and later, with as few changes to Forge as possible.
- Run well-behaved mods on Java 9 and later.

## Non-goals

- Making every mod work. Mods that depend on Java 8 internals, LWJGL 2 internals or LaunchWrapper details may break.
- Providing a modding toolchain for modern Java. See [lwjgl3ify](https://github.com/GTNewHorizons/lwjgl3ify) for that.

## Installation

Download `anneal-1.7.10-<version>-installer.jar` from the
[releases](https://github.com/k0michi/Anneal/releases).

### Client

1. Run the installer (`java -jar anneal-1.7.10-<version>-installer.jar`) and choose **Install client**.
2. Start the **Anneal** profile in the launcher. The version asks the launcher for Java 21.
3. Put mods in the `mods` folder of the game directory, as with Forge.

### Server

1. Install the server into the current directory:

   ```
   java -jar anneal-1.7.10-<version>-installer.jar --installServer
   ```

2. Start it with the installed `anneal-1.7.10-<version>.jar`. `java -jar` does not work, as the server must start
   through RetroFuturaBootstrap:

   ```
   java -Djava.system.class.loader=com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader \
     -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.net=ALL-UNNAMED \
     -cp anneal-1.7.10-<version>.jar com.gtnewhorizons.retrofuturabootstrap.Main \
     --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui
   ```

Anneal is not required on both sides of a connection: it accepts clients and servers that run plain Forge.

### Tested

- Server: Java 8, 17, 21 and 25.
- Client: Java 21 on macOS (arm64). Windows and Linux are untested.

## Building

Anneal builds with JDK 8 and the ForgeGradle 1.2 fork at [k0michi/ForgeGradle](https://github.com/k0michi/ForgeGradle).
Install it to your local Maven repository first:

```
git clone https://github.com/k0michi/ForgeGradle
cd ForgeGradle
./gradlew install
```

Then build Anneal with its Gradle, telling it which ForgeGradle version was installed (`./gradlew -q properties` in
ForgeGradle prints it as `version:`):

```
FG_VERSION=<ForgeGradle version> ../ForgeGradle/gradlew --init-script ../ForgeGradle/.github/ci/use-local-fg.gradle setupForge packageInstaller
```

The installer is written to `build/distributions` and the patched classes jar to `build/libs`. The version comes from
the latest `anneal/1.7.10/<version>` tag. `.github/workflows/build.yml` runs the same steps.

## License

Code written for Anneal is under the MIT License. Files from Forge Mod Loader and Minecraft Forge, and the changes made
to them, stay under their own licenses. See [LICENSE-anneal.txt](LICENSE-anneal.txt).

Anneal is not affiliated with Minecraft Forge, Mojang or Microsoft.
