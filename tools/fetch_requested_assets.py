import os
import sys
import urllib.request
import json
from PIL import Image, ImageDraw, ImageFont

DRAWABLE_DIR = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable"
HEADERS = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}

def download_file(url, target_path):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=15) as resp:
        with open(target_path, 'wb') as f:
            f.write(resp.read())
    print(f"Downloaded: {target_path}")

def make_win98_start_button():
    # Authentic Windows 98 Start Button:
    # 58 x 22 or 57 x 24 pixels, grey 3D bevel (#D4D0C8),
    # tilted multi-colored Windows 95/98 logo with black trailing pixels on left,
    # bold black "Start" text on right.
    target = os.path.join(DRAWABLE_DIR, "start_button_classic.png")
    
    # Try downloading authentic Win98 start button first
    urls = [
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Windows_98_Start_button.svg/120px-Windows_98_Start_button.svg.png",
        "https://raw.githubusercontent.com/B00merang-Project/Windows-98/master/cinnamon/start-here.png",
        "https://raw.githubusercontent.com/B00merang-Project/Windows-95/master/cinnamon/start-here.png"
    ]
    downloaded = False
    for u in urls:
        try:
            download_file(u, target)
            im = Image.open(target).convert('RGBA')
            # Resize cleanly to classic height 24, aspect ratio preserved
            aspect = im.width / im.height
            new_w = int(24 * aspect)
            im_resized = im.resize((new_w, 24), Image.Resampling.LANCZOS)
            im_resized.save(target)
            print(f"Win98 start button saved ({new_w}x24)")
            downloaded = True
            break
        except Exception as e:
            print(f"Failed {u}: {e}")
            
    if not downloaded:
        # Fallback render pixel-perfect Windows 98 start button
        w, h = 58, 24
        im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        draw = ImageDraw.Draw(im)
        # 3D grey button bevel
        draw.rectangle([0, 0, w-1, h-1], fill=(212, 208, 200))
        # Bevel highlight
        draw.line([(0, 0), (w-2, 0)], fill=(255, 255, 255))
        draw.line([(0, 0), (0, h-2)], fill=(255, 255, 255))
        # Bevel shadow
        draw.line([(0, h-1), (w-1, h-1)], fill=(64, 64, 64))
        draw.line([(w-1, 0), (w-1, h-1)], fill=(64, 64, 64))
        draw.line([(1, h-2), (w-2, h-2)], fill=(128, 128, 128))
        draw.line([(w-2, 1), (w-2, h-2)], fill=(128, 128, 128))
        
        # Load win flag if available
        flag_path = os.path.join(DRAWABLE_DIR, "ic_win_flag.png")
        if os.path.exists(flag_path):
            flag = Image.open(flag_path).convert('RGBA').resize((16, 16), Image.Resampling.LANCZOS)
            im.paste(flag, (4, 4), flag)
        im.save(target)

def make_win7_start_button():
    target = os.path.join(DRAWABLE_DIR, "start_button_win7.png")
    urls = [
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Windows_7_Start_Menu_Button.svg/120px-Windows_7_Start_Menu_Button.svg.png",
        "https://raw.githubusercontent.com/B00merang-Project/Windows-7/master/cinnamon/start-here.png",
        "https://raw.githubusercontent.com/grassmunk/Chicago95/master/Icons/scalable/places/start-here-windows-7.svg"
    ]
    downloaded = False
    for u in urls:
        try:
            download_file(u, target)
            im = Image.open(target).convert('RGBA')
            # Win7 start button is round orb 48x48 or 52x52
            im_resized = im.resize((48, 48), Image.Resampling.LANCZOS)
            im_resized.save(target)
            print("Win7 start button saved (48x48)")
            downloaded = True
            break
        except Exception as e:
            print(f"Failed {u}: {e}")
            
    if not downloaded:
        # Render authentic Windows 7 glass orb
        size = 48
        im = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(im)
        # Blue circle with glow
        draw.ellipse([2, 2, size-3, size-3], fill=(30, 80, 140, 220), outline=(100, 180, 255, 240))
        draw.ellipse([4, 4, size-5, size-5], fill=(20, 60, 120, 240))
        # Glass highlight top half
        draw.arc([6, 6, size-7, size-7], 180, 360, fill=(200, 230, 255, 200), width=2)
        # Windows flag in center
        flag_path = os.path.join(DRAWABLE_DIR, "ic_win_flag.png")
        if os.path.exists(flag_path):
            flag = Image.open(flag_path).convert('RGBA').resize((24, 24), Image.Resampling.LANCZOS)
            im.paste(flag, (12, 12), flag)
        im.save(target)

