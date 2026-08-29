#!/usr/bin/env python3
"""CoreCmp auto versioning: rc → alpha → beta (001-100), then base bump."""

from __future__ import annotations

import re
import sys
from dataclasses import dataclass
from pathlib import Path

CHANNELS = ("rc", "beta")
MAX_CHANNEL_BUILD = 100
DEFAULT_VERSION = "1.0.0.1-rc-001"
BUILD_INFO_RELATIVE = Path("shared/src/commonMain/kotlin/com/corecmp/shared/internal/CoreCmpBuildInfo.kt")
VERSION_PATTERN = re.compile(
    r"^(?P<base>\d+\.\d+\.\d+\.(?P<build>\d+))-(?P<channel>rc|beta)-(?P<num>\d{1,3})$"
)


@dataclass(frozen=True)
class ParsedVersion:
    base: str
    build: int
    channel: str
    number: int

    @property
    def full(self) -> str:
        return f"{self.base}-{self.channel}-{self.number:03d}"


def parse_version(value: str) -> ParsedVersion:
    match = VERSION_PATTERN.match(value.strip())
    if not match:
        raise ValueError(
            f"Invalid version '{value}'. Expected format like 1.0.0.0-rc-001"
        )
    channel = match.group("channel")
    number = int(match.group("num"))
    if number < 1 or number > MAX_CHANNEL_BUILD:
        raise ValueError(f"Channel build must be 1..{MAX_CHANNEL_BUILD}, got {number}")
    return ParsedVersion(
        base=match.group("base"),
        build=int(match.group("build")),
        channel=channel,
        number=number,
    )


def increment_version(value: str) -> str:
    current = parse_version(value)

    if current.number < MAX_CHANNEL_BUILD:
        return f"{current.base}-{current.channel}-{current.number + 1:03d}"

    channel_index = CHANNELS.index(current.channel)
    if channel_index < len(CHANNELS) - 1:
        next_channel = CHANNELS[channel_index + 1]
        return f"{current.base}-{next_channel}-001"

    next_build = current.build + 1
    base_parts = current.base.rsplit(".", 1)
    next_base = f"{base_parts[0]}.{next_build}"
    return f"{next_base}-rc-001"


def read_version_file(path: Path) -> str:
    if not path.exists():
        return DEFAULT_VERSION
    for line in path.read_text().splitlines():
        line = line.strip()
        if line.startswith("version="):
            value = line.split("=", 1)[1].strip()
            if value:
                parse_version(value)  # validate
                return value
    return DEFAULT_VERSION


def write_build_info(root: Path, version: str) -> None:
    path = root / BUILD_INFO_RELATIVE
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        f"""package com.corecmp.shared.internal

internal object CoreCmpBuildInfo {{
    const val VERSION: String = "{version}"
}}
"""
    )


def write_version_file(path: Path, version: str, root: Path | None = None) -> None:
    parse_version(version)
    path.write_text(f"version={version}\n")
    if root is not None:
        write_build_info(root, version)
        gradle_props = root / "gradle.properties"
        if gradle_props.exists():
            lines = gradle_props.read_text().splitlines()
            new_lines = [f"corecmp.version={version}" if l.startswith("corecmp.version=") else l for l in lines]
            gradle_props.write_text("\n".join(new_lines) + "\n")


def describe_next(value: str) -> str:
    current = parse_version(value)
    nxt = increment_version(value)
    return (
        f"Current : {current.full}\n"
        f"Next    : {nxt}\n"
        f"Channel : {current.channel} ({current.number}/{MAX_CHANNEL_BUILD})\n"
        f"Base    : {current.base}"
    )


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    version_file = root / "version.properties"
    command = sys.argv[1] if len(sys.argv) > 1 else "current"

    if command == "current":
        print(read_version_file(version_file))
        return

    if command == "next":
        print(increment_version(read_version_file(version_file)))
        return

    if command == "describe":
        print(describe_next(read_version_file(version_file)))
        return

    if command == "bump":
        current = read_version_file(version_file)
        nxt = increment_version(current)
        write_version_file(version_file, nxt, root)
        print(nxt)
        return

    if command == "set":
        if len(sys.argv) < 3:
            raise SystemExit("Usage: version.py set <version>")
        write_version_file(version_file, sys.argv[2], root)
        print(sys.argv[2])
        return

    if command == "sync":
        current = read_version_file(version_file)
        write_build_info(root, current)
        print(current)
        return

    if command == "validate":
        parse_version(read_version_file(version_file))
        print("ok")
        return

    raise SystemExit(
        "Usage: version.py [current|next|bump|describe|set <version>|validate|sync]"
    )


if __name__ == "__main__":
    main()
