#!/bin/bash
OUT_DIR="/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable"
BASE="https://raw.githubusercontent.com/B00merang-Artwork/Windows-XP/master"

resolve_and_download() {
    local rel="$1"
    local out="$2"
    local dir
    dir=$(dirname "$rel")
    
    echo "Resolving $rel -> $out"
    local encoded
    encoded=$(echo "$rel" | sed 's/+/%2B/g')
    curl -L -s --connect-timeout 10 "$BASE/$encoded" -o "$out"
    
    # Check if downloaded file is PNG or symlink text
    if head -c 8 "$out" | grep -q "PNG"; then
        echo "Successfully downloaded PNG: $out"
    else
        local target
        target=$(cat "$out" | tr -d '\r\n')
        echo "Symlink target is: $target"
        local new_rel="$dir/$target"
        encoded=$(echo "$new_rel" | sed 's/+/%2B/g')
        curl -L -s --connect-timeout 10 "$BASE/$encoded" -o "$out"
        if head -c 8 "$out" | grep -q "PNG"; then
            echo "Successfully resolved and downloaded PNG: $out"
        else
            echo "Failed resolving $new_rel"
        fi
    fi
}

resolve_and_download "32x32/actions/zoom-in.png" "$OUT_DIR/ic_pv_zoom_in.png"
resolve_and_download "32x32/actions/zoom-out.png" "$OUT_DIR/ic_pv_zoom_out.png"
resolve_and_download "32x32/actions/zoom-fit-best.png" "$OUT_DIR/ic_pv_fit.png"
resolve_and_download "32x32/actions/view-presentation.png" "$OUT_DIR/ic_pv_slideshow.png"
resolve_and_download "32x32/actions/go-previous.png" "$OUT_DIR/ic_pv_prev.png"
resolve_and_download "32x32/actions/go-next.png" "$OUT_DIR/ic_pv_next.png"
resolve_and_download "32x32/actions/edit-undo.png" "$OUT_DIR/ic_pv_rotate_left.png"
resolve_and_download "32x32/actions/edit-redo.png" "$OUT_DIR/ic_pv_rotate_right.png"

echo "=== Final Status ==="
file "$OUT_DIR"/ic_pv_*.png
