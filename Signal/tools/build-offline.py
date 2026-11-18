"""Pack the original phone photos for an offline APK of any album size."""
from pathlib import Path
from shutil import copyfile, rmtree
import struct

android = Path(__file__).resolve().parents[1] / "android"
source = android / "src/main/assets"
target = android / "build/offline-assets"
staged = target.with_name("offline-assets.tmp")
if staged.exists():
    rmtree(staged)
(staged / "art").mkdir(parents=True)
for path in source.iterdir():
    if path.is_file():
        copyfile(path, staged / path.name)
for path in (source / "art").iterdir():
    if not (path.name.startswith("phone") and path.suffix == ".webp"):
        copyfile(path, staged / "art" / path.name)

photos = sorted((source / "art").glob("phone*.webp"), key=lambda p: int(p.stem[5:]))
with (staged / "photos.pack").open("wb") as data, (staged / "photos.idx").open("wb") as index:
    index.write(struct.pack(">IQ", len(photos), 0))
    for number, path in enumerate(photos):
        if path.name != f"phone{number}.webp":
            raise ValueError(f"Missing photo for album number {number + 1}")
        data.write(path.read_bytes())
        index.write(struct.pack(">Q", data.tell()))

if target.exists():
    rmtree(target)
staged.rename(target)