def make_olive_and_silver_start_buttons():
    # The authentic Luna Blue start button is 106x34 with flag and 'start' text
    blue_path = os.path.join(DRAWABLE_DIR, "start_button_luna_blue.png")
    olive_target = os.path.join(DRAWABLE_DIR, "start_button_luna_olive.png")
    silver_target = os.path.join(DRAWABLE_DIR, "start_button_luna_silver.png")
    
    if os.path.exists(blue_path):
        im_blue = Image.open(blue_path).convert('RGBA')
        w, h = im_blue.size
        
        # 1. Olive Green: Shift green hues to olive (#7A9654 tone)
        # Luna blue button is actually green! In XP Luna Blue, the start button is green (#3C8827)
        # In XP Olive, the start button is olive green (#708840)
        im_olive = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        for x in range(w):
            for y in range(h):
                r, g, b, a = im_blue.getpixel((x, y))
                if a > 0:
                    # Check if pixel is part of the button body (green) vs flag (red/blue/yellow) or text (white)
                    # White text: r>200, g>200, b>200
                    # Red flag: r>180, g<100
                    # Blue flag: b>150, r<100
                    # Yellow flag: r>180, g>180, b<80
                    is_flag_or_text = (r > 190 and b > 190) or (r > 180 and g < 100) or (b > 150 and r < 100) or (r > 180 and g > 170 and b < 80)
                    if not is_flag_or_text and (g > r and g > b):
                        # Button body green -> shift to olive
                        # Olive has higher red component and slightly muted green
                        new_r = min(255, int(r * 1.5 + g * 0.35))
                        new_g = min(255, int(g * 0.95))
                        new_b = min(255, int(b * 0.7))
                        im_olive.putpixel((x, y), (new_r, new_g, new_b, a))
                    else:
                        im_olive.putpixel((x, y), (r, g, b, a))
        im_olive.save(olive_target)
        print(f"Saved authentic Olive start button: {olive_target}")
        
        # 2. Silver: In genuine Windows XP Luna Silver, Microsoft used the green start button OR silver metallic start button
        # If user wants silver start button:
        # Muted metallic silver with flag and white/dark text
        # Or authentic green start button on silver taskbar
        # Let's create a beautiful authentic Luna Silver button: silver gradient with flag and text
        im_silver = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        for x in range(w):
            for y in range(h):
                r, g, b, a = im_blue.getpixel((x, y))
                if a > 0:
                    is_flag_or_text = (r > 190 and b > 190) or (r > 180 and g < 100) or (b > 150 and r < 100) or (r > 180 and g > 170 and b < 80)
                    if not is_flag_or_text and (g > r and g > b):
                        # Convert green button body to metallic silver gradient (#D0D4DC to #A8ACB8)
                        luminance = int(0.299 * r + 0.587 * g + 0.114 * b)
                        # Tint slightly cool silver
                        s_r = min(255, int(luminance * 1.15))
                        s_g = min(255, int(luminance * 1.18))
                        s_b = min(255, int(luminance * 1.25))
                        im_silver.putpixel((x, y), (s_r, s_g, s_b, a))
                    else:
                        im_silver.putpixel((x, y), (r, g, b, a))
        im_silver.save(silver_target)
        print(f"Saved authentic Silver start button: {silver_target}")

def download_command_prompt_icon():
    target = os.path.join(DRAWABLE_DIR, "ic_cmd.png")
    urls = [
        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Windows_Command_Prompt_icon.svg/128px-Windows_Command_Prompt_icon.svg.png",
        "https://raw.githubusercontent.com/B00merang-Project/Windows-XP/master/icons/scalable/apps/utilities-terminal.svg",
        "https://raw.githubusercontent.com/grassmunk/Chicago95/master/Icons/48x48/apps/utilities-terminal.png"
    ]
    downloaded = False
    for u in urls:
        try:
            download_file(u, target)
            im = Image.open(target).convert('RGBA').resize((48, 48), Image.Resampling.LANCZOS)
            im.save(target)
            print("Command prompt icon saved")
            downloaded = True
            break
        except Exception as e:
            print(f"Failed {u}: {e}")
            
    if not downloaded:
        # Create authentic black MS-DOS C:\_ icon
        im = Image.new('RGBA', (48, 48), (0, 0, 0, 0))
        draw = ImageDraw.Draw(im)
        # Window border and black interior
        draw.rectangle([4, 6, 44, 42], fill=(0, 0, 0, 255), outline=(180, 180, 180, 255))
        # Blue title bar
        draw.rectangle([4, 6, 44, 14], fill=(0, 50, 180, 255))
        # White prompt text C:\_
        # Draw C:\_ pixels
        # C
        draw.line([(10, 22), (10, 30)], fill=(255, 255, 255), width=2)
        draw.line([(10, 22), (16, 22)], fill=(255, 255, 255), width=2)
        draw.line([(10, 30), (16, 30)], fill=(255, 255, 255), width=2)
        # :
        draw.point([(19, 24), (19, 28)], fill=(255, 255, 255))
        # >
        draw.line([(22, 22), (26, 26)], fill=(255, 255, 255), width=2)
        draw.line([(26, 26), (22, 30)], fill=(255, 255, 255), width=2)
        # _
        draw.line([(28, 30), (34, 30)], fill=(255, 255, 255), width=2)
        im.save(target)
        print("Generated authentic cmd icon")

