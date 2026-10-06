from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
errors=[]
app=(root/'app/build.gradle').read_text()
if "applicationId 'com.noir.game.engine'" not in app: errors.append('wrong applicationId')
if "versionName '0.4.0'" not in app: errors.append('wrong versionName')
java=list((root/'engine/src/main/java').rglob('*.java'))
small=[str(p.relative_to(root)) for p in java if p.stat().st_size<1024]
if small: errors.append('engine source below 1KB: '+', '.join(small))
for required in ['docs/GAME_SCRIPTING_API.md','docs/API_NODES.md','docs/ENCRYPTED_GAME_FORMAT.md','docs/MOBILE_EDITOR_FEATURES.md','tools/NoirGamePack.java']:
    if not (root/required).exists(): errors.append('missing '+required)
for workflow in ['.github/workflows/android.yml','.github/workflows/release.yml']:
    if not (root/workflow).exists(): errors.append('missing '+workflow)
if errors:
    print('NOIR REPOSITORY VALIDATION: FAIL')
    for e in errors: print(' -',e)
    sys.exit(1)
print(f'NOIR REPOSITORY VALIDATION: PASS ({len(java)} engine Java files, all >= 1KB)')
