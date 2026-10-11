import struct

with open("tools/services.odex", "rb") as f:
    f.seek(40)
    hdr = f.read(0x70)
    magic = hdr[:8]
    file_size = struct.unpack_from("<I", hdr, 0x20)[0]
    print(f"Magic: {magic}, DEX file size: {file_size}")

with open("tools/services.odex", "rb") as f:
    f.seek(40)
    dex_data = f.read(file_size)

with open("tools/services.dex", "wb") as f:
    f.write(dex_data)

print(f"Extracted {len(dex_data)} bytes to tools/services.dex")