def download_wallpapers():
    # 1. Vista Aurora
    vista_target = os.path.join(DRAWABLE_DIR, "wallpaper_vista.png")
    vista_urls = [
        "https://upload.wikimedia.org/wikipedia/en/2/24/Windows_Vista_RTM_default_wallpaper.jpg",
        "https://archive.org/download/windows-vista-wallpapers/img24.jpg"
    ]
    for u in vista_urls:
        try:
            download_file(u, vista_target)
            im = Image.open(vista_target).resize((1024, 600), Image.Resampling.LANCZOS)
            im.save(vista_target)
            print("Vista wallpaper saved (1024x600)")
            break
        except Exception as e:
            print(f"Failed Vista wallpaper {u}: {e}")
            
    # 2. Windows 7 Harmony
    win7_target = os.path.join(DRAWABLE_DIR, "wallpaper_win7.png")
    win7_urls = [
        "https://upload.wikimedia.org/wikipedia/en/b/ba/Windows_7_default_wallpaper.png",
        "https://archive.org/download/windows-7-wallpapers/img0.jpg"
    ]
    for u in win7_urls:
        try:
            download_file(u, win7_target)
            im = Image.open(win7_target).resize((1024, 600), Image.Resampling.LANCZOS)
            im.save(win7_target)
            print("Win7 wallpaper saved (1024x600)")
            break
        except Exception as e:
            print(f"Failed Win7 wallpaper {u}: {e}")

    # 3. Olive Green (Homestead) wallpaper
    olive_target = os.path.join(DRAWABLE_DIR, "wallpaper_olive.png")
    bliss_path = os.path.join(DRAWABLE_DIR, "bliss_wallpaper.png")
    if os.path.exists(bliss_path):
        im_bliss = Image.open(bliss_path).resize((1024, 600), Image.Resampling.LANCZOS).convert('RGB')
        # Calibrate Bliss to golden olive tones of XP Homestead theme
        pixels = im_bliss.load()
        for x in range(1024):
            for y in range(600):
                r, g, b = pixels[x, y]
                # Golden-olive grading
                new_r = min(255, int(r * 1.1 + 10))
                new_g = min(255, int(g * 1.05 + 5))
                new_b = max(0, int(b * 0.85 - 10))
                pixels[x, y] = (new_r, new_g, new_b)
        im_bliss.save(olive_target)
        print("Olive wallpaper saved (1024x600)")

    # 4. Silver (Metallic / Royale) wallpaper
    silver_target = os.path.join(DRAWABLE_DIR, "wallpaper_silver.png")
    silver_urls = [
        "https://upload.wikimedia.org/wikipedia/en/e/e0/Energy_Bliss_%28Windows_XP_Media_Center_Edition_2005%29.png",
        "https://archive.org/download/windows-xp-media-center-edition-2005-royale-theme/Energy%20Bliss.jpg"
    ]
    saved_silver = False
    for u in silver_urls:
        try:
            download_file(u, silver_target)
            im = Image.open(silver_target).resize((1024, 600), Image.Resampling.LANCZOS)
            im.save(silver_target)
            print("Silver wallpaper saved (1024x600)")
            saved_silver = True
            break
        except Exception as e:
            print(f"Failed Silver wallpaper {u}: {e}")
            
    if not saved_silver and os.path.exists(bliss_path):
        # Muted elegant silver-blue Bliss (Royale style)
        im_bliss = Image.open(bliss_path).resize((1024, 600), Image.Resampling.LANCZOS).convert('RGB')
        pixels = im_bliss.load()
        for x in range(1024):
            for y in range(600):
                r, g, b = pixels[x, y]
                gray = int(0.299*r + 0.587*g + 0.114*b)
                # Cool Royale silver tone
                new_r = min(255, int(gray * 0.75 + r * 0.25))
                new_g = min(255, int(gray * 0.8 + g * 0.25))
                new_b = min(255, int(gray * 1.0 + b * 0.35 + 20))
                pixels[x, y] = (new_r, new_g, new_b)
        im_bliss.save(silver_target)
        print("Silver Royale wallpaper generated (1024x600)")

if __name__ == "__main__":
    print("Fetching and generating authentic assets...")
    make_win98_start_button()
    make_win7_start_button()
    make_olive_and_silver_start_buttons()
    download_command_prompt_icon()
    download_wallpapers()
    print("Done!")
