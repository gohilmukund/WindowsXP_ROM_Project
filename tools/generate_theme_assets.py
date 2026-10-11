import os
import math
import colorsys
import numpy as np
from PIL import Image, ImageDraw

PROJECT_ROOT = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project"
DRAWABLE_DIR = os.path.join(PROJECT_ROOT, "apps/LunaLauncher/res/drawable")
REF_DIR = "/mnt/c/Users/gohil/.gemini/antigravity/brain/e4519e1e-a57e-4b5b-8ef0-8128cf9a52bc/.user_uploaded"

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

    # 2. Royale Emerald Green Luna Button (106x30)
    arr_royale = arr.copy()
    for y in range(h_img):
        for x in range(w_img):
            r, g, b, a = arr_royale[y, x]
            if a == 0:
                continue
            rf, gf, bf = r / 255.0, g / 255.0, b / 255.0
            h, s, v = colorsys.rgb_to_hsv(rf, gf, bf)
            
            is_flag_pane = (x < 28) and (9 <= y <= 22) and (15 <= x <= 27)
            if 95 <= h*360 <= 145 and s > 0.20 and not is_flag_pane:
                # Authentic XP Media Center / Royale Emerald Green
                h_new = 118.0 / 360.0
                s_new = min(1.0, s * 1.15)
                v_new = min(1.0, v * 1.10)
                rn, gn, bn = colorsys.hsv_to_rgb(h_new, s_new, v_new)
                arr_royale[y, x] = [int(rn*255), int(gn*255), int(bn*255), a]

    img_royale = Image.fromarray(arr_royale)
    dest_royale = os.path.join(DRAWABLE_DIR, "start_button_royale.png")
    img_royale.save(dest_royale, "PNG")
    print(f"[OK] Saved {dest_royale}: size={img_royale.size}")

def generate_win7_orb():
    # 3. Windows 7 Aero Orb (48x48)
    W, H = 48, 48
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    
    cx, cy = 23.5, 23.5
    R = 19.5
    
    # Render glass sphere pixel-by-pixel
    for y in range(H):
        for x in range(W):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            
            # Outside outer halo
            if dist > R + 2.2:
                continue
                
            # Outer cyan glass glow
            if dist > R:
                glow_alpha = int(max(0, 1.0 - (dist - R) / 2.2) * 80)
                im.putpixel((x, y), (110, 195, 255, glow_alpha))
                continue
                
            # Normalized coordinates
            nx = dx / R
            ny = dy / R
            nz = math.sqrt(max(0.0, 1.0 - nx*nx - ny*ny))
            
            # Base dark blue sphere gradient (Aero glass)
            t = (ny + 1.0) / 2.0
            r_base = int(22 * (1 - t) + 8 * t)
            g_base = int(100 * (1 - t) + 40 * t)
            b_base = int(205 * (1 - t) + 110 * t)
            
            # Specular glass reflection on top dome
            rx_d = dx / 11.5
            ry_d = (dy + 5.5) / 7.0
            refl_dist = rx_d*rx_d + ry_d*ry_d
            refl = max(0.0, 1.0 - refl_dist) if refl_dist < 1.0 else 0.0
            
            # Rim highlight
            rim = math.pow(1.0 - nz, 2.0)
            
            r_col = int(min(255, r_base + refl * 150 + rim * 30))
            g_col = int(min(255, g_base + refl * 180 + rim * 70))
            b_col = int(min(255, b_base + refl * 220 + rim * 110))
            
            # Antialiasing at outer rim
            alpha = 255
            if dist > R - 1.0:
                alpha = int(max(0, (R - dist) * 255))
                
            im.putpixel((x, y), (r_col, g_col, b_col, alpha))

    # Glass dome sheen overlay
    for y in range(6, 22):
        for x in range(12, 36):
            dx = x - cx
            dy = (y - (cy - 5.5)) / 0.65
            if dx*dx + dy*dy < 11.0*11.0:
                p = im.getpixel((x, y))
                glow = max(0, int((1.0 - (dx*dx + dy*dy)/(11.0*11.0)) * 75))
                im.putpixel((x, y), (min(255, p[0] + glow), min(255, p[1] + glow), min(255, p[2] + glow), p[3]))
                
    # Windows 7 4-colored wavy flag in center
    d = ImageDraw.Draw(im)
    # Top-Left: Red pane (#EB3C28)
    d.polygon([(16, 17), (22, 16), (22, 22), (16, 23)], fill=(235, 60, 40, 255))
    # Top-Right: Green pane (#70C420)
    d.polygon([(24, 16), (31, 15), (31, 21), (24, 22)], fill=(112, 196, 32, 255))
    # Bottom-Left: Blue pane (#00A6F0)
    d.polygon([(16, 24), (22, 23), (22, 29), (16, 30)], fill=(0, 166, 240, 255))
    # Bottom-Right: Yellow pane (#FFBE00)
    d.polygon([(24, 23), (31, 22), (31, 28), (24, 29)], fill=(255, 190, 0, 255))
    
    # Soft inner glass reflection over the flag
    for y in range(16, 22):
        for x in range(16, 31):
            p = im.getpixel((x, y))
            im.putpixel((x, y), (min(255, p[0] + 30), min(255, p[1] + 30), min(255, p[2] + 30), p[3]))

    dest_win7 = os.path.join(DRAWABLE_DIR, "start_button_win7.png")
    im.save(dest_win7, "PNG")
    print(f"[OK] Saved {dest_win7}: size={im.size}")

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
    # 5. wallpaper_win11.png (1024x600)
    ref_win11_path = os.path.join(REF_DIR, "media_1791731909634_623ee14a.png")
    if os.path.exists(ref_win11_path):
        im11 = Image.open(ref_win11_path).convert("RGB")
        desktop11 = im11.crop((0, 0, 1024, 648))
        wall11 = desktop11.resize((1024, 600), Image.Resampling.LANCZOS)
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
    generate_win7_orb()
    generate_win11_button()
    generate_wallpapers()
    print("All theme assets generated successfully!")
