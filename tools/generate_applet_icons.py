import os
from PIL import Image, ImageDraw

def generate_applet_icons(target_dir):
    os.makedirs(target_dir, exist_ok=True)

    def save_icon(name, img):
        p = os.path.join(target_dir, f"{name}.png")
        img.save(p, "PNG")
        print(f"Saved {p}")

    # 1. ic_system_properties (48x48) - Computer with checkmark/gear
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Monitor bezel
    d.rounded_rectangle([6, 6, 38, 32], radius=3, fill=(230, 230, 235), outline=(120, 130, 150), width=2)
    # Screen glass
    d.rectangle([10, 10, 34, 28], fill=(0, 120, 215))
    d.polygon([(10, 24), (34, 18), (34, 28), (10, 28)], fill=(70, 160, 20))
    # Stand
    d.rectangle([19, 32, 25, 37], fill=(180, 185, 195))
    d.rounded_rectangle([14, 37, 30, 41], radius=2, fill=(210, 215, 225), outline=(100, 110, 130), width=1)
    # Big Golden Gear or Checkmark Badge in bottom right
    d.ellipse([26, 24, 44, 42], fill=(255, 204, 0), outline=(180, 140, 0), width=2)
    # Blue checkmark inside badge
    d.line([(31, 33), (35, 37)], fill=(0, 70, 180), width=3)
    d.line([(35, 37), (41, 29)], fill=(0, 70, 180), width=3)
    save_icon("ic_system_properties", img)

    # 2. ic_network (48x48) - Two connected networked computers
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Rear PC (top right)
    d.rounded_rectangle([18, 6, 42, 26], radius=2, fill=(210, 215, 225), outline=(120, 130, 150), width=1)
    d.rectangle([21, 9, 39, 23], fill=(0, 120, 215))
    d.rectangle([28, 26, 32, 30], fill=(180, 185, 195))
    d.rectangle([24, 30, 36, 33], fill=(200, 205, 215))
    # Front PC (bottom left)
    d.rounded_rectangle([6, 16, 30, 36], radius=2, fill=(235, 240, 245), outline=(90, 100, 120), width=2)
    d.rectangle([9, 19, 27, 33], fill=(0, 150, 255))
    d.rectangle([16, 36, 20, 40], fill=(180, 185, 195))
    d.rectangle([12, 40, 24, 43], fill=(210, 215, 225), outline=(100, 110, 130), width=1)
    # Network cable / bus underneath
    d.line([(8, 45), (42, 45)], fill=(0, 0, 0), width=2)
    d.line([(18, 43), (18, 45)], fill=(0, 0, 0), width=2)
    d.line([(30, 33), (30, 45)], fill=(0, 0, 0), width=2)
    # Activity lights (green/amber)
    d.ellipse([10, 34, 12, 36], fill=(50, 220, 50))
    d.ellipse([22, 24, 24, 26], fill=(255, 180, 0))
    save_icon("ic_network", img)

    # 3. ic_display (48x48) - CRT monitor with color calibration palette
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Bezel
    d.rounded_rectangle([4, 6, 44, 34], radius=3, fill=(235, 235, 240), outline=(100, 110, 130), width=2)
    # Screen glass
    d.rectangle([8, 10, 40, 30], fill=(20, 30, 50))
    # Color bars on screen (Red, Green, Blue, Yellow, Cyan)
    d.rectangle([8, 10, 14, 30], fill=(230, 40, 40))
    d.rectangle([14, 10, 20, 30], fill=(40, 200, 40))
    d.rectangle([20, 10, 26, 30], fill=(40, 100, 240))
    d.rectangle([26, 10, 32, 30], fill=(255, 220, 0))
    d.rectangle([32, 10, 40, 30], fill=(0, 220, 230))
    # Stand
    d.rectangle([21, 34, 27, 40], fill=(180, 185, 195))
    d.rounded_rectangle([14, 40, 34, 44], radius=2, fill=(210, 215, 225), outline=(100, 110, 130), width=1)
    save_icon("ic_display", img)

if __name__ == "__main__":
    base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    target = os.path.join(base, "apps", "LunaLauncher", "res", "drawable")
    generate_applet_icons(target)
