#!/usr/bin/env python3
"""Reachability sweep for app/src/main/res. Prints + optionally applies deletions.
File-wise for non-values resources; entry-wise for values/*.xml."""
import os, re, sys, glob

ROOT = 'app/src/main'
RES = ROOT + '/res'
JAVA = ROOT + '/java'
KEEP_NAMES = {'system_wallpaper_directory', 'default_wallpapper_hidden'}  # Partner.getIdentifier lookups
WHITELIST_FILES = {'ic_launcher.png'}  # never touch launcher icon bases

# ---- inventory: list of (type, name, path) ; values entries separately
resource_by_name = {}  # (type,name) -> [paths]
values_files = []      # values xml paths
for dirpath, _, files in os.walk(RES):
    rel = os.path.relpath(dirpath, RES)
    parts = rel.split(os.sep)
    if parts[0] == 'values' or parts[0].startswith('values-'):
        for f in files:
            if f.endswith('.xml'):
                values_files.append(os.path.join(dirpath, f))
        continue
    rtype = parts[0].split('-')[0]
    if rtype not in ('drawable', 'mipmap', 'layout', 'anim', 'raw', 'xml', 'menu', 'color'):
        continue
    for f in files:
        name = f.split('.')[0]
        resource_by_name.setdefault((rtype, name), []).append(os.path.join(dirpath, f))

# also values-defined types: parse all values files
values_entries = {}  # (type,name) -> [valuesfile]
val_elem_re = re.compile(r'<(string-array|integer-array|plurals|string|bool|color|dimen|id|integer|style|attr|fraction|drawable|menu|layout|mipmap|raw|xml|declare-styleable)\b[^>]*?\bname="([^"]+)"')
for vf in values_files:
    content = open(vf, encoding='utf-8').read()
    for m in val_elem_re.finditer(content):
        t, n = m.group(1), m.group(2)
        t = {'string-array': 'array', 'integer-array': 'array', 'declare-styleable': 'styleable'}.get(t, t)
        values_entries.setdefault((t, n), []).append(vf)
        if t in ('style', 'attr') and '.' in n:
            values_entries.setdefault((t, n.replace('.', '_')), []).append(vf)

# ---- java + manifest reference scan (R.type.name + @type/name)
java_text = []
for dirpath, _, files in os.walk(JAVA):
    for f in files:
        if f.endswith(('.java', '.xml')):
            java_text.append(os.path.join(dirpath, f))
man = ROOT + '/AndroidManifest.xml'
java_text.append(man)

def scan_text_for_refs(text, refs):
    for m in re.finditer(r'\bR\.(\w+)\.(\w+)', text):
        refs.add((m.group(1), m.group(2)))
    for m in re.finditer(r'[?@]\+?(?:android:)?(\w+)/([A-Za-z0-9_.]+)', text):
        refs.add((m.group(1), m.group(2)))
    for m in re.finditer(r'parent="(@?style/)?([A-Za-z0-9_.]+)"', text):
        if not m.group(2).startswith('android'):
            refs.add(('style', m.group(2)))
    for m in re.finditer(r'<item\s+name="([^"]+)"', text):
        nm = m.group(1)
        if nm and not nm.startswith('android'):
            refs.add(('attr', nm.split('/')[0]))
    for m in re.finditer(r'<(?:declare-styleable|attr-format)?[^>]*>\s*<attr\s+[^>]*?name="([^"]+)"', text):
        nm = m.group(1)
        if nm and not nm.startswith('android'):
            refs.add(('attr', nm))
    # namespace-prefixed attribute usage: app:foo="...", lib:bar="..." (any non-android prefix)
    for m in re.finditer(r'\s([A-Za-z_][\w-]*):([A-Za-z_][\w.]*)="', text):
        ns, nm = m.group(1), m.group(2)
        if ns in ('android', 'xmlns', 'tools'):
            continue
        if nm in ('id', 'name'):
            continue
        refs.add(('attr', nm))

refs = set(KEEP_RES := set())
file_contents = {}
for path in java_text:
    t = open(path, encoding='utf-8', errors='ignore').read()
    file_contents[path] = t
    scan_text_for_refs(t, refs)

# ---- fixpoint over res xml content
kept = set()
def norm(typ, name):
    return (typ, name)

def expand(current_refs):
    more = set()
    for (t, n) in current_refs:
        paths = resource_by_name.get((t, n), []) + values_entries.get((t, n), [])
        for p in paths:
            if p in file_contents or True:
                if p not in file_contents:
                    try:
                        file_contents[p] = open(p, encoding='utf-8', errors='ignore').read()
                    except OSError:
                        continue
                txt = file_contents[p]
                before = None
                if p not in scanned:
                    scanned.add(p)
                    scan_text_for_refs(txt, more)
    return more

scanned = set()
work = set(refs)
while work:
    more = expand(work)
    new = more - refs
    refs |= new
    work = new

# ---- compute orphans
KEEP_RES.update(KEEP_NAMES)
all_names = set(resource_by_name) | set(values_entries)
def name_alive(t, n):
    if (t, n) in refs:
        return True
    if t in ('style', 'attr', 'array', 'bool', 'color', 'dimen', 'id', 'string', 'integer'):
        if (t, n.replace('_', '.')) in refs:
            return True
        if (t, n.replace('.', '_')) in refs:
            return True
    return False
orphans = set(k for k in all_names if not name_alive(*k))

# protected: always keep
protect = set()
PROTECT_SUBSTR = ('ic_launcher', 'devie_')
for (t, n) in list(orphans):
    if n in WHITELIST_FILES or any(k in n for k in PROTECT_SUBSTR):
        protect.add((t, n))
orphans -= protect

print(f'resource entries total: {len(all_names)}, dead: {len(orphans)}')
sizes = 0
del_files, del_entries = [], {}
for (t, n) in sorted(orphans):
    for p in resource_by_name.get((t, n), []):
        del_files.append(p)
        sizes += os.path.getsize(p)
    vps = values_entries.get((t, n), [])
    if vps:
        del_entries[(t, n)] = vps

print(f'orphan files: {len(del_files)} ({sizes/1e6:.2f} MB raw), value entries: {len(del_entries)}')
if len(sys.argv) > 1 and sys.argv[1] == 'apply':
    for p in del_files:
        os.remove(p)
    # prune value entries from values files
    byvf = {}
    for (t, n), vps in del_entries.items():
        for vf in vps:
            byvf.setdefault(vf, []).append((t, n))
    for vf, entries in byvf.items():
        c = open(vf, encoding='utf-8').read()
        for (t, n) in entries:
            alt = 'array' if t == 'array' else t
            pat = re.compile(r'\n?\s*<' + alt + r'\b[^>]*\bname="' + re.escape(n) + r'"[^>]*?(?:/>|>.*?</' + alt + r'>|>.*?</string-array>|>.*?</integer-array>|>.*?</plurals>)', re.S)
            c2 = pat.sub('', c, count=1)
            if c2 != c:
                c = c2
            else:
                print(f'  COULD NOT PRUNE {t} {n} in {vf}')
        open(vf, 'w', encoding='utf-8').write(c)
        # drop empty locale values files
        body = c.split('>', 1)[-1]
        if '<' not in body:
            os.remove(vf)
            print(f'  removed empty {vf}')
    print('APPLIED')
else:
    for (t, n) in list(orphans)[:25]:
        print('  dead:', t, n)
    print('(dry-run; rerun with apply)')