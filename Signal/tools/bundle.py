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

art = {path.stem: embed(path) for path in sorted((web / "art").iterdir())}
script = "window.signalArt=" + json.dumps(art, separators=(",", ":")) + ";\n" + args.script.read_text()
page = (web / "index.html").read_text().replace("/*__STYLE__*/", (web / "style.css").read_text())
target = root / "android/src/main/assets/index.html"
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(page.replace("/*__GAME__*/", script.replace("</script", "<\\/script")))

for path in (root / "LICENSE", root / "LICENSE-APACHE-2.0", root / "NOTICE", web / "PHOTO-SOURCES.md"):
    shutil.copyfile(path, target.parent / path.name)
