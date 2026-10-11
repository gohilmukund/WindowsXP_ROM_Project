import os
import math
import colorsys
import numpy as np
from PIL import Image, ImageDraw, ImageFont

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DRAWABLE_DIR = os.path.join(PROJECT_ROOT, "apps", "LunaLauncher", "res", "drawable")

def find_win7_orb_source():
    candidates = [
        r"C:\Users\gohil\.gemini\antigravity\brain\e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc\scratch\win7_orb_256.png",
        os.path.expanduser("~/.gemini/antigravity/brain/e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc/scratch/win7_orb_256.png"),
        "/mnt/c/Users/gohil/.gemini/antigravity/brain/e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc/scratch/win7_orb_256.png",
    ]
    for c in candidates:
        if os.path.exists(c):
            return c
    return None

def find_win11_wallpaper_source():
    candidates = [
        r"C:\Windows\Web\Wallpaper\Windows\img0.jpg",
        "/mnt/c/Windows/Web/Wallpaper/Windows/img0.jpg",
    ]
    for c in candidates:
        if os.path.exists(c):
            return c
    return None

def find_framdit_font():
    candidates = [
        r"C:\Windows\Fonts\framdit.ttf",
        "/mnt/c/Windows/Fonts/framdit.ttf",
    ]
    for c in candidates:
        if os.path.exists(c):
            return c
    return None

def generate_luna_pill_buttons():
    # Base authentic Luna button
    src_path = os.path.join(DRAWABLE_DIR, "start_button_luna_blue.png")
    src = Image.open(src_path).convert("RGBA")
    # Resize to exact 106x30
    res = src.resize((106, 30), Image.Resampling.LANCZOS)
    arr = np.array(res)
    h_img, w_img = arr.shape[:2]

    # 1. Olive Green Luna Button (106x30)
    arr_olive = arr.copy()
    for y in range(h_img):
        for x in range(w_img):
            r, g, b, a = arr_olive[y, x]
            if a == 0:
                continue
            rf, gf, bf = r / 255.0, g / 255.0, b / 255.0
            h, s, v = colorsys.rgb_to_hsv(rf, gf, bf)
            
            # Avoid flag panes (x < 28, y in 10..22)
            is_flag_pane = (x < 28) and (9 <= y <= 22) and (15 <= x <= 27)
            if 95 <= h*360 <= 145 and s > 0.20 and not is_flag_pane:
                # Authentic XP Homestead Olive Green
                h_new = 84.0 / 360.0
                s_new = min(1.0, s * 0.88)
                v_new = v * 0.95
                rn, gn, bn = colorsys.hsv_to_rgb(h_new, s_new, v_new)
                arr_olive[y, x] = [int(rn*255), int(gn*255), int(bn*255), a]

    img_olive = Image.fromarray(arr_olive)
    dest_olive = os.path.join(DRAWABLE_DIR, "start_button_luna_olive.png")
    img_olive.save(dest_olive, "PNG")
    print(f"[OK] Saved {dest_olive}: size={img_olive.size}")

def generate_start_button_royale():
    # Authentic Microsoft royale.msstyles pill from RetroBar
    pill_path = os.path.join(PROJECT_ROOT, "tools", "cache", "RetroBar", "RetroBar", "Resources", "startxproyalebutton.png")
    if not os.path.exists(pill_path):
        raise FileNotFoundError(f"Missing RetroBar royale button: {pill_path}")
    pill_src = Image.open(pill_path).convert("RGBA")
    # Crop normal state (top 33px) and resize to 106x34 with LANCZOS
    pill = pill_src.crop((0, 0, 99, 33)).resize((106, 34), Image.Resampling.LANCZOS)

    # Paste authentic flag from ic_turn_off_header_flag.png at (14, 9)
    flag_path = os.path.join(DRAWABLE_DIR, "ic_turn_off_header_flag.png")
    if not os.path.exists(flag_path):
        raise FileNotFoundError(f"Missing header flag: {flag_path}")
    flag_src = Image.open(flag_path).convert("RGBA")
    flag_resized = flag_src.resize((18, 17), Image.Resampling.LANCZOS)
    canvas = pill.copy()
    canvas.paste(flag_resized, (14, 9), flag_resized)

    # Render 'start' text using Franklin Gothic Medium Italic font
    font_path = find_framdit_font()
    if font_path:
        font = ImageFont.truetype(font_path, 19)
    else:
        raise FileNotFoundError("Franklin Gothic Medium Italic font (framdit.ttf) not found")
    draw = ImageDraw.Draw(canvas)
    # Dark green drop shadow (0, 60, 10, 180) at (39, 7)
    draw.text((39, 7), "start", font=font, fill=(0, 60, 10, 180))
    # White text (255, 255, 255, 255) at (38, 6)
    draw.text((38, 6), "start", font=font, fill=(255, 255, 255, 255))

    dest_royale = os.path.join(DRAWABLE_DIR, "start_button_royale.png")
    canvas.save(dest_royale, "PNG")
    print(f"[OK] Saved {dest_royale}: size={canvas.size}")

