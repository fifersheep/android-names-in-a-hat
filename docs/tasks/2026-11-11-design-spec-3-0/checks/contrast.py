"""WCAG contrast check for the NAM-2 brand directions. Usage: python3 -I contrast.py"""
P = {
 "ticket": {
  "light": dict(primary="#9C4400", onPrimary="#FFFFFF", primaryContainer="#FFDBCA", onPrimaryContainer="#341100",
                secondaryContainer="#F6DED3", onSecondaryContainer="#2C160D", tertiary="#006874", tertiaryContainer="#9EEFFD",
                onTertiaryContainer="#001F24", surface="#FFF8F5", surfaceContainer="#F9EBE4", surfaceContainerHigh="#F3E5DE",
                onSurface="#221A16", onSurfaceVariant="#53443C", outline="#85736A", outlineVariant="#D8C2B8"),
  "dark": dict(primary="#FFB68F", onPrimary="#552000", primaryContainer="#773300", onPrimaryContainer="#FFDBCA",
               secondaryContainer="#5B4135", onSecondaryContainer="#FFDBCC", tertiary="#82D3E0", tertiaryContainer="#004F58",
               onTertiaryContainer="#9EEFFD", surface="#1A120E", surfaceContainer="#271E1A", surfaceContainerHigh="#322824",
               onSurface="#F0DFD8", onSurfaceVariant="#D8C2B8", outline="#A08D84", outlineVariant="#53443C"),
 },
 "tophat": {
  "light": dict(primary="#4D4BB3", onPrimary="#FFFFFF", primaryContainer="#E2DFFF", onPrimaryContainer="#0B006B",
                secondaryContainer="#E3E0F9", onSecondaryContainer="#1A1A2C", tertiary="#7A5900", tertiaryContainer="#FFDEA3",
                onTertiaryContainer="#261900", surface="#FCF8FF", surfaceContainer="#F0ECF7", surfaceContainerHigh="#EAE6F1",
                onSurface="#1B1B21", onSurfaceVariant="#47464F", outline="#787680", outlineVariant="#C8C5D0"),
  "dark": dict(primary="#C3C0FF", onPrimary="#1F1A86", primaryContainer="#35319A", onPrimaryContainer="#E2DFFF",
               secondaryContainer="#46455A", onSecondaryContainer="#E3E0F9", tertiary="#F5BE48", tertiaryContainer="#5C4200",
               onTertiaryContainer="#FFDEA3", surface="#131318", surfaceContainer="#1F1F25", surfaceContainerHigh="#2A292F",
               onSurface="#E4E1E9", onSurfaceVariant="#C8C5D0", outline="#928F99", outlineVariant="#47464F"),
 },
 "chalk": {
  "light": dict(primary="#006B5F", onPrimary="#FFFFFF", primaryContainer="#9FF2E2", onPrimaryContainer="#00201C",
                secondaryContainer="#CCE8E2", onSecondaryContainer="#051F1C", tertiary="#6A5F00", tertiaryContainer="#F7E570",
                onTertiaryContainer="#201C00", surface="#F5FBF8", surfaceContainer="#E9EFEC", surfaceContainerHigh="#E3EAE7",
                onSurface="#171D1B", onSurfaceVariant="#3F4946", outline="#6F7976", outlineVariant="#BEC9C5"),
  "dark": dict(primary="#83D5C6", onPrimary="#003731", primaryContainer="#005047", onPrimaryContainer="#9FF2E2",
               secondaryContainer="#334B47", onSecondaryContainer="#CCE8E2", tertiary="#DAC957", tertiaryContainer="#4F4800",
               onTertiaryContainer="#F7E570", surface="#0E1513", surfaceContainer="#1B2120", surfaceContainerHigh="#252B2A",
               onSurface="#DEE4E1", onSurfaceVariant="#BEC9C5", outline="#899390", outlineVariant="#3F4946"),
 },
}
# (fg, bg, minimum, what)
PAIRS = [
 ("onPrimary", "primary", 4.5, "Draw button label"),
 ("onPrimaryContainer", "primaryContainer", 4.5, "primary container text"),
 ("onSecondaryContainer", "secondaryContainer", 4.5, "chip IN text"),
 ("onSurfaceVariant", "surface", 4.5, "chip OUT text"),
 ("outline", "surface", 3.0, "chip OUT border (UI)"),
 ("onSurfaceVariant", "surfaceContainerHigh", 4.5, "chip DRAWN text (struck)"),
 ("onSurface", "surface", 4.5, "body text"),
 ("onSurfaceVariant", "surfaceContainer", 4.5, "secondary text on container"),
 ("onTertiaryContainer", "tertiaryContainer", 4.5, "accent container text"),
 ("primary", "surface", 3.0, "primary icons/large text on surface"),
 ("tertiary", "surface", 3.0, "accent icons on surface"),
]

def lum(h):
    r, g, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
    f = lambda c: c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b)

def ratio(a, b):
    la, lb = sorted((lum(a), lum(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)

fails = 0
for d, themes in P.items():
    for t, c in themes.items():
        for fg, bg, mn, what in PAIRS:
            r = ratio(c[fg], c[bg])
            ok = r >= mn
            fails += not ok
            print(f"{d:7} {t:5} {what:34} {r:5.2f} (min {mn}) {'ok' if ok else 'FAIL'}")
print(f"\n{fails} failures")
