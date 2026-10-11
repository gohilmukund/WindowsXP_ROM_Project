with open("tools/services.odex", "rb") as f:
    data = f.read()

p = 0
while True:
    idx = data.find(b"setOverscan", p)
    if idx == -1:
        break
    print(f"Found setOverscan at {idx}")
    start = max(0, idx - 100)
    end = min(len(data), idx + 200)
    print(f"Context: {data[start:end]}")
    p = idx + 11
