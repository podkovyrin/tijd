#!/usr/bin/env python3
"""Render the app's bundled privacy text as a static GitHub Pages document."""

from html import escape
from pathlib import Path
import os
import re
import shutil

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "android/app/src/main/res/raw/privacy_policy.txt"
OUTPUT = ROOT / "build/privacy-site"


def linked_text(text):
    """Escape all policy content; link only explicit web URLs and email addresses."""
    pattern = r"https://[^\s<>]+|[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}"
    parts = []
    position = 0
    for match in re.finditer(pattern, text):
        parts.append(escape(text[position:match.start()]))
        address = match.group().rstrip(".,;")
        href = address if address.startswith("https://") else "mailto:" + address
        parts.append(f'<a href="{escape(href, quote=True)}">{escape(address)}</a>')
        parts.append(escape(match.group()[len(address):]))
        position = match.end()
    parts.append(escape(text[position:]))
    return "".join(parts)


def main():
    policy = SOURCE.read_text(encoding="utf-8").strip()
    if os.environ.get("GITHUB_ACTIONS") == "true" and "\n\nPrivacy questions\n" not in policy:
        raise SystemExit("Add a Privacy questions section with the confirmed public contact before publishing. See docs/github-pages.md.")
    blocks = policy.split("\n\n")
    title, date = blocks[0].split("\n", 1)
    sections = []
    for block in blocks[1:]:
        heading, paragraph = block.split("\n", 1)
        sections.append(
            f"<section><h2>{escape(heading)}</h2>"
            f"<p>{linked_text(' '.join(paragraph.splitlines()))}</p></section>"
        )
    template = (ROOT / "privacy/template.html").read_text(encoding="utf-8")
    page = template.replace("{{TITLE}}", escape(title))
    page = page.replace("{{DATE}}", escape(date)).replace("{{SECTIONS}}", "\n".join(sections))
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / "index.html").write_text(page, encoding="utf-8")
    # A stable explicit URL for Play Console, alongside the same policy at the root.
    (OUTPUT / "privacy-policy.html").write_text(page, encoding="utf-8")
    (OUTPUT / "privacy-policy.txt").write_text(policy + "\n", encoding="utf-8")
    (OUTPUT / ".nojekyll").touch()
    shutil.copyfile(ROOT / "assets/branding/play-store-icon.png", OUTPUT / "icon.png")
    print(f"Privacy page built in {OUTPUT}")


if __name__ == "__main__":
    main()
