#!/usr/bin/env python3
"""ADB smoke flow. Every tap/swipe target comes from a fresh UI hierarchy."""
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("app/build/reports/world-qa")
OUT.mkdir(parents=True, exist_ok=True)
PACKAGE = "com.phonesimulator.game"
devices = subprocess.check_output(["adb", "devices"], text=True).splitlines()[1:]
serials = [line.split()[0] for line in devices if "\tdevice" in line]
if not serials:
    raise RuntimeError("No adb target")
SERIAL = next((s for s in serials if s.startswith("emulator-")), serials[0])


def adb(*args, binary=False):
    return subprocess.check_output(["adb", "-s", SERIAL, *args], text=not binary)


def tree(name):
    raw = adb("exec-out", "uiautomator", "dump", "/dev/tty")
    start = raw.find("<?xml")
    if start < 0:
        start = raw.find("<hierarchy")
    end = raw.rfind("</hierarchy>")
    if start < 0 or end < 0:
        raise RuntimeError("UI hierarchy unavailable: " + raw[:200])
    xml = raw[start:end + len("</hierarchy>")]
    (OUT / (name + ".xml")).write_text(xml, encoding="utf-8")
    return ET.fromstring(xml)


def bounds(node):
    points = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
    return points if len(points) == 4 else [0, 0, 0, 0]


def find(root, value, clickable=True, contains=False):
    for node in root.iter("node"):
        words = [node.get("text", ""), node.get("content-desc", "")]
        match = any(value in word if contains else value == word for word in words)
        x1, y1, x2, y2 = bounds(node)
        if match and x2 - x1 > 30 and y2 - y1 > 30 and (not clickable or node.get("clickable") == "true"):
            return node
    return None


def tap(value, name):
    for attempt in range(9):
        root = tree(name + "-" + str(attempt))
        node = find(root, value)
        if node is not None:
            x1, y1, x2, y2 = bounds(node)
            adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
            time.sleep(0.6)
            print("QA tapped:", value, flush=True)
            return
        scroll = next((n for n in root.iter("node") if n.get("scrollable") == "true"), None)
        if scroll is None:
            break
        x1, y1, x2, y2 = bounds(scroll)
        x = (x1 + x2) // 2
        adb("shell", "input", "swipe", str(x), str(y1 + (y2-y1)*3//4), str(x), str(y1 + (y2-y1)//4), "350")
        time.sleep(0.5)
    raise RuntimeError("Missing UI target after scrolling: " + value)


def screenshot(name):
    (OUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p", binary=True))
    tree(name)


try:
    # The selected instrumentation scenario prepares deterministic, test-only saved data.
    result = adb("shell", "am", "instrument", "-w", "-e", "class",
                 PACKAGE + ".WorldCompatibilityTest#visualWorldFlows",
                 PACKAGE + ".test/androidx.test.runner.AndroidJUnitRunner")
    (OUT / "fixture.txt").write_text(result)
    if "FAILURES" in result or "INSTRUMENTATION_FAILED" in result or "OK (1 test)" not in result:
        raise RuntimeError("QA fixture instrumentation failed")
    adb("shell", "am", "force-stop", PACKAGE)
    activity = adb("shell", "cmd", "package", "resolve-activity", "--brief", PACKAGE).strip().splitlines()[-1]
    if "/" not in activity:
        raise RuntimeError("Unable to resolve launcher activity")
    adb("shell", "am", "start", "-n", activity)
    time.sleep(1)
    tap("Мир телефонов 4.0", "hub-target")
    screenshot("world-hub")
    tap("📱 Экземпляры и виртуальная ОС", "inventory-target")
    tap("Открыть экран · Nokia 3310", "nokia-target")
    screenshot("nokia-os")
    tap("3D-просмотр", "3d-target")
    screenshot("phone-3d-front")
    root = tree("3d-drag-target")
    target = find(root, "Объёмный Nokia 3310", clickable=False, contains=True)
    if target is None:
        raise RuntimeError("3D view missing from UI tree")
    x1, y1, x2, y2 = bounds(target)
    adb("shell", "input", "swipe", str(x1+(x2-x1)//4), str((y1+y2)//2), str(x1+(x2-x1)*3//4), str((y1+y2)//2), "550")
    time.sleep(0.5)
    tap("Повернуть на 90°", "rotate-target")
    tap("Приблизить", "zoom-target")
    screenshot("phone-3d-rotated")
    crashes = adb("logcat", "-b", "crash", "-d")
    if PACKAGE in crashes and "FATAL EXCEPTION" in crashes:
        raise RuntimeError("Application crash captured in logcat")
    print("QA visual flow completed on", SERIAL, flush=True)
finally:
    (OUT / "crash-logcat.txt").write_text(adb("logcat", "-b", "crash", "-d"), encoding="utf-8")
