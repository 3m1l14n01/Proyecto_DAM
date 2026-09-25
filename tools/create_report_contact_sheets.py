"""Create labeled contact sheets for visual QA of rendered report pages."""

from __future__ import annotations

import argparse
import re
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


def page_number(path: Path) -> int:
    match = re.search(r"(\d+)$", path.stem)
    return int(match.group(1)) if match else 0


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("render_dir", type=Path)
    parser.add_argument("--per-sheet", type=int, default=8)
    args = parser.parse_args()

    render_dir = args.render_dir.resolve()
    pages = sorted(render_dir.glob("page-*.png"), key=page_number)
    if not pages:
        raise SystemExit(f"No rendered pages found in {render_dir}")

    columns = 2
    rows = args.per_sheet // columns
    thumb_w, thumb_h = 420, 544
    label_h, gap = 28, 14
    sheet_w = columns * thumb_w + (columns + 1) * gap
    sheet_h = rows * (thumb_h + label_h) + (rows + 1) * gap
    font = ImageFont.load_default(size=18)

    for sheet_index in range(0, len(pages), args.per_sheet):
        batch = pages[sheet_index : sheet_index + args.per_sheet]
        sheet = Image.new("RGB", (sheet_w, sheet_h), "#D7D7D7")
        draw = ImageDraw.Draw(sheet)
        for position, page_path in enumerate(batch):
            row, column = divmod(position, columns)
            x = gap + column * (thumb_w + gap)
            y = gap + row * (thumb_h + label_h)
            with Image.open(page_path) as page:
                page = page.convert("RGB")
                page.thumbnail((thumb_w, thumb_h), Image.Resampling.LANCZOS)
                page_x = x + (thumb_w - page.width) // 2
                page_y = y + label_h
                sheet.paste(page, (page_x, page_y))
            label = f"Página {page_number(page_path)}"
            draw.text((x + 6, y + 3), label, fill="#111111", font=font)

        output = render_dir / f"contact-{sheet_index // args.per_sheet + 1:02d}.png"
        sheet.save(output, optimize=True)
        print(output)


if __name__ == "__main__":
    main()
