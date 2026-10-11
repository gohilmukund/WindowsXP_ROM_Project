#!/usr/bin/env python3
import os
import shutil
import subprocess
from PIL import Image, ImageDraw, ImageFont

def generate():
    out_dir = "/tmp/bootanim_build"
    part0_dir = os.path.join(out_dir, "part0")
    if os.path.exists(out_dir):
        shutil.rmtree(out_dir)
    os.makedirs(part0_dir, exist_ok=True)

    w, h = 1024, 600
    total_frames = 36

    # Track coordinates
    track_w, track_h = 280, 20
    track_x = (w - track_w) // 2
    track_y = 390

    # 3-segment marquee dimensions
    seg_w = 16
    seg_h = 12
    seg_gap = 5
    block_total_w = 3 * seg_w + 2 * seg_gap  # 16*3 + 10 = 58
    travel_dist = track_w + block_total_w

    for i in range(total_frames):
        img = Image.new("RGB", (w, h), (0, 0, 0))
        draw = ImageDraw.Draw(img)

        # Draw Windows XP Logo Emblem
        logo_cx, logo_cy = w // 2, 230

        # Draw 4 flying color tiles
        tile_size = 28
        offset = 18
        # Red (Top-Left)
        draw.polygon([(logo_cx - offset - tile_size, logo_cy - offset - 4),
                      (logo_cx - offset, logo_cy - offset - 8),
                      (logo_cx - offset + 2, logo_cy - 4),
                      (logo_cx - offset - tile_size + 2, logo_cy - 2)], fill=(235, 60, 0))
        # Green (Top-Right)
        draw.polygon([(logo_cx + offset - tile_size + 6, logo_cy - offset - 8),
                      (logo_cx + offset + 6, logo_cy - offset - 4),
                      (logo_cx + offset + 8, logo_cy - 2),
                      (logo_cx + offset - tile_size + 8, logo_cy - 4)], fill=(120, 186, 0))
        # Blue (Bottom-Left)
        draw.polygon([(logo_cx - offset - tile_size + 2, logo_cy + 2),
                      (logo_cx - offset + 2, logo_cy),
                      (logo_cx - offset, logo_cy + offset + 6),
                      (logo_cx - offset - tile_size, logo_cy + offset + 2)], fill=(0, 164, 239))
        # Yellow (Bottom-Right)
        draw.polygon([(logo_cx + offset - tile_size + 8, logo_cy),
                      (logo_cx + offset + 8, logo_cy + 2),
                      (logo_cx + offset + 6, logo_cy + offset + 2),
                      (logo_cx + offset - tile_size + 6, logo_cy + offset + 6)], fill=(255, 185, 0))

        # Text labels
        try:
            font_micro = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 16)
            font_win = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 36)
            font_xp = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 44)
        except Exception:
            font_micro = font_win = font_xp = ImageFont.load_default()

        draw.text((logo_cx - 105, logo_cy + 36), "Microsoft", fill=(180, 180, 180), font=font_micro)
        draw.text((logo_cx - 118, logo_cy + 56), "Windows", fill=(255, 255, 255), font=font_win)
        draw.text((logo_cx + 80, logo_cy + 50), "MK", fill=(215, 60, 15), font=font_xp)
        draw.text((logo_cx + 162, logo_cy + 52), "®", fill=(215, 60, 15), font=font_micro)

        # Draw rounded progress track
        draw.rounded_rectangle([track_x, track_y, track_x + track_w, track_y + track_h], radius=4, fill=(4, 12, 35), outline=(30, 60, 120), width=2)

        # Position of 3-segment marquee
        t = i / total_frames
        head_x = track_x - block_total_w + int(t * travel_dist)

        for s in range(3):
            sx = head_x + s * (seg_w + seg_gap)
            sy = track_y + (track_h - seg_h) // 2
            # Clip inside track
            if sx + seg_w > track_x and sx < track_x + track_w:
                x1 = max(sx, track_x + 2)
                x2 = min(sx + seg_w, track_x + track_w - 2)
                if x2 > x1:
                    # Segment gradient look (cyan top highlight, royal blue base)
                    draw.rectangle([x1, sy, x2, sy + 3], fill=(120, 210, 255))
                    draw.rectangle([x1, sy + 3, x2, sy + seg_h], fill=(0, 100, 240))

        frame_path = os.path.join(part0_dir, f"{i:04d}.png")
        img.save(frame_path, "PNG")

    # Write desc.txt
    desc_path = os.path.join(out_dir, "desc.txt")
    with open(desc_path, "w") as f:
        f.write("1024 600 30\np 0 0 part0\n")

    # Zip with compression 0
    final_zip = "/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/assets/bootanimation/bootanimation.zip"
    os.makedirs(os.path.dirname(final_zip), exist_ok=True)
    if os.path.exists(final_zip):
        os.remove(final_zip)

    cmd = ["zip", "-0", "-r", final_zip, "desc.txt", "part0"]
    subprocess.check_call(cmd, cwd=out_dir)
    print(f"Generated {final_zip} successfully!")

if __name__ == "__main__":
    generate()
