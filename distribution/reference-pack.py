"""Build the v2.4.0 photo reference used to transport the signed release."""
import sys
from zipfile import ZipFile

with ZipFile(sys.argv[1]) as old, open(sys.argv[2], "wb") as reference:
    for index in range(60303):
        reference.write(old.read(f"assets/art/phone{index}.webp"))
