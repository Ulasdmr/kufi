#!/usr/bin/env bash
set -euo pipefail
# Run after a successful mvn clean test. Captures concise Surefire summaries, no tokens.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
mkdir -p evidence/week-06
shopt -s nullglob
reports=(target/surefire-reports/*.txt)
if [ "${#reports[@]}" -eq 0 ]; then
  echo "No Surefire text reports found. Run: mvn clean test" >&2
  exit 1
fi
python3 - <<'PY'
import glob,re,sys
from pathlib import Path
paths=glob.glob('target/surefire-reports/*.txt')
totals={'Tests run':0,'Failures':0,'Errors':0,'Skipped':0}
rows=[]
for path in sorted(paths):
    content=Path(path).read_text(errors='replace')
    match=re.search(r'Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)',content)
    if not match: continue
    name=Path(path).stem.replace('com.kufi.backend.','')
    run,failed,errors,skip=map(int,match.groups())
    rows.append((name,run,failed,errors,skip))
    for key,val in zip(totals,(run,failed,errors,skip)): totals[key]+=val
out=['Kufi Week 6: Maven Surefire verified report summary',
     'Generated from target/surefire-reports/*.txt on student machine',
     'No credentials or JWTs included','']
for name,*values in rows:
    out.append(f'{name}: tests={values[0]}, failures={values[1]}, errors={values[2]}, skipped={values[3]}')
out.extend(['', 'TOTAL: tests={Tests run}, failures={Failures}, errors={Errors}, skipped={Skipped}'.format(**totals)])
output=Path('evidence/week-06/maven-surefire-summary.txt')
output.write_text('\n'.join(out)+'\n',encoding='utf8')
print('\n'.join(out))
if totals['Tests run']!=13 or totals['Failures'] or totals['Errors'] or totals['Skipped']:
    print('Not the expected clean 13/13. Do not commit as pass evidence.',file=sys.stderr)
    sys.exit(1)
PY
