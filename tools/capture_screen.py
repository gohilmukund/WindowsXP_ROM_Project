import subprocess
import sys
import os
from PIL import Image

def capture(output_path):
    adb = r"C:\Users\gohil\Downloads\Samsung_Tab3_SM-T210_Tools\platform-tools\adb.exe"
    artifact_dir = r"C:\Users\gohil\.gemini\antigravity\brain\e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc"
    
    if not os.path.isabs(output_path):
        target = os.path.join(artifact_dir, output_path)
    else:
        target = output_path

    temp_device = "/sdcard/temp_screen.png"
    temp_local = os.path.join(artifact_dir, "temp_screen.png")

    subprocess.run([adb, "shell", "screencap", "-p", temp_device], check=True)
    subprocess.run([adb, "pull", temp_device, temp_local], check=True, stdout=subprocess.DEVNULL)
    
    im = Image.open(temp_local)
    if im.height > im.width:
        im = im.rotate(90, expand=True)
    im.save(target)
    print(f"Captured {target} with dimensions: {im.size}")

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "screen_capture.png"
    capture(out)
