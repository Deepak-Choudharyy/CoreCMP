#!/usr/bin/env python3
"""Add or update a release entry in maven-repo/versions.json."""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

GROUP_ID = "com.corecmp"
ARTIFACT_ID = "shared"
MAVEN_REPO_URL = "https://deepak-choudharyy.github.io/CoreCMP/maven-repo/"


def load_manifest(path: Path) -> dict:
    if not path.exists():
        return {
            "groupId": GROUP_ID,
            "artifactId": ARTIFACT_ID,
            "latest": "",
            "versions": [],
            "releases": [],
            "repository": MAVEN_REPO_URL,
            "updatedAt": "",
        }
    return json.loads(path.read_text())


def save_manifest(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def upsert_release(
    data: dict,
    version: str,
    message: str,
    published_at: str,
    commit: str,
) -> dict:
    releases: list[dict] = list(data.get("releases") or [])
    entry = {
        "version": version,
        "message": message.strip() or f"Release CoreCmp {version}",
        "publishedAt": published_at,
        "commit": commit,
    }

    replaced = False
    for index, release in enumerate(releases):
        if release.get("version") == version:
            releases[index] = entry
            replaced = True
            break
    if not replaced:
        releases.insert(0, entry)

    releases.sort(key=release_sort_key, reverse=True)

    versions = [r["version"] for r in releases]
    # Keep any legacy version strings not present in releases.
    for legacy in data.get("versions") or []:
        if legacy not in versions:
            versions.append(legacy)

    data["releases"] = releases
    data["versions"] = versions
    data["latest"] = version
    data["groupId"] = GROUP_ID
    data["artifactId"] = ARTIFACT_ID
    data["repository"] = MAVEN_REPO_URL
    data["updatedAt"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    return data


def directory_published_at(version_dir: Path) -> str:
    if not version_dir.is_dir():
        return ""
    ts = version_dir.stat().st_mtime
    return datetime.fromtimestamp(ts, tz=timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def git_tag_metadata(version: str) -> tuple[str, str, str]:
    """Return (message, published_at, commit) from local git tag."""
    try:
        message = subprocess.check_output(
            ["git", "log", "-1", "--format=%B", version],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
        published_at = subprocess.check_output(
            ["git", "log", "-1", "--format=%cI", version],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
        commit = subprocess.check_output(
            ["git", "rev-parse", f"{version}^{{commit}}"],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
        return message, published_at, commit
    except (subprocess.CalledProcessError, FileNotFoundError):
        return f"Release CoreCmp {version}", "", ""


def release_sort_key(release: dict) -> tuple[str, str]:
    published = release.get("publishedAt") or ""
    return (published, release.get("version", ""))


def discover_maven_versions(repo_root: Path) -> list[str]:
    """Return version dirs published under shared/ (canonical KMP artifact)."""
    shared_root = repo_root / "com" / "corecmp" / "shared"
    if not shared_root.is_dir():
        return []

    versions: list[str] = []
    for child in shared_root.iterdir():
        if child.is_dir() and not child.name.startswith("."):
            versions.append(child.name)
    return sorted(versions, reverse=True)


def backfill_releases_from_maven(data: dict, repo_root: Path) -> dict:
    releases: list[dict] = list(data.get("releases") or [])
    known = {r.get("version") for r in releases}

    for version in discover_maven_versions(repo_root):
        if version in known:
            continue
        message, published_at, commit = git_tag_metadata(version)
        if not published_at:
            version_dir = repo_root / "com" / "corecmp" / "shared" / version
            published_at = directory_published_at(version_dir)
        releases.append(
            {
                "version": version,
                "message": message,
                "publishedAt": published_at,
                "commit": commit,
            }
        )

    releases.sort(key=release_sort_key, reverse=True)
    data["releases"] = releases

    versions = [r["version"] for r in releases]
    for legacy in data.get("versions") or []:
        if legacy not in versions:
            versions.append(legacy)
    data["versions"] = versions

    if releases and not data.get("latest"):
        data["latest"] = releases[0]["version"]
    data["updatedAt"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    return data


def backfill_releases_from_git(data: dict) -> dict:
    releases: list[dict] = list(data.get("releases") or [])
    known = {r.get("version") for r in releases}

    for version in data.get("versions") or []:
        if version in known:
            continue
        message, published_at, commit = git_tag_metadata(version)
        releases.append(
            {
                "version": version,
                "message": message,
                "publishedAt": published_at,
                "commit": commit,
            }
        )

    releases.sort(key=release_sort_key, reverse=True)
    data["releases"] = releases
    if releases and not data.get("latest"):
        data["latest"] = releases[0]["version"]
    return data


def main() -> None:
    parser = argparse.ArgumentParser(description="Update CoreCmp versions.json release metadata")
    parser.add_argument("--manifest", default="maven-repo/versions.json")
    parser.add_argument("--version", help="Release version")
    parser.add_argument("--message", default="", help="Release commit/tag message")
    parser.add_argument("--published-at", default="", help="ISO-8601 publish time")
    parser.add_argument("--commit", default="", help="Git commit SHA")
    parser.add_argument("--backfill-git", action="store_true", help="Fill missing releases from git tags")
    parser.add_argument("--record-failure", action="store_true", help="Record this run as a failure")
    parser.add_argument("--failed-log", default="", help="Path to the failed build log file relative to maven-repo")
    parser.add_argument(
        "--scan-maven-repo",
        action="store_true",
        help="Add releases for every version directory in maven-repo/shared",
    )
    args = parser.parse_args()

    manifest_path = Path(args.manifest)
    data = load_manifest(manifest_path)
    repo_root = manifest_path.parent

    if args.scan_maven_repo:
        data = backfill_releases_from_maven(data, repo_root)

    if args.backfill_git:
        data = backfill_releases_from_git(data)

    if args.version:
        if args.record_failure:
            failures = list(data.get("failures") or [])
            failures = [f for f in failures if f.get("version") != args.version]
            failures.insert(0, {
                "version": args.version,
                "commit": args.commit,
                "publishedAt": args.published_at or datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
                "logFile": args.failed_log
            })
            data["failures"] = failures[:10]
            data["updatedAt"] = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        else:
            # Clean from failures list if successful
            failures = list(data.get("failures") or [])
            failures = [f for f in failures if f.get("version") != args.version]
            data["failures"] = failures
            
            message = args.message
            published_at = args.published_at
            commit = args.commit
            if not message or not published_at or not commit:
                git_message, git_time, git_commit = git_tag_metadata(args.version)
                message = message or git_message
                published_at = published_at or git_time
                commit = commit or git_commit
            data = upsert_release(data, args.version, message, published_at, commit)

    save_manifest(manifest_path, data)

    if args.version or args.backfill_git or args.scan_maven_repo:
        root = Path(__file__).resolve().parents[1]
        subprocess.check_call(
            [sys.executable, str(root / "scripts/generate-maven-pages.py"), str(manifest_path.parent)]
        )


if __name__ == "__main__":
    main()
