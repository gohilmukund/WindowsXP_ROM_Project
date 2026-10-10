import os
from PIL import Image, ImageDraw

def generate_paint_icons(target_dir):
    os.makedirs(target_dir, exist_ok=True)

    def save(name, img):
        img.save(os.path.join(target_dir, f"{name}.png"), "PNG")

    # 1. Pencil
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(5, 18), (17, 6)], fill=(240, 180, 20), width=4)
    d.polygon([(3, 20), (5, 18), (6, 21)], fill=(210, 160, 120))
    d.point((3, 21), fill=(0, 0, 0))
    save("tool_pencil", img)

    # 2. Brush
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(8, 6), (18, 16)], fill=(120, 60, 20), width=4)
    d.line([(6, 4), (8, 6)], fill=(180, 180, 180), width=5)
    d.polygon([(4, 2), (6, 4), (3, 5)], fill=(30, 30, 30))
    save("tool_brush", img)

    # 3. Eraser
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(6, 14), (12, 8), (18, 14), (12, 20)], fill=(255, 192, 203), outline=(180, 120, 140))
    d.polygon([(12, 14), (18, 8), (21, 11), (15, 17)], fill=(230, 230, 240), outline=(150, 150, 160))
    save("tool_eraser", img)

    # 4. Fill Bucket
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(6, 10), (14, 4), (18, 12), (10, 18)], fill=(180, 180, 190), outline=(80, 80, 90))
    d.polygon([(8, 16), (6, 22), (10, 22)], fill=(220, 40, 30)) # Spilling red paint
    save("tool_bucket", img)

    # 5. Line
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(4, 20), (20, 4)], fill=(0, 0, 0), width=3)
    save("tool_line", img)

    # 6. Rectangle
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 6, 20, 18], outline=(0, 0, 0), width=2)
    save("tool_rect", img)

    # 7. Ellipse
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 5, 20, 19], outline=(0, 0, 0), width=2)
    save("tool_circle", img)

    print("MSPaint tool icons generated in:", target_dir)

if __name__ == "__main__":
    generate_paint_icons("/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/MSPaint/res/drawable")
