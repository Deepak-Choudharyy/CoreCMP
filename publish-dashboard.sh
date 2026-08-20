#!/usr/bin/env bash
set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "Error: Please specify the version to publish."
  echo "Usage: ./publish-dashboard.sh 1.0.0 \"Your release description\""
  exit 1
fi

VERSION="$1"
MESSAGE="${2:-Release version $VERSION}"
COMMIT_SHA="$(git rev-parse HEAD 2>/dev/null || echo "manual")"
DATE_ISO="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

# 1. Update version inside gradle.properties
echo "Updating gradle.properties with version $VERSION..."
python3 -c "
path = 'gradle.properties'
lines = open(path).read().splitlines()
for i, line in enumerate(lines):
    if line.startswith('corecmp.version='):
        lines[i] = 'corecmp.version=${VERSION}'
        break
else:
    lines.append('corecmp.version=${VERSION}')
open(path, 'w').write('\n'.join(lines) + '\n')
"

# 2. Update VERSION constant inside CoreCmp.kt
echo "Updating CoreCmp.kt with version $VERSION..."
python3 -c "
path = 'shared/src/commonMain/kotlin/com/corecmp/shared/CoreCmp.kt'
content = open(path).read()
import re
new_content = re.sub(r'const val VERSION: String = \".*\"', 'const val VERSION: String = \"${VERSION}\"', content)
open(path, 'w').write(new_content)
"

# 3. Compile and publish to local maven-repo folder
echo "Compiling and building Maven artifacts locally..."
./gradlew publishAllPublicationsToMavenRepository

# 4. Generate the dashboard index.html and versions.json at the root
echo "Generating dashboard page..."
python3 scripts/update-version-manifest.py \
  --manifest versions.json \
  --version "$VERSION" \
  --message "$MESSAGE" \
  --published-at "$DATE_ISO" \
  --commit "$COMMIT_SHA" \
  --scan-maven-repo

# Copy to maven-repo folder for backup/hosting consistency
cp versions.json maven-repo/versions.json
cp index.html maven-repo/index.html
touch .nojekyll

echo ""
echo "=========================================================="
echo "CoreCmp library $VERSION built & dashboard generated successfully!"
echo "Now run these commands to push it manually to GitHub:"
echo ""
echo "  git add ."
echo "  git commit -m \"Publish CoreCmp $VERSION\""
echo "  git push origin main"
echo "=========================================================="
