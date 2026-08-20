#!/usr/bin/env bash
# CoreCmp Auto Publish & Git Helper: Auto-bumping, compiling, publishing locally, and pushing.
set -euo pipefail

if [ -z "${1:-}" ]; then
  echo "Error: Please provide a release description / commit message."
  echo "Usage: ./scripts/push.sh \"Your release message\""
  exit 1
fi

COMMIT_MSG="$1"

# 1. Bump version locally
echo "Bumping library version..."
VERSION="$(python3 scripts/version.py bump)"
echo "New version bumped to: $VERSION"

DATE_ISO="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

# 2. Compile and build Maven artifacts locally
echo "Compiling and building Maven artifacts locally..."
./gradlew publishAllPublicationsToMavenRepository

# 3. Generate release dashboard index.html and versions.json at the root
echo "Generating dashboard page..."
# Get temporary commit hash (will update after commit)
TEMP_COMMIT_SHA="$(git rev-parse HEAD 2>/dev/null || echo "manual")"
python3 scripts/update-version-manifest.py \
  --manifest versions.json \
  --version "$VERSION" \
  --message "$COMMIT_MSG" \
  --published-at "$DATE_ISO" \
  --commit "$TEMP_COMMIT_SHA" \
  --scan-maven-repo

# Copy to maven-repo folder for backup/hosting consistency
cp versions.json maven-repo/versions.json
cp index.html maven-repo/index.html
touch .nojekyll

# 4. Stage and commit everything
echo "Staging all changes..."
git add .

echo "Committing release $VERSION..."
git commit -m "Publish CoreCmp $VERSION: $COMMIT_MSG" || echo "No changes to commit."

# Update the manifest with the final commit hash
FINAL_COMMIT_SHA="$(git rev-parse HEAD)"
python3 scripts/update-version-manifest.py \
  --manifest versions.json \
  --version "$VERSION" \
  --message "$COMMIT_MSG" \
  --published-at "$DATE_ISO" \
  --commit "$FINAL_COMMIT_SHA" \
  --scan-maven-repo

cp versions.json maven-repo/versions.json
cp index.html maven-repo/index.html

# Amend commit to include the updated manifest files with final commit SHA
git add versions.json index.html maven-repo/versions.json maven-repo/index.html
git commit --amend --no-edit || true

# 5. Pull & Rebase with conflict resolution for generated files
echo "Pulling latest commits from remote..."
if ! git pull --rebase origin main; then
  echo "Conflicts detected in auto-generated files. Resolving automatically..."
  
  # Checkout our local version for conflicted/generated files to override remote bots
  git checkout --theirs \
    index.html \
    maven-repo/index.html \
    maven-repo/versions.json \
    versions.json \
    shared/src/commonMain/kotlin/com/corecmp/shared/internal/CoreCmpBuildInfo.kt \
    version.properties || true
    
  git add .
  git -c core.editor=true rebase --continue
fi

# 6. Push to main branch
echo "Pushing to main branch..."
git push origin main

echo ""
echo "=========================================================="
echo "Success! CoreCmp $VERSION published and pushed to GitHub!"
echo "Your live site will update in a minute at:"
echo "  https://deepak-choudharyy.github.io/CoreCMP/"
echo "=========================================================="
