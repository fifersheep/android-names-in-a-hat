"""Contrast table for the chosen Top Hat scheme, printed as markdown. Usage: python3 -I spec_contrast.py"""
S = {
 "light": dict(primary="#4D4BB3", onPrimary="#FFFFFF", primaryContainer="#E2DFFF", onPrimaryContainer="#0B006B",
   secondary="#5D5C72", onSecondary="#FFFFFF", secondaryContainer="#E3E0F9", onSecondaryContainer="#1A1A2C",
   tertiary="#7A5900", onTertiary="#FFFFFF", tertiaryContainer="#FFDEA3", onTertiaryContainer="#261900",
   error="#BA1A1A", onError="#FFFFFF", errorContainer="#FFDAD6", onErrorContainer="#410002",
   surface="#FCF8FF", onSurface="#1B1B21", onSurfaceVariant="#47464F",
   surfaceContainerLowest="#FFFFFF", surfaceContainerLow="#F6F2FA", surfaceContainer="#F0ECF7",
   surfaceContainerHigh="#EAE6F1", surfaceContainerHighest="#E4E1E9",
   outline="#787680", outlineVariant="#C8C5D0", inverseSurface="#303036", inverseOnSurface="#F3EFF7", inversePrimary="#C3C0FF"),
 "dark": dict(primary="#C3C0FF", onPrimary="#1F1A86", primaryContainer="#35319A", onPrimaryContainer="#E2DFFF",
   secondary="#C6C4DD", onSecondary="#2F2F42", secondaryContainer="#46455A", onSecondaryContainer="#E3E0F9",
   tertiary="#F5BE48", onTertiary="#402D00", tertiaryContainer="#5C4200", onTertiaryContainer="#FFDEA3",
   error="#FFB4AB", onError="#690005", errorContainer="#93000A", onErrorContainer="#FFDAD6",
   surface="#131318", onSurface="#E4E1E9", onSurfaceVariant="#C8C5D0",
   surfaceContainerLowest="#0E0E13", surfaceContainerLow="#1B1B21", surfaceContainer="#1F1F25",
   surfaceContainerHigh="#2A292F", surfaceContainerHighest="#35343A",
   outline="#928F99", outlineVariant="#47464F", inverseSurface="#E4E1E9", inverseOnSurface="#303036", inversePrimary="#4D4BB3"),
}
PAIRS = [
 ("onSurface", "surface", 4.5, "Body text"),
 ("onSurfaceVariant", "surface", 4.5, "Secondary text; chip Out label"),
 ("outline", "surface", 3.0, "Chip Out border; unchecked box (UI)"),
 ("onSecondaryContainer", "secondaryContainer", 4.5, "Chip In label"),
 ("onSurfaceVariant", "surfaceContainerHigh", 4.5, "Chip Drawn label (struck)"),
 ("onPrimary", "primary", 4.5, "Draw button; checked box tick"),
 ("primary", "surface", 3.0, "Checked box; mixed-box border; sort/accent UI"),
 ("primary", "primaryContainer", 3.0, "Mixed-box dash on tonal fill"),
 ("onSurface", "surfaceContainerHigh", 4.5, "Stepper value and icons"),
 ("onSurface", "surfaceContainer", 4.5, "Draw bar text"),
 ("onPrimaryContainer", "primaryContainer", 4.5, "FAB label; reveal backdrop text (dark)"),
 ("onSurface", "surfaceContainerLowest", 4.5, "Reveal name cards"),
 ("onPrimary", "primary", 4.5, "Reveal backdrop text (light)", "light"),
 ("tertiaryContainer", "primary", 3.0, "Reveal sparkles on backdrop (light: backdrop = primary)", "light"),
 ("tertiary", "primaryContainer", 3.0, "Reveal sparkles on backdrop (dark: backdrop = primaryContainer)", "dark"),
 ("error", "surface", 4.5, "Field error text"),
 ("inverseOnSurface", "inverseSurface", 4.5, "Snackbar text"),
 ("inversePrimary", "inverseSurface", 4.5, "Snackbar Undo action"),
]

def lum(h):
    r, g, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
    f = lambda c: c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b)

def ratio(a, b):
    la, lb = sorted((lum(a), lum(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)

fails = 0
print("| Use | Pair (fg / bg) | Min | Light | Dark |")
print("|---|---|---|---|---|")
for row in PAIRS:
    fg, bg, mn, what = row[:4]; only = row[4] if len(row) > 4 else None
    cells = []
    for t in ("light", "dark"):
        if only and t != only: cells.append("—"); continue
        r = ratio(S[t][fg], S[t][bg]); ok = r >= mn; fails += not ok
        cells.append(f"{r:.2f}{'' if ok else ' **FAIL**'}")
    print(f"| {what} | `{fg}` / `{bg}` | {mn} | {cells[0]} | {cells[1]} |")
print(f"\n{fails} failures")
