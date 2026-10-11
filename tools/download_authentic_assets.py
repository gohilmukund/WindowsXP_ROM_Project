#!/usr/bin/env python3
import urllib.request
import io
import os
import sys
from PIL import Image

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'apps', 'LunaLauncher', 'res', 'drawable')
os.makedirs(OUT_DIR, exist_ok=True)

def fetch_image(url):
    print(f"Fetching: {url}")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = resp.read()
            return Image.open(io.BytesIO(data))
    except Exception as e:
        print(f"Error fetching {url}: {e}")
        return None

def save_image(img, filename, resize=None):
    path = os.path.join(OUT_DIR, filename)
    if os.path.exists(path) and os.path.getsize(path) > 0:
        print(f"Already exists: {filename}")
        return
    if img is None:
        print(f"Skipping save for {filename} (img is None)")
        return
    if resize:
        img = img.resize(resize, Image.Resampling.LANCZOS)
    img.save(path, format='PNG')
    print(f"Saved: {filename} ({img.size})")

def main():
    print("=== Downloading Authentic Windows Assets ===")

    # 1. Start Buttons
    # Luna Blue Start Button
    img_blue = fetch_image('https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/start.png')
    if img_blue:
        save_image(img_blue, 'start_button_luna_blue.png')

    # RetroBar Olive Start Button (sprite sheet 99x99 -> 3 states: 99x33)
    img_green_sheet = fetch_image('https://raw.githubusercontent.com/dremin/RetroBar/master/RetroBar/Resources/startxpgreenbutton.png')
    if img_green_sheet:
        img_olive = img_green_sheet.crop((0, 0, 99, 33))
        save_image(img_olive, 'start_button_luna_olive.png')

    # RetroBar Silver Start Button (sprite sheet 99x99 -> 3 states: 99x33)
    img_silver_sheet = fetch_image('https://raw.githubusercontent.com/dremin/RetroBar/master/RetroBar/Resources/startxpsilverbutton.png')
    if img_silver_sheet:
        img_silver = img_silver_sheet.crop((0, 0, 99, 33))
        save_image(img_silver, 'start_button_luna_silver.png')

    # Windows Classic Start Button (from OpenShell btn_classic.bmp or create authentic bevel with 98 logo)
    img_classic = fetch_image('https://raw.githubusercontent.com/Open-Shell/Open-Shell-Menu/master/Src/StartMenu/StartMenuDLL/btn_classic.bmp')
    if img_classic:
        # bmp might have 3 states vertically
        w, h = img_classic.size
        single_h = h // 3 if h > 30 else h
        img_classic_cut = img_classic.crop((0, 0, w, single_h))
        save_image(img_classic_cut, 'start_button_classic.png')

    # Vista Start Orb (from RetroBar startvistabuttonlg.png, top state 60x46)
    img_vista_sheet = fetch_image('https://raw.githubusercontent.com/dremin/RetroBar/master/RetroBar/Resources/startvistabuttonlg.png')
    if img_vista_sheet:
        img_vista = img_vista_sheet.crop((0, 0, 60, 46))
        save_image(img_vista, 'start_button_vista.png')

    # Windows 7 Start Orb (from OpenShell btn_aero.ico or style_7.png)
    img_win7 = fetch_image('https://raw.githubusercontent.com/Open-Shell/Open-Shell-Menu/master/Src/StartMenu/StartMenuDLL/style_7.png')
    if img_win7:
        save_image(img_win7, 'start_button_win7.png', resize=(54, 54))
    else:
        # Fallback to Vista orb if win7 style_7 is not available
        if img_vista_sheet:
            save_image(img_vista, 'start_button_win7.png')

    # 2. Explorer & Browser Toolbar Buttons
    tb_icons = {
        'tb_back.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/back.png',
        'tb_forward.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/forward.png',
        'tb_up.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/up.png',
        'tb_search.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Search.png',
        'tb_folders.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/FolderView-CommonTasks.png',
        'tb_views.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/DetailView.png',
        'tb_refresh.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/refresh.png',
        'tb_stop.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/stop.png',
        'tb_home.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/home.png',
        'tb_go.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Go.png',
        'tb_delete.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Delete.png',
        'tb_newfolder.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/NewFolder.png',
    }
    for fname, url in tb_icons.items():
        if os.path.exists(os.path.join(OUT_DIR, fname)) and os.path.getsize(os.path.join(OUT_DIR, fname)) > 0:
            continue
        img = fetch_image(url)
        save_image(img, fname)

    # 3. Authentic Shutdown Dialog Assets
    shutdown_assets = {
        'ic_turn_off_header_flag.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/windows-off.png',
        'btn_standby.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Standby.png',
        'btn_turn_off.png': 'https://raw.githubusercontent.com/ShizukuIchi/winXP/master/src/assets/windowsIcons/310(32x32).png',
        'btn_restart.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Restart.png',
    }
    for fname, url in shutdown_assets.items():
        if os.path.exists(os.path.join(OUT_DIR, fname)) and os.path.getsize(os.path.join(OUT_DIR, fname)) > 0:
            continue
        img = fetch_image(url)
        save_image(img, fname)

    # 4. Core System & App Icons
    app_icons = {
        'ic_my_computer.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/MyComputer.png',
        'ic_drive_c.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/LocalDisk.png',
        'ic_my_documents.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/MyDocuments.png',
        'ic_folder.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/FolderClosed.png',
        'ic_folder_open.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/FolderOpened.png',
        'ic_recycle_bin.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/RecycleBin(empty).png',
        'ic_recycle_bin_full.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/RecycleBin(full).png',
        'ic_control_panel.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/ControlPanel.png',
        'ic_network.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/NetworkConnections.png',
        'ic_system_properties.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/SystemProperties.png',
        'ic_display.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/DisplayProperties.png',
        'ic_ie.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/InternetExplorer6.png',
        'ic_internet_explorer.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/InternetExplorer6.png',
        'ic_wmp.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/WindowsMediaPlayer9.png',
        'ic_picture_viewer.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/WindowsPictureandFaxViewer.png',
        'ic_paint.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Paint.png',
        'ic_notepad.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Notepad.png',
        'ic_solitaire.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Solitaire.png',
        'ic_minesweeper.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Minesweeper.png',
        'ic_pinball.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Pinball.png',
        'ic_help.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/HelpandSupport.png',
        'ic_run.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/Run.png',
        'ic_cmd.png': 'https://raw.githubusercontent.com/softwarehistorysociety/XPIcons/master/XP/TourXP.png', # CMD / DOS prompt
    }
    for fname, url in app_icons.items():
        if os.path.exists(os.path.join(OUT_DIR, fname)) and os.path.getsize(os.path.join(OUT_DIR, fname)) > 0:
            continue
        img = fetch_image(url)
        save_image(img, fname)

    # 5. Classic Theme Icons (Windows 98 authentic icons)
    classic_icons = {
        'ic_classic_my_computer.png': 'https://raw.githubusercontent.com/zhaotianff/Win98-Icons/master/icons/computer-0.png',
        'ic_classic_drive_c.png': 'https://raw.githubusercontent.com/zhaotianff/Win98-Icons/master/icons/hard_disk_drive-0.png',
        'ic_classic_folder.png': 'https://raw.githubusercontent.com/zhaotianff/Win98-Icons/master/icons/directory_folder_options-0.png',
        'ic_classic_recycle_bin.png': 'https://raw.githubusercontent.com/zhaotianff/Win98-Icons/master/icons/recycle_bin_empty-0.png',
    }
    for fname, url in classic_icons.items():
        if os.path.exists(os.path.join(OUT_DIR, fname)) and os.path.getsize(os.path.join(OUT_DIR, fname)) > 0:
            continue
        img = fetch_image(url)
        save_image(img, fname)

    # 6. Windows Vista & Windows 7 Aero Icons (from bartekl1/windows-ui-assets)
    aero_icos = {
        'ic_win7_my_computer.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%207/ico/shell32.dll/ICON16_1.ico',
        'ic_win7_drive_c.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%207/ico/shell32.dll/ICON9_1.ico',
        'ic_win7_folder.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%207/ico/shell32.dll/ICON4_1.ico',
        'ic_win7_recycle_bin.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%207/ico/shell32.dll/ICON32_1.ico',
        'ic_vista_my_computer.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%20Vista/ico/shell32.dll/ICON16_1.ico',
        'ic_vista_drive_c.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%20Vista/ico/shell32.dll/ICON9_1.ico',
        'ic_vista_folder.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%20Vista/ico/shell32.dll/ICON4_1.ico',
        'ic_vista_recycle_bin.png': 'https://raw.githubusercontent.com/bartekl1/windows-ui-assets/master/Icons/Windows%20Vista/ico/shell32.dll/ICON32_1.ico',
    }
    for fname, url in aero_icos.items():
        if os.path.exists(os.path.join(OUT_DIR, fname)) and os.path.getsize(os.path.join(OUT_DIR, fname)) > 0:
            continue
        img = fetch_image(url)
        save_image(img, fname, resize=(48, 48))

    print("=== All Authentic Assets Processed Successfully ===")

if __name__ == '__main__':
    main()
