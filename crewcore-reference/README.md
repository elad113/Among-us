# CrewCore V3 APK reference

This folder records the uploaded CrewCore ModLoader APK and the source material embedded inside it.

## Original APK inspected
- Input: `CrewCore_ModLoader-vc67.apk` (49.61 MiB)
- Android application ID: `dev.crewcore.modloader.v3`
- Variant: `googleGameAuthInternal`
- ABI payload: `arm64-v8a`
- Embedded source: `assets/source/crewcore-corresponding-source.zip` and `assets/source/crewcore-modifications.zip`.

## Important source limitation
The APK's own CrewCore loader implementation is proprietary and is explicitly not included in its embedded source offer. The embedded corresponding-source archive covers named third-party components and build scripts, not the full Android application source. This folder is a reference/import snapshot, not a complete rebuildable CrewCore project.

The existing `app/` Android project in this repository is a separate Among Us companion launcher. It is not the source project for the CrewCore APK and has been left intact.

## Next step for full source-level editing
To make direct changes to CrewCore itself, the original Android/Gradle and native/.NET project source (including the proprietary loader source, if you own or are authorized to modify it) is needed. The embedded third-party source offer alone cannot recreate it.

## APK upload limitation
The uploaded 49.61 MiB binary was inspected locally but was not committed: the connected GitHub file-writing interface available in this session supports text files, not binary release-asset uploads.
