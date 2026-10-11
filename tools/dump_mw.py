import struct

with open("tools/multiwindow.dex", "rb") as f:
    data = f.read()

string_ids_size, string_ids_off = struct.unpack_from("<II", data, 0x38)
type_ids_size, type_ids_off = struct.unpack_from("<II", data, 0x40)
proto_ids_size, proto_ids_off = struct.unpack_from("<II", data, 0x48)
field_ids_size, field_ids_off = struct.unpack_from("<II", data, 0x50)
method_ids_size, method_ids_off = struct.unpack_from("<II", data, 0x58)
class_defs_size, class_defs_off = struct.unpack_from("<II", data, 0x60)

strings = []
for i in range(string_ids_size):
    str_off = struct.unpack_from("<I", data, string_ids_off + i * 4)[0]
    null_pos = data.find(b"\x00", str_off + 1)
    s = data[str_off+1:null_pos].decode("utf-8", errors="ignore")
    if str_off + 2 <= len(data) and (data[str_off] & 0x80):
        s = data[str_off+2:null_pos].decode("utf-8", errors="ignore")
    strings.append(s)

types = []
for i in range(type_ids_size):
    type_desc_idx = struct.unpack_from("<I", data, type_ids_off + i * 4)[0]
    types.append(strings[type_desc_idx])

fields = []
for i in range(field_ids_size):
    class_idx, type_idx, name_idx = struct.unpack_from("<HHI", data, field_ids_off + i * 8)
    fields.append((types[class_idx], types[type_idx], strings[name_idx]))

# Let's inspect class defs
for i in range(class_defs_size):
    cdef_off = class_defs_off + i * 32
    class_idx, access_flags, superclass_idx, interfaces_off, source_file_idx, annotations_off, class_data_off, static_values_off = struct.unpack_from("<IIIIIIII", data, cdef_off)
    cname = types[class_idx]
    if "WindowManagerPolicy" in cname or "MultiWindowManager" in cname:
        print(f"\nClass: {cname}, static_values_off={static_values_off}, class_data_off={class_data_off}")
