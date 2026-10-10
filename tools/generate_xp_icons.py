import os
from PIL import Image, ImageDraw

def generate_icons(target_dir):
    os.makedirs(target_dir, exist_ok=True)

    def save_icon(name, img):
        img.save(os.path.join(target_dir, f"{name}.png"), "PNG")

    # 1. ic_win_flag (32x32)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # 4 squares
    d.polygon([(6, 6), (14, 5), (14, 13), (6, 14)], fill=(235, 60, 0))       # Red
    d.polygon([(16, 5), (25, 4), (25, 12), (16, 13)], fill=(120, 186, 0))     # Green
    d.polygon([(6, 16), (14, 15), (14, 23), (6, 24)], fill=(0, 164, 239))     # Blue
    d.polygon([(16, 15), (25, 14), (25, 22), (16, 23)], fill=(255, 185, 0))   # Yellow
    save_icon("ic_win_flag", img)

    # 2. ic_my_computer (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Monitor bezel
    d.rounded_rectangle([6, 6, 42, 34], radius=3, fill=(230, 230, 235), outline=(120, 130, 150), width=2)
    # Screen glass (cyan/blue XP desktop)
    d.rectangle([10, 10, 38, 30], fill=(0, 120, 215))
    d.polygon([(10, 25), (38, 20), (38, 30), (10, 30)], fill=(70, 160, 20)) # Bliss hill in screen
    # Stand
    d.rectangle([21, 34, 27, 39], fill=(180, 185, 195))
    d.rounded_rectangle([15, 39, 33, 43], radius=2, fill=(210, 215, 225), outline=(100, 110, 130), width=1)
    save_icon("ic_my_computer", img)

    # 3. ic_my_documents (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Back folder tab
    d.polygon([(6, 14), (18, 14), (23, 19), (42, 19), (42, 40), (6, 40)], fill=(210, 160, 40))
    # Paper inside
    d.rectangle([14, 12, 36, 32], fill=(255, 255, 255), outline=(180, 180, 180), width=1)
    d.line([(18, 16), (32, 16)], fill=(0, 120, 215), width=2)
    d.line([(18, 20), (30, 20)], fill=(150, 150, 150), width=1)
    d.line([(18, 24), (28, 24)], fill=(150, 150, 150), width=1)
    # Front folder flap
    d.polygon([(4, 21), (44, 21), (39, 42), (9, 42)], fill=(245, 195, 65), outline=(190, 140, 20), width=1)
    save_icon("ic_my_documents", img)

    # 4. ic_internet_explorer (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Gold orbital ring behind
    d.arc([4, 6, 44, 42], start=120, end=330, fill=(250, 200, 50), width=5)
    # Classic Blue 'e'
    d.ellipse([10, 8, 38, 38], fill=(0, 122, 237))
    d.ellipse([17, 15, 31, 31], fill=(0, 0, 0, 0)) # hollow
    # Center crossbar
    d.rectangle([10, 21, 38, 27], fill=(0, 122, 237))
    # Cutout bottom right of 'e'
    d.rectangle([25, 27, 40, 36], fill=(0, 0, 0, 0))
    # Gold orbital ring front swoop
    d.arc([4, 6, 44, 42], start=300, end=70, fill=(255, 215, 60), width=5)
    save_icon("ic_internet_explorer", img)

    # 5. ic_wmp (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Brushed metallic circle
    d.ellipse([6, 6, 42, 42], fill=(210, 220, 235), outline=(80, 110, 160), width=2)
    d.ellipse([11, 11, 37, 37], fill=(15, 35, 75)) # Dark navy inner core
    # Orange and blue play triangle
    d.polygon([(18, 15), (34, 24), (18, 33)], fill=(245, 130, 20))
    d.polygon([(18, 15), (26, 24), (18, 24)], fill=(0, 160, 240))
    save_icon("ic_wmp", img)

    # 6. ic_paint (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Artist palette
    d.ellipse([6, 12, 40, 42], fill=(225, 190, 135), outline=(140, 95, 45), width=2)
    # Thumb hole
    d.ellipse([12, 26, 18, 32], fill=(0, 0, 0, 0))
    # Paint dabs
    d.ellipse([15, 16, 21, 22], fill=(230, 40, 30))   # Red
    d.ellipse([24, 15, 30, 21], fill=(40, 175, 40))   # Green
    d.ellipse([32, 21, 38, 27], fill=(30, 110, 230))  # Blue
    d.ellipse([28, 30, 34, 36], fill=(245, 210, 30))  # Yellow
    # Brush
    d.line([(24, 8), (42, 38)], fill=(120, 60, 20), width=4)
    d.line([(21, 5), (24, 8)], fill=(180, 180, 190), width=5)
    d.line([(18, 2), (21, 5)], fill=(30, 30, 30), width=4)
    save_icon("ic_paint", img)

    # 7. ic_notepad (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Notepad cover/body
    d.rounded_rectangle([10, 6, 38, 42], radius=3, fill=(240, 245, 255), outline=(130, 150, 185), width=2)
    # Blue top binding
    d.rounded_rectangle([10, 6, 38, 14], radius=2, fill=(40, 95, 200))
    # Text lines
    for ly in [18, 23, 28, 33, 38]:
        d.line([(15, ly), (33, ly)], fill=(120, 140, 180), width=1)
    # Pencil across notepad
    d.line([(28, 38), (42, 18)], fill=(245, 180, 30), width=4)
    d.polygon([(26, 40), (28, 38), (28, 41)], fill=(220, 170, 140)) # tip
    d.point((26, 41), fill=(30, 30, 30)) # lead
    save_icon("ic_notepad", img)

    # 8. ic_games (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Joystick base
    d.rounded_rectangle([10, 26, 38, 42], radius=4, fill=(180, 185, 195), outline=(90, 100, 115), width=2)
    # Red buttons
    d.ellipse([14, 31, 20, 37], fill=(225, 40, 35))
    d.ellipse([28, 31, 34, 37], fill=(30, 120, 235))
    # Stick
    d.line([(24, 26), (24, 12)], fill=(80, 85, 95), width=4)
    d.ellipse([19, 7, 29, 17], fill=(235, 30, 30)) # Red ball knob
    save_icon("ic_games", img)

    # 9. ic_recycle_bin (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Basket
    d.polygon([(12, 14), (36, 14), (32, 42), (16, 42)], fill=(210, 225, 245), outline=(100, 130, 170), width=2)
    # Rim
    d.ellipse([10, 11, 38, 17], fill=(180, 205, 235), outline=(90, 120, 160), width=2)
    # Green recycle arrows
    d.arc([19, 23, 29, 33], start=0, end=270, fill=(40, 160, 40), width=2)
    save_icon("ic_recycle_bin", img)

    # 10. ic_fotoo (48x48)
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Frame
    d.rounded_rectangle([8, 8, 40, 40], radius=3, fill=(60, 40, 20), outline=(220, 180, 80), width=2)
    # Photo canvas
    d.rectangle([12, 12, 36, 36], fill=(135, 206, 235)) # Sky
    d.polygon([(12, 30), (22, 22), (28, 27), (36, 19), (36, 36), (12, 36)], fill=(34, 139, 34)) # Hills
    d.ellipse([27, 15, 33, 21], fill=(255, 215, 0)) # Sun
    save_icon("ic_fotoo", img)

    # 11. ic_control_panel (32x32)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([4, 4, 28, 28], radius=3, fill=(225, 235, 250), outline=(90, 120, 175), width=2)
    d.line([(8, 10), (24, 10)], fill=(80, 100, 140), width=2)
    d.rectangle([11, 8, 15, 12], fill=(220, 50, 30))
    d.line([(8, 16), (24, 16)], fill=(80, 100, 140), width=2)
    d.rectangle([18, 14, 22, 18], fill=(30, 130, 220))
    d.line([(8, 22), (24, 22)], fill=(80, 100, 140), width=2)
    d.rectangle([13, 20, 17, 24], fill=(40, 170, 50))
    save_icon("ic_control_panel", img)

    # 12. ic_shutdown (32x32)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, 28, 28], fill=(220, 45, 30), outline=(150, 20, 10), width=1)
    d.arc([8, 8, 24, 24], start=300, end=240, fill=(255, 255, 255), width=3)
    d.line([(16, 7), (16, 16)], fill=(255, 255, 255), width=3)
    save_icon("ic_shutdown", img)

    # 13. ic_logoff (32x32)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 4, 28, 28], fill=(245, 180, 30), outline=(170, 115, 10), width=1)
    # Key shape
    d.ellipse([8, 12, 16, 20], outline=(255, 255, 255), width=2)
    d.line([(15, 16), (25, 16)], fill=(255, 255, 255), width=2)
    d.line([(22, 16), (22, 20)], fill=(255, 255, 255), width=2)
    save_icon("ic_logoff", img)

    print("All Windows XP Luna icons generated successfully in:", target_dir)

if __name__ == "__main__":
    generate_icons("/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable")
