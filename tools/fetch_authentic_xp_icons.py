#!/usr/bin/env python3
"""
fetch_authentic_xp_icons.py
Fetches, processes, and formats authentic 32-bit alpha-blended Windows XP icons
from high-fidelity verified repositories and places them in LunaLauncher,
WindowsExplorer, and other application drawable directories.
"""

import os
import sys
import urllib.request
from PIL import Image, ImageDraw, ImageFont

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LUNA_DRAWABLE = os.path.join(BASE_DIR, "apps", "LunaLauncher", "res", "drawable")
EXPLORER_DRAWABLE = os.path.join(BASE_DIR, "apps", "WindowsExplorer", "res", "drawable")

os.makedirs(LUNA_DRAWABLE, exist_ok=True)
os.makedirs(EXPLORER_DRAWABLE, exist_ok=True)

RAW_URL_BASE = "https://raw.githubusercontent.com/wieslawsoltes/WinXp/master/dist/assets/icons"

ICONS_TO_FETCH = {
    "ic_my_computer.png": f"{RAW_URL_BASE}/xp-my-computer-48.png",
    "ic_my_documents.png": f"{RAW_URL_BASE}/xp-my-documents-48.png",
    "ic_drive_c.png": f"{RAW_URL_BASE}/xp-hard-drive-48.png",
    "ic_drive_d.png": f"{RAW_URL_BASE}/xp-cd-drive-48.png",
    "ic_drive_system.png": f"{RAW_URL_BASE}/xp-hard-drive-48.png",
    "ic_folder.png": f"{RAW_URL_BASE}/xp-folder-48.png",
    "ic_folder_open.png": f"{RAW_URL_BASE}/xp-folder-open-48.png",
    "ic_folder_docs.png": f"{RAW_URL_BASE}/xp-my-documents-48.png",
    "ic_paint.png": f"{RAW_URL_BASE}/paint.png",
    "ic_notepad.png": f"{RAW_URL_BASE}/notepad.png",
    "ic_calc.png": f"{RAW_URL_BASE}/calculator.png",
    "ic_ie.png": f"{RAW_URL_BASE}/internet-explorer.png",
    "ic_wmp.png": f"{RAW_URL_BASE}/media-player.png",
    "ic_recycle_bin.png": f"{RAW_URL_BASE}/xp-recycle-bin-empty-48.png",
    "ic_recycle_bin_full.png": f"{RAW_URL_BASE}/xp-recycle-bin-full-48.png",
    "ic_control_panel.png": f"{RAW_URL_BASE}/xp-control-panel-48.png",
}

def fetch_and_resize(target_filename, url, target_size=(48, 48)):
    print(f"Fetching {target_filename} from {url}...")
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp:
            data = resp.read()
            tmp_path = f"/tmp/{target_filename}"
            with open(tmp_path, "wb") as f:
                f.write(data)
            
            im = Image.open(tmp_path).convert("RGBA")
            if im.size != target_size:
                im = im.resize(target_size, Image.Resampling.LANCZOS)
            
            # Save to LunaLauncher
            luna_dest = os.path.join(LUNA_DRAWABLE, target_filename)
            im.save(luna_dest, "PNG")
            
            # Also save to WindowsExplorer if relevant
            explorer_dest = os.path.join(EXPLORER_DRAWABLE, target_filename)
            im.save(explorer_dest, "PNG")
            print(f"  Saved {target_filename} ({target_size[0]}x{target_size[1]})")
    except Exception as e:
        print(f"  Error fetching {target_filename}: {e}")

def create_cmd_prompt_icon():
    """Generates authentic 48x48 Windows XP Command Prompt icon."""
    im = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    
    # Outer CRT monitor / window frame with drop shadow
    draw.rectangle([6, 8, 44, 42], fill=(0, 0, 0, 60)) # Shadow
    draw.rectangle([4, 6, 42, 40], fill=(212, 208, 200, 255), outline=(0, 0, 0, 255), width=1)
    
    # Classic XP blue title bar
    draw.rectangle([5, 7, 41, 14], fill=(10, 36, 106, 255))
    
    # Title bar close button
    draw.rectangle([36, 8, 40, 13], fill=(212, 208, 200, 255), outline=(128, 128, 128, 255), width=1)
    
    # Black console screen
    draw.rectangle([6, 15, 40, 38], fill=(0, 0, 0, 255))
    
    # Prompt text 'C:\>'
    # Simple bitmap representation of C:\_
    # 'C'
    draw.line([9, 20, 13, 20], fill=(255, 255, 255, 255))
    draw.line([9, 20, 9, 26], fill=(255, 255, 255, 255))
    draw.line([9, 26, 13, 26], fill=(255, 255, 255, 255))
    
    # ':'
    draw.point((15, 22), fill=(255, 255, 255, 255))
    draw.point((15, 24), fill=(255, 255, 255, 255))
    
    # '\'
    draw.line([18, 20, 21, 26], fill=(255, 255, 255, 255))
    
    # '>'
    draw.line([24, 21, 27, 23], fill=(255, 255, 255, 255))
    draw.line([27, 23, 24, 25], fill=(255, 255, 255, 255))
    
    # Blinking cursor '_'
    draw.line([30, 26, 34, 26], fill=(255, 255, 255, 255))
    
    dest1 = os.path.join(LUNA_DRAWABLE, "ic_cmd.png")
    im.save(dest1, "PNG")
    print("  Created ic_cmd.png (48x48)")

def create_restore_button():
    """Generates authentic Luna 21x21 restore button with overlapping squares."""
    size = (21, 21)
    im = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    
    # Luna blue gradient pill background
    for y in range(size[1]):
        r = int(0 + (y / size[1]) * 20)
        g = int(85 + (y / size[1]) * 40)
        b = int(234 - (y / size[1]) * 40)
        draw.line([(1, y), (size[0]-2, y)], fill=(r, g, b, 255))
    
    # Border
    draw.rectangle([0, 0, size[0]-1, size[1]-1], outline=(255, 255, 255, 180), width=1)
    
    # Overlapping squares symbol (white)
    # Background square
    draw.rectangle([8, 5, 15, 12], outline=(255, 255, 255, 255), width=2)
    # Foreground square
    draw.rectangle([5, 8, 12, 15], outline=(255, 255, 255, 255), width=2)
    
    dest1 = os.path.join(LUNA_DRAWABLE, "btn_restore.png")
    im.save(dest1, "PNG")
    dest2 = os.path.join(EXPLORER_DRAWABLE, "btn_restore.png")
    im.save(dest2, "PNG")
    print("  Created btn_restore.png (21x21)")

def main():
    print("--- Fetching & Generating Authentic Windows XP Assets ---")
    for filename, url in ICONS_TO_FETCH.items():
        fetch_and_resize(filename, url)
    
    create_cmd_prompt_icon()
    create_restore_button()
    print("--- Done! All assets placed. ---")

if __name__ == "__main__":
    main()
