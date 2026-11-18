"""Stage the small Android download after running tools/build-web.sh."""
import hashlib
import shutil
from pathlib import Path

root = Path(__file__).resolve().parents[1] / "android"
source = root / "src/main/assets"
target = root / "build/light-assets"
staged = root / "build/light-assets.tmp"
if staged.exists():
    shutil.rmtree(staged)
(staged / "art").mkdir(parents=True)

for file in source.iterdir():
    if file.is_file():
        shutil.copyfile(file, staged / file.name)

photos = sorted((source / "art").glob("phone*.webp"), key=lambda p: int(p.stem[5:]))
# Keep menu art, the settings easter egg and some familiar handsets available from the start.
starter = {250, 361, 412, 413, 414, 415, 416, 1236, 1500, 7652}
with (staged / "photo-hashes.bin").open("wb") as hashes:
    for index, file in enumerate(photos):
        if file.name != f"phone{index}.webp":
            raise ValueError(f"Missing picture for album number {index + 1}")
        hashes.write(hashlib.sha256(file.read_bytes()).digest())
        if index < 300 or index in starter:
            shutil.copyfile(file, staged / "art" / file.name)

for file in (source / "art").glob("*.png"):
    shutil.copyfile(file, staged / "art" / file.name)
if target.exists():
    shutil.rmtree(target)
staged.rename(target)
