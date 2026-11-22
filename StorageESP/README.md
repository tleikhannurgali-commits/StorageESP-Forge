# Storage ESP · Liquid Glass

Client-side Minecraft Forge mod project for Minecraft Java Edition **1.21.11**.

- Forge: `61.2.5`
- Java: 21 (JDK 21)
- ForgeGradle: 7
- Gradle: 9.5.0 (installed by GitHub Actions)
- Open GUI: **Right Control**
- Soft lavender / translucent Liquid Glass click GUI in `ModScreen.java`.

## Build on GitHub — no IntelliJ required

1. Upload the **contents of this extracted project folder** to the root of your GitHub repo, preserving `.github/workflows/build.yml` and `src/...` directory structure. Do **not** upload only the ZIP — GitHub does not unpack it automatically.
2. GitHub Actions starts a build on every push to `main`. Open the **Actions** tab, then the **Build Storage ESP 1.21.11** workflow.
3. When the job is green, open the run and download the **StorageESP-Forge-1.21.11** artifact under **Artifacts**. Unzip it to get the `.jar`.

If the build is red, open the failed step and share the build logs. The Java sources were ported from an earlier project and **have not yet been confirmed to compile** with the selected Forge API. Do not assume the JAR will be valid until the workflow succeeds and the mod is tested in-game.

## Notes

This repository uses the latest Liquid Glass GUI source from the supplied ZIP and replaces its older ForgeGradle 6 settings with the ForgeGradle 7 build format. All original feature classes are preserved.

## Important compatibility notes

- This is **Forge**, not Fabric. A Fabric 1.21.4 profile cannot load this mod.
- For Minecraft **1.21.11**, use **Forge 61.2.5** and **Java 21**.
- The build requires external downloads and the source has not been verified to compile in a complete Forge environment.
- Do not rename the source ZIP to .jar: this does not build a mod.
