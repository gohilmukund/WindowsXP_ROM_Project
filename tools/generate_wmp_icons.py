import os
from PIL import Image, ImageDraw

def generate_wmp_icons(target_dir):
    os.makedirs(target_dir, exist_ok=True)

    def save(name, img):
        img.save(os.path.join(target_dir, f"{name}.png"), "PNG")

    # Play (triangle)
    img = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 5), (22, 14), (8, 23)], fill=(40, 160, 40), outline=(20, 90, 20))
    save("ic_play", img)

    # Pause (two vertical bars)
    img = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([8, 6, 12, 22], fill=(40, 160, 40), outline=(20, 90, 20))
    d.rectangle([16, 6, 20, 22], fill=(40, 160, 40), outline=(20, 90, 20))
    save("ic_pause", img)

    # Stop (square)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([5, 5, 19, 19], fill=(220, 50, 40), outline=(140, 20, 10))
    save("ic_stop", img)

    # Next (>>|)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(4, 5), (12, 12), (4, 19)], fill=(40, 90, 180))
    d.polygon([(11, 5), (19, 12), (11, 19)], fill=(40, 90, 180))
    d.line([(19, 5), (19, 19)], fill=(40, 90, 180), width=2)
    save("ic_next", img)

    # Prev (|<<)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(5, 5), (5, 19)], fill=(40, 90, 180), width=2)
    d.polygon([(13, 12), (5, 5), (13, 19)], fill=(40, 90, 180))
    d.polygon([(20, 12), (12, 5), (20, 19)], fill=(40, 90, 180))
    save("ic_prev", img)

    print("WMP icons generated in:", target_dir)

if __name__ == "__main__":
    generate_wmp_icons("/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/WindowsMediaPlayer/res/drawable")
