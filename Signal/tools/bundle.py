"""Package the art and Kotlin/JS output into a single offline HTML file."""
import argparse
import base64
import io
import shutil
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
web = root / "web"
parser = argparse.ArgumentParser()
parser.add_argument("script", type=Path)
parser.add_argument("--compact", action="store_true")
parser.add_argument("--standalone", type=Path, help="Also write a self-contained HTML copy")
args = parser.parse_args()

def embed(path):
    if args.compact and (path.stem.startswith("phone") or path.stem == "memory-wave"):
        from PIL import Image

        with Image.open(path) as image:
            size = (300, 300) if path.stem == "memory-wave" else (110, 130)
            image.thumbnail(size, Image.Resampling.LANCZOS)
            output = io.BytesIO()
            image.save(output, format="WEBP", quality=45, method=4)
        mime, data = "webp", output.getvalue()
    else:
        mime, data = path.suffix[1:], path.read_bytes()
    return f"data:image/{mime};base64," + base64.b64encode(data).decode()

assets = root / "android/src/main/assets"
paths = sorted((assets / "art").iterdir())
catalog = (web / "archive.tsv").read_text()
runtime = args.script.read_text()
page = (web / "index.html").read_text().replace("/*__STYLE__*/", (web / "style.css").read_text())

def write_page(target, embedded):
    art = {p.stem: embed(p) if embedded else "art/" + p.name for p in paths}
    script = ("window.signalArt=" + json.dumps(art, separators=(",", ":")) +
              ";\nwindow.signalCatalog=" + json.dumps(catalog, ensure_ascii=False) + ";\n" + runtime)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(page.replace("/*__GAME__*/", script.replace("</script", "<\\/script")))

target = (root / "android/build/compact-assets" if args.compact else assets) / "index.html"
write_page(target, args.compact)
if args.standalone:
    write_page(args.standalone, True)

for path in (root / "LICENSE", root / "LICENSE-APACHE-2.0", root / "NOTICE", web / "PHOTO-SOURCES.md"):
    shutil.copyfile(path, target.parent / path.name)
