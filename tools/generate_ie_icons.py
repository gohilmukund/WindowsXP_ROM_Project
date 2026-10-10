import os
from PIL import Image, ImageDraw

def generate_ie_icons(target_dir):
    os.makedirs(target_dir, exist_ok=True)

    def save(name, img):
        img.save(os.path.join(target_dir, f"{name}.png"), "PNG")

    # 1. Back button (green arrow pointing left)
    img = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 26, 26], fill=(50, 160, 50), outline=(20, 100, 20), width=1)
    d.polygon([(8, 14), (16, 8), (16, 12), (22, 12), (22, 16), (16, 16), (16, 20)], fill=(255, 255, 255))
    save("ic_ie_back", img)

    # 2. Forward button (green arrow pointing right)
    img = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 26, 26], fill=(50, 160, 50), outline=(20, 100, 20), width=1)
    d.polygon([(20, 14), (12, 8), (12, 12), (6, 12), (6, 16), (12, 16), (12, 20)], fill=(255, 255, 255))
    save("ic_ie_forward", img)

    # 3. Stop button (Red X)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(6, 6), (18, 18)], fill=(220, 40, 30), width=3)
    d.line([(18, 6), (6, 18)], fill=(220, 40, 30), width=3)
    save("ic_ie_stop", img)

    # 4. Refresh button (circular reload arrows)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.arc([4, 4, 20, 20], start=30, end=300, fill=(40, 150, 40), width=3)
    d.polygon([(18, 4), (14, 0), (14, 8)], fill=(40, 150, 40))
    save("ic_ie_refresh", img)

    # 5. Home button (House)
    img = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(12, 3), (3, 11), (21, 11)], fill=(180, 50, 30))
    d.rectangle([6, 11, 18, 21], fill=(220, 180, 120), outline=(130, 90, 40), width=1)
    d.rectangle([10, 14, 14, 21], fill=(120, 60, 20))
    save("ic_ie_home", img)

    # 6. Spinning globe logo (28x28)
    img = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 3, 25, 25], fill=(30, 100, 200), outline=(10, 40, 120), width=1)
    # Latitude & longitude lines
    d.arc([5, 3, 23, 25], start=0, end=360, fill=(100, 180, 255), width=1)
    d.line([(3, 14), (25, 14)], fill=(100, 180, 255), width=1)
    d.arc([3, 8, 25, 20], start=0, end=360, fill=(100, 180, 255), width=1)
    save("ic_ie_globe", img)

    # 7. Green Go button (Right arrow in pill)
    img = Image.new("RGBA", (22, 22), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([1, 1, 21, 21], radius=3, fill=(40, 160, 40), outline=(20, 90, 20), width=1)
    d.polygon([(15, 11), (8, 6), (8, 16)], fill=(255, 255, 255))
    save("ic_ie_go", img)

    print("Internet Explorer icons generated in:", target_dir)

if __name__ == "__main__":
    generate_ie_icons("/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/InternetExplorer/res/drawable")
