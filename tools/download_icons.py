import urllib.request
import os

def download_resolved(rel_path, out_path):
    base = "https://raw.githubusercontent.com/B00merang-Artwork/Windows-XP/master/"
    url = base + rel_path.replace("+", "%2B")
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    data = urllib.request.urlopen(req).read()
    if data.startswith(b"\x89PNG\r\n\x1a\n"):
        with open(out_path, "wb") as f:
            f.write(data)
        print("Downloaded " + rel_path + " -> " + out_path + " (" + str(len(data)) + " bytes)")
    else:
        target = data.decode("utf-8").strip()
        dir_part = "/".join(rel_path.split("/")[:-1])
        new_path = (dir_part + "/" + target) if dir_part else target
        print(rel_path + " -> symlink to " + target + " -> downloading " + new_path)
        download_resolved(new_path, out_path)

os.makedirs("/tmp/icons_test", exist_ok=True)
test_files = [
    ("32x32/actions/zoom-in.png", "/tmp/icons_test/pv_zoom_in.png"),
    ("32x32/actions/zoom-out.png", "/tmp/icons_test/pv_zoom_out.png"),
    ("32x32/actions/zoom-fit-best.png", "/tmp/icons_test/pv_zoom_fit.png"),
    ("32x32/actions/view-presentation.png", "/tmp/icons_test/pv_slideshow.png"),
    ("32x32/actions/edit-undo.png", "/tmp/icons_test/pv_rotate_left.png"),
    ("32x32/actions/edit-redo.png", "/tmp/icons_test/pv_rotate_right.png"),
    ("32x32/actions/go-previous.png", "/tmp/icons_test/pv_prev.png"),
    ("32x32/actions/go-next.png", "/tmp/icons_test/pv_next.png"),
    ("32x32/places/folder-open.png", "/tmp/icons_test/pv_open.png"),
]

for rel, out in test_files:
    try:
        download_resolved(rel, out)
    except Exception as e:
        print("Failed for " + rel + ": " + str(e))
