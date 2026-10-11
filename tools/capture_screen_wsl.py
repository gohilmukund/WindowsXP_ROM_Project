import subprocess
import sys
import os
from PIL import Image

def capture(output_filename):
    adb = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/platform-tools/adb.exe"
    win_artifact_dir = r"C:\Users\gohil\.gemini\antigravity\brain\e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc"
    wsl_artifact_dir = "/mnt/c/Users/gohil/.gemini/antigravity/brain/e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc"
    
    wsl_target = os.path.join(wsl_artifact_dir, output_filename)
    win_temp = os.path.join(win_artifact_dir, "temp_screen.png")
    wsl_temp = os.path.join(wsl_artifact_dir, "temp_screen.png")
    temp_device = "/sdcard/temp_screen.png"

    subprocess.run([adb, "shell", "screencap", "-p", temp_device], check=True)
    subprocess.run([adb, "pull", temp_device, win_temp], check=True, stdout=subprocess.DEVNULL)
    
    im = Image.open(wsl_temp)
    if im.height > im.width:
        im = im.rotate(90, expand=True)
    im.save(wsl_target)
    print(f"Captured {wsl_target} with dimensions: {im.size}")

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "screen_capture.png"
    capture(out)
