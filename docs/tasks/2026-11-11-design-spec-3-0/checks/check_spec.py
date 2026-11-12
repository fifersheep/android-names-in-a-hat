"""Checks docs/design/3.0-spec.md. Usage: python3 -I check_spec.py <spec.md> <strings.xml from origin/master>"""
import re, sys
spec = open(sys.argv[1]).read()
strings_xml = open(sys.argv[2]).read()
gaps = 0

# 1. Every #anchor link resolves to an <a id>.
ids = set(re.findall(r'<a id="([^"]+)"></a>', spec))
links = set(re.findall(r'\]\(#([^)]+)\)', spec))
missing = sorted(links - ids)
print(f"anchors: {len(ids)} defined, {len(links)} linked, missing: {missing or 'none'}")
gaps += len(missing)

# 2. Coverage table: 9 NAM-2 rows, each linking a defined anchor.
cov = spec.split('<a id="coverage"></a>')[1].split('<a id="decisions"></a>')[0]
rows = [l for l in cov.splitlines() if l.startswith('| ') and not l.startswith('| NAM-2 question') and '---' not in l]
bad = [r for r in rows if not re.search(r'\]\(#', r)]
print(f"coverage rows: {len(rows)} (expect 9), without a link: {len(bad)}")
gaps += (len(rows) != 9) + len(bad)

# 3. Ticket index: per-ticket counts match research.md 'What downstream tickets expect'
#    (NAM-7: 7 items, NAM-8: 4, NAM-9: 7, NAM-10: 4, NAM-12: 6) plus the NAM-6 hand-off (NAM-5 merged before the spec; its hand-off moved to NAM-12).
expected = {"7": 7, "8": 4, "9": 7, "10": 4, "12": 6, "6": 1}
idx = spec.split('<a id="ticket-index"></a>')[1].split('<a id="artboards"></a>')[0]
got = {}
for m in re.finditer(r'^\| NAM-(\d+)\.(\d+) \|', idx, re.M):
    got[m.group(1)] = got.get(m.group(1), 0) + 1
diff = {k: (expected[k], got.get(k, 0)) for k in expected if expected[k] != got.get(k, 0)}
print(f"ticket index per ticket: {got}; mismatches: {diff or 'none'}")
gaps += len(diff)

# 4. Copy deck: every 2.7.0 string key and hard-coded string is accounted for.
keys = re.findall(r'<string name="([^"]+)"', strings_xml)
deck = spec.split('<a id="copy-deck"></a>')[1].split('<a id="links"></a>')[0]
source_col = "\n".join(l.rsplit('|', 2)[-2] for l in deck.splitlines() if l.startswith('|'))
hard = ['"N names"', '"… deleted" (`OverviewActivity', '"… deleted" (`EditNames', '"Edit: "', '"Names: "',
        '"n out of t names selected"', '"Select All"', '"Clear All"']
unacc = [k for k in keys if f"`{k}`" not in source_col] + [h for h in hard if h not in source_col]
print(f"copy deck: {len(keys)} 2.7.0 keys + {len(hard)} hard-coded; unaccounted: {unacc or 'none'}")
gaps += len(unacc)

# 5. Stability notes next to the experimental/deprecated API names.
for name in ["MaterialShapes", "LoadingIndicator", "ListDetailPaneScaffold", "NavigableListDetailPaneScaffold",
             "ListDetailSceneStrategy", "currentWindowAdaptiveInfo`"]:
    lines = [l for l in spec.splitlines() if name in l]
    noted = all(re.search(r'Experimental|experimental|deprecated|opt-in|API stability', l) for l in lines)
    print(f"stability note for {name}: {len(lines)} mentions, {'ok' if noted else 'MISSING NOTE'}")
    gaps += not noted
pin = "1.5.0-rc01" in spec.split('<a id="api-stability"></a>')[1]
print(f"material3 1.5.0-rc01 pin stated in API stability: {pin}")
gaps += not pin
print(f"\n{gaps} gaps")
