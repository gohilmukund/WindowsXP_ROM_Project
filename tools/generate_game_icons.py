import os
from PIL import Image, ImageDraw

def generate_game_icons():
    # 1. Minesweeper icon
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Beveled cell
    d.rectangle([2, 2, 45, 45], fill=(192, 192, 192))
    d.line([(2, 2), (45, 2)], fill=(255, 255, 255), width=3)
    d.line([(2, 2), (2, 45)], fill=(255, 255, 255), width=3)
    d.line([(45, 2), (45, 45)], fill=(128, 128, 128), width=3)
    d.line([(2, 45), (45, 45)], fill=(128, 128, 128), width=3)
    # Mine body
    cx, cy = 24, 24
    d.ellipse([14, 14, 34, 34], fill=(0, 0, 0))
    # Spikes
    d.line([(24, 7), (24, 41)], fill=(0, 0, 0), width=3)
    d.line([(7, 24), (41, 24)], fill=(0, 0, 0), width=3)
    d.line([(12, 12), (36, 36)], fill=(0, 0, 0), width=3)
    d.line([(36, 12), (12, 36)], fill=(0, 0, 0), width=3)
    # Highlight
    d.rectangle([18, 18, 21, 21], fill=(255, 255, 255))
    os.makedirs("apps/LunaLauncher/res/drawable", exist_ok=True)
    img.save("apps/LunaLauncher/res/drawable/ic_minesweeper.png", "PNG")

    # 2. Solitaire icon
    img_s = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    ds = ImageDraw.Draw(img_s)
    # Card 1 (behind)
    ds.rectangle([6, 8, 36, 44], fill=(0, 0, 160), outline=(255, 255, 255), width=2)
    # Card 2 (front)
    ds.rectangle([12, 4, 42, 40], fill=(255, 255, 255), outline=(0, 0, 0), width=2)
    # Red Heart
    ds.polygon([(27, 26), (20, 18), (22, 14), (27, 18), (32, 14), (34, 18)], fill=(220, 20, 20))
    # 'A'
    ds.text((15, 6), "A", fill=(220, 20, 20))
    img_s.save("apps/LunaLauncher/res/drawable/ic_solitaire.png", "PNG")
    print("Game icons generated.")

if __name__ == "__main__":
    generate_game_icons()
