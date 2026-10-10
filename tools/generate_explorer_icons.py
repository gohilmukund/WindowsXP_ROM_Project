#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

OUT_DIR = "apps/WindowsExplorer/res/drawable"
os.makedirs(OUT_DIR, exist_ok=True)

def draw_beveled_rect(draw, box, fill, highlight, shadow, width=1):
    x0, y0, x1, y1 = box
    draw.rectangle([x0, y0, x1, y1], fill=fill)
    for i in range(width):
        draw.line([(x0+i, y0+i), (x1-i, y0+i)], fill=highlight)
        draw.line([(x0+i, y0+i), (x0+i, y1-i)], fill=highlight)
        draw.line([(x1-i, y0+i), (x1-i, y1-i)], fill=shadow)
        draw.line([(x0+i, y1-i), (x1-i, y1-i)], fill=shadow)

def gen_icons():
    # 1. ic_my_computer.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # CRT Monitor
    draw_beveled_rect(d, [6, 4, 40, 32], (220, 220, 215), (255, 255, 255), (140, 140, 135), 2)
    # Screen Glass
    d.rectangle([10, 8, 36, 28], fill=(0, 85, 234))
    # Bliss desktop look on screen
    d.polygon([(10, 22), (22, 18), (36, 24), (36, 28), (10, 28)], fill=(106, 178, 54))
    # Monitor Stand
    d.polygon([(18, 32), (30, 32), (34, 38), (14, 38)], fill=(180, 180, 175))
    draw_beveled_rect(d, [12, 38, 36, 42], (210, 210, 205), (255, 255, 255), (120, 120, 115), 1)
    img.save(os.path.join(OUT_DIR, "ic_my_computer.png"), "PNG")

    # 2. ic_drive_c.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # HDD chassis
    draw_beveled_rect(d, [6, 10, 42, 38], (185, 190, 195), (255, 255, 255), (100, 105, 110), 2)
    # Top plate
    d.rectangle([10, 14, 38, 32], fill=(215, 220, 225))
    # HDD Platter slot
    d.ellipse([14, 16, 34, 30], outline=(150, 155, 160), fill=(230, 235, 240))
    # LED
    d.ellipse([34, 34, 38, 36], fill=(0, 230, 0))
    # Windows flag badge
    d.rectangle([10, 28, 18, 34], fill=(0, 85, 234))
    img.save(os.path.join(OUT_DIR, "ic_drive_c.png"), "PNG")

    # 3. ic_drive_d.png (48x48) - SD Card
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # SD shape (clipped corner top-right)
    d.polygon([(12, 8), (32, 8), (38, 14), (38, 40), (12, 40)], fill=(45, 60, 80))
    # Gold contacts
    for x in range(16, 32, 4):
        d.rectangle([x, 10, x+2, 16], fill=(240, 200, 40))
    # Label sticker
    d.rectangle([16, 22, 34, 36], fill=(220, 230, 245))
    d.text((18, 24), "SD", fill=(0, 0, 0))
    img.save(os.path.join(OUT_DIR, "ic_drive_d.png"), "PNG")

    # 4. ic_folder.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Back tab
    d.polygon([(6, 12), (20, 12), (24, 16), (40, 16), (40, 36), (6, 36)], fill=(225, 175, 45))
    # Front flap
    d.polygon([(4, 18), (42, 18), (38, 40), (4, 40)], fill=(255, 215, 75))
    d.line([(4, 18), (42, 18)], fill=(255, 245, 160), width=1)
    d.line([(4, 18), (4, 40)], fill=(255, 245, 160), width=1)
    d.line([(42, 18), (38, 40)], fill=(180, 130, 20), width=1)
    d.line([(4, 40), (38, 40)], fill=(180, 130, 20), width=1)
    img.save(os.path.join(OUT_DIR, "ic_folder.png"), "PNG")

    # 5. ic_folder_docs.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Back tab
    d.polygon([(6, 12), (20, 12), (24, 16), (40, 16), (40, 36), (6, 36)], fill=(225, 175, 45))
    # Paper inside
    d.rectangle([14, 8, 32, 26], fill=(255, 255, 255), outline=(180, 180, 180))
    d.line([(18, 12), (28, 12)], fill=(0, 85, 234), width=1)
    d.line([(18, 16), (28, 16)], fill=(100, 100, 100), width=1)
    d.line([(18, 20), (25, 20)], fill=(100, 100, 100), width=1)
    # Front flap
    d.polygon([(4, 18), (42, 18), (38, 40), (4, 40)], fill=(255, 215, 75))
    d.line([(4, 18), (42, 18)], fill=(255, 245, 160), width=1)
    img.save(os.path.join(OUT_DIR, "ic_folder_docs.png"), "PNG")

    # 6. ic_folder_pics.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(6, 12), (20, 12), (24, 16), (40, 16), (40, 36), (6, 36)], fill=(225, 175, 45))
    # Picture inside
    d.rectangle([16, 6, 36, 24], fill=(240, 240, 240), outline=(150, 150, 150))
    d.rectangle([18, 8, 34, 20], fill=(70, 140, 220))
    d.polygon([(18, 20), (24, 14), (28, 18), (34, 12), (34, 20)], fill=(80, 180, 70))
    d.ellipse([20, 10, 23, 13], fill=(250, 230, 40))
    # Front flap
    d.polygon([(4, 18), (42, 18), (38, 40), (4, 40)], fill=(255, 215, 75))
    d.line([(4, 18), (42, 18)], fill=(255, 245, 160), width=1)
    img.save(os.path.join(OUT_DIR, "ic_folder_pics.png"), "PNG")

    # 7. ic_folder_music.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(6, 12), (20, 12), (24, 16), (40, 16), (40, 36), (6, 36)], fill=(225, 175, 45))
    # Notes inside
    d.ellipse([18, 18, 24, 23], fill=(0, 85, 234))
    d.ellipse([28, 15, 34, 20], fill=(0, 85, 234))
    d.line([(24, 12), (24, 20)], fill=(0, 85, 234), width=2)
    d.line([(34, 9), (34, 17)], fill=(0, 85, 234), width=2)
    d.line([(24, 12), (34, 9)], fill=(0, 85, 234), width=2)
    # Front flap
    d.polygon([(4, 18), (42, 18), (38, 40), (4, 40)], fill=(255, 215, 75))
    d.line([(4, 18), (42, 18)], fill=(255, 245, 160), width=1)
    img.save(os.path.join(OUT_DIR, "ic_folder_music.png"), "PNG")

    # 8. ic_file_text.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Dog-eared page
    d.polygon([(10, 6), (30, 6), (38, 14), (38, 42), (10, 42)], fill=(255, 255, 255), outline=(160, 160, 160))
    d.polygon([(30, 6), (30, 14), (38, 14)], fill=(220, 220, 220), outline=(160, 160, 160))
    # Text lines
    for y in range(18, 38, 4):
        d.line([(15, y), (33, y)], fill=(80, 80, 140), width=1)
    img.save(os.path.join(OUT_DIR, "ic_file_text.png"), "PNG")

    # 9. ic_file_generic.png (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(10, 6), (30, 6), (38, 14), (38, 42), (10, 42)], fill=(255, 255, 255), outline=(160, 160, 160))
    d.polygon([(30, 6), (30, 14), (38, 14)], fill=(220, 220, 220), outline=(160, 160, 160))
    d.rectangle([16, 20, 32, 32], outline=(0, 85, 234), fill=(240, 245, 255))
    img.save(os.path.join(OUT_DIR, "ic_file_generic.png"), "PNG")

    # 10. Toolbar Icons (24x24)
    # tb_back.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 21, 21], fill=(85, 175, 45), outline=(50, 120, 25))
    d.polygon([(8, 12), (14, 7), (14, 10), (18, 10), (18, 14), (14, 14), (14, 17)], fill=(255, 255, 255))
    img.save(os.path.join(OUT_DIR, "tb_back.png"), "PNG")

    # tb_forward.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 21, 21], fill=(85, 175, 45), outline=(50, 120, 25))
    d.polygon([(16, 12), (10, 7), (10, 10), (6, 10), (6, 14), (10, 14), (10, 17)], fill=(255, 255, 255))
    img.save(os.path.join(OUT_DIR, "tb_forward.png"), "PNG")

    # tb_up.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 6), (9, 6), (11, 8), (20, 8), (20, 20), (2, 20)], fill=(240, 190, 50))
    d.polygon([(12, 6), (7, 12), (10, 12), (10, 17), (14, 17), (14, 12), (17, 12)], fill=(85, 175, 45))
    img.save(os.path.join(OUT_DIR, "tb_up.png"), "PNG")

    # tb_search.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, 14, 14], outline=(0, 85, 234), fill=(225, 240, 255), width=2)
    d.line([(12, 12), (20, 20)], fill=(0, 60, 160), width=3)
    img.save(os.path.join(OUT_DIR, "tb_search.png"), "PNG")

    # tb_folders.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 4), (8, 4), (10, 6), (18, 6), (18, 16), (2, 16)], fill=(225, 175, 45))
    d.polygon([(6, 8), (12, 8), (14, 10), (22, 10), (22, 20), (6, 20)], fill=(255, 215, 75))
    img.save(os.path.join(OUT_DIR, "tb_folders.png"), "PNG")

    # tb_views.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 21, 19], outline=(80, 80, 80), fill=(255, 255, 255))
    d.rectangle([4, 6, 9, 10], fill=(0, 85, 234))
    d.rectangle([12, 6, 19, 10], fill=(200, 200, 200))
    d.rectangle([4, 12, 9, 16], fill=(0, 85, 234))
    d.rectangle([12, 12, 19, 16], fill=(200, 200, 200))
    img.save(os.path.join(OUT_DIR, "tb_views.png"), "PNG")

    # tb_delete.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(5, 5), (19, 19)], fill=(220, 20, 20), width=3)
    d.line([(19, 5), (5, 19)], fill=(220, 20, 20), width=3)
    img.save(os.path.join(OUT_DIR, "tb_delete.png"), "PNG")

    # tb_newfolder.png
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(2, 6), (9, 6), (11, 8), (20, 8), (20, 20), (2, 20)], fill=(240, 190, 50))
    d.line([(16, 12), (16, 20)], fill=(85, 175, 45), width=2)
    d.line([(12, 16), (20, 16)], fill=(85, 175, 45), width=2)
    img.save(os.path.join(OUT_DIR, "tb_newfolder.png"), "PNG")

    # tb_go.png
    img = Image.new("RGBA", (20, 20), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 1, 18, 18], fill=(85, 175, 45), outline=(50, 120, 25))
    d.polygon([(5, 4), (15, 10), (5, 16)], fill=(255, 255, 255))
    img.save(os.path.join(OUT_DIR, "tb_go.png"), "PNG")

    # 11. Window controls (21x21)
    # btn_min.png
    img = Image.new("RGBA", (21, 21), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_beveled_rect(d, [0, 0, 20, 20], (0, 85, 234), (100, 180, 255), (0, 40, 140), 1)
    d.line([(4, 15), (16, 15)], fill=(255, 255, 255), width=2)
    img.save(os.path.join(OUT_DIR, "btn_min.png"), "PNG")

    # btn_max.png
    img = Image.new("RGBA", (21, 21), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_beveled_rect(d, [0, 0, 20, 20], (0, 85, 234), (100, 180, 255), (0, 40, 140), 1)
    d.rectangle([4, 4, 16, 16], outline=(255, 255, 255), width=2)
    img.save(os.path.join(OUT_DIR, "btn_max.png"), "PNG")

    # btn_close.png
    img = Image.new("RGBA", (21, 21), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_beveled_rect(d, [0, 0, 20, 20], (220, 40, 30), (255, 130, 120), (140, 15, 10), 1)
    d.line([(5, 5), (15, 15)], fill=(255, 255, 255), width=2)
    d.line([(15, 5), (5, 15)], fill=(255, 255, 255), width=2)
    img.save(os.path.join(OUT_DIR, "btn_close.png"), "PNG")

    print(f"Explorer icons successfully generated in {OUT_DIR}")

if __name__ == "__main__":
    gen_icons()