def generate_win7_orb():
    orb_src_path = find_win7_orb_source()
    if not orb_src_path:
        raise FileNotFoundError("win7_orb_256.png not found")
    orb = Image.open(orb_src_path).convert("RGBA")
    # Target size: 144x96 (48x32dp at 3x density). Center 31dp (93px) circular orb
    W, H = 144, 96
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    orb_size = 93
    orb_resized = orb.resize((orb_size, orb_size), Image.Resampling.LANCZOS)
    offset_x = (W - orb_size) // 2
    offset_y = (H - orb_size) // 2
    canvas.paste(orb_resized, (offset_x, offset_y), orb_resized)

    dest_win7 = os.path.join(DRAWABLE_DIR, "start_button_win7.png")
    canvas.save(dest_win7, "PNG")
    print(f"[OK] Saved {dest_win7}: size={canvas.size}")

def generate_win11_button():
    # 4. Windows 11 Fluent 4-Square Logo (42x32)
    W, H = 42, 32
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    
    # Windows 11 brand blue (#0078D4)
    color = (0, 120, 212, 255)
    
    # 4 clean squares: 7x7 each, 2px gap, centered
    # Left column: 13..19, Right column: 22..28
    # Top row: 8..14, Bottom row: 17..23
    d.rectangle([13, 8, 19, 14], fill=color)
    d.rectangle([22, 8, 28, 14], fill=color)
    d.rectangle([13, 17, 19, 23], fill=color)
    d.rectangle([22, 17, 28, 23], fill=color)
    
    dest_win11 = os.path.join(DRAWABLE_DIR, "start_button_win11.png")
    im.save(dest_win11, "PNG")
    print(f"[OK] Saved {dest_win11}: size={im.size}")

def generate_wallpapers():
    # 5. wallpaper_win11.png (1024x600 clean master Bloom wallpaper)
    win11_src_path = find_win11_wallpaper_source()
    if not win11_src_path:
        raise FileNotFoundError("Windows 11 master wallpaper img0.jpg not found")
    img = Image.open(win11_src_path).convert("RGB")
    target_w, target_h = 1024, 600
    src_w, src_h = img.size
    target_aspect = target_w / target_h
    src_aspect = src_w / src_h

    if src_aspect > target_aspect:
        crop_w = int(src_h * target_aspect)
        crop_h = src_h
        offset_x = (src_w - crop_w) // 2
        offset_y = 0
    else:
        crop_w = src_w
        crop_h = int(src_w / target_aspect)
        offset_x = 0
        offset_y = (src_h - crop_h) // 2

    crop_box = (offset_x, offset_y, offset_x + crop_w, offset_y + crop_h)
    cropped = img.crop(crop_box)
    wall11 = cropped.resize((target_w, target_h), Image.Resampling.LANCZOS)
    dest11 = os.path.join(DRAWABLE_DIR, "wallpaper_win11.png")
    wall11.save(dest11, "PNG")
    print(f"[OK] Saved {dest11}: size={wall11.size}")

    # 6. wallpaper_royale.png (1024x600 Energy Blue)
    bliss_path = os.path.join(DRAWABLE_DIR, "bliss_wallpaper.png")
    bliss = Image.open(bliss_path).convert("RGB")
    arr = np.array(bliss, dtype=np.float32)

    r = arr[:, :, 0]
    g = arr[:, :, 1]
    b = arr[:, :, 2]

    is_hill = (g > b * 0.95) & (g > r * 0.9)
    h, w = arr.shape[:2]
    y_norm = np.linspace(0.0, 1.0, h)[:, None]

    # Smooth sky transition from cobalt blue at top (30, 76, 166) to azure cyan (69, 148, 227)
    sky_t = np.clip(y_norm / 0.70, 0.0, 1.0)
    factor_r = 0.20 + 0.65 * sky_t
    factor_g = 0.40 + 0.67 * sky_t
    factor_b = 0.70 + 0.28 * sky_t

    sky_r = r * factor_r
    sky_g = g * factor_g
    sky_b = b * factor_b

    # Hill: lush vibrant emerald green
    hill_r = r * 0.85
    hill_g = np.clip(g * 1.42, 0, 255)
    hill_b = b * 0.88

    out = np.zeros_like(arr)
    out[:, :, 0] = np.where(is_hill, hill_r, sky_r)
    out[:, :, 1] = np.where(is_hill, hill_g, sky_g)
    out[:, :, 2] = np.where(is_hill, hill_b, sky_b)

    out = np.clip(out, 0, 255).astype(np.uint8)
    wall_royale = Image.fromarray(out)
    dest_royale = os.path.join(DRAWABLE_DIR, "wallpaper_royale.png")
    wall_royale.save(dest_royale, "PNG")
    print(f"[OK] Saved {dest_royale}: size={wall_royale.size}")

if __name__ == "__main__":
    generate_luna_pill_buttons()
    generate_start_button_royale()
    generate_win7_orb()
    generate_win11_button()
    generate_wallpapers()
    print("All theme assets generated successfully!")
