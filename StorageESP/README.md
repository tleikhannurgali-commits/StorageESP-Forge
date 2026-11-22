# StorageESP 1.1.0 / Forge Minecraft 1.21.11

This is SOURCE CODE, not a compiled mod JAR. Built on the previous working
Forge 61.2.5 project. The previous screen crash fix is retained.

## Changes

* Responsive ClickGUI: virtual 710 x 470 canvas, scaled to fit any GUI Scale.
  Both drawing and clicks are transformed to the same coordinates.
* Scrolling: mouse wheel vertically scrolls the module list; pressing the
  middle mouse button scrolls down a page and wraps to top at the bottom.
* Click a module's switch/card to toggle it; **right-click** Player ESP or
  Storage ESP (or click its "НАСТРОИТЬ" button) to edit its detailed settings.
* Player ESP options: health (HP label), tracer line, distance in meters.
* Storage ESP option: tracer line to containers within 64 blocks.
* Fullbright option under "МИР": client-side Night Vision lighting,
  not a shader/gamma hack. This can have a different color tone from full gamma.
* Saved settings in config/storageesp.properties.

## Build

Forge 61.2.5 / Java 21 + JDK 25 for ForgeGradle / Gradle 9.5.0.
Use the GitHub Actions workflow in .github/workflows/build.yml or BUILD_MOD.cmd.
The new source has NOT yet been compiled or tested in Minecraft. Do not keep
both old and new StorageESP jars in mods at the same time.

## GUI / keyboard

Right Ctrl: menu; wheel or middle-click: scroll; left-click: toggle;
right-click on Player ESP / Storage ESP: configure; Esc: close.
H/J/K/O: old feature hotkeys; M: create mark.
