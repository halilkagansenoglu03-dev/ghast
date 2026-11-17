# Ghast Quit (Fabric 1.21.11, client-side)

Only ghasts count. If none is loaded for 60 seconds in a row, you are
disconnected from the multiplayer server. A countdown is always shown above
your hotbar. In singleplayer it only shows the timer (test mode) and never kicks.

Change the time: edit SECONDS_WITHOUT_GHAST in
src/main/java/com/ghastquit/GhastQuitClient.java

The build file is .github/workflows/build.yml (hidden folder - if your upload
skips it, create it by hand on GitHub). The finished mod is ghastquit-jar,
containing ghastquit-1.0.0.jar.
