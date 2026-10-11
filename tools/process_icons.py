from PIL import Image
import os

drawable_dir = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable"

# 1. Generate crisp 32x32 ic_pv_open.png and ic_wmp_open.png from ic_folder_open.png
folder_open_path = os.path.join(drawable_dir, "ic_folder_open.png")
if os.path.exists(folder_open_path):
    img = Image.open(folder_open_path).convert("RGBA")
    img_32 = img.resize((32, 32), Image.Resampling.LANCZOS)
    img_32.save(os.path.join(drawable_dir, "ic_pv_open.png"))
    img_32.save(os.path.join(drawable_dir, "ic_wmp_open.png"))
    print("Saved ic_pv_open.png & ic_wmp_open.png (32x32)")

# 2. Check and resize ic_pv_prev and ic_pv_next to 32x32 if needed
for name in ["ic_pv_prev.png", "ic_pv_next.png"]:
    p = os.path.join(drawable_dir, name)
    if os.path.exists(p):
        im = Image.open(p).convert("RGBA")
        if im.size != (32, 32):
            im = im.resize((32, 32), Image.Resampling.LANCZOS)
            im.save(p)
            print(f"Resized {name} to 32x32")

print("All icons processed.")
