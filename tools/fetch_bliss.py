import os
import subprocess
import PIL.Image

dest_dir = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable"
os.makedirs(dest_dir, exist_ok=True)
dest_path = os.path.join(dest_dir, "bliss_wallpaper.png")

url = "https://upload.wikimedia.org/wikipedia/en/2/27/Bliss_%28Windows_XP%29.png"
subprocess.run(["curl", "-A", "Mozilla/5.0", "-s", "-L", "-o", "/tmp/bliss_orig.png", url], check=True)
img = PIL.Image.open("/tmp/bliss_orig.png")
img = img.resize((1024, 600), PIL.Image.Resampling.LANCZOS)
img.save(dest_path)
print("Saved Bliss wallpaper directly to:", dest_path, img.size)
