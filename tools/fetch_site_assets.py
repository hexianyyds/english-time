"""
从 gamestolearnenglish.com/food 抓取词卡素材：图集裁剪数据 + 原声时间轴。

游戏脚本里内嵌三类数据：
  1) 词表        i[setId][index] = "apple"
  2) 图集坐标    s[setId] = {smallImages:[[x,y,w,h]], bigImages:[[x,y,w,h,ox,oy,sw,sh]]}
  3) 原声时长    i[setId] = [d0..d7, ...]  （秒，前 8 个是单词发音）

资源：
  图集  {GAME}/res/content-1.0/set-{id}.0@1.3x.webp
  原声  {GAME}/res/audio-1.0/audio-{id}.ogg

用法：
  python tools/fetch_site_assets.py --out tmp_assets
  python tools/fetch_site_assets.py --out tmp_assets --skip-audio
"""

import argparse
import io
import json
import os
import re
import urllib.request

from PIL import Image

GAME = "food"
BASE = f"https://www.gamestolearnenglish.com/{GAME}"
RES_IMG = f"{BASE}/res/content-1.0"
RES_AUDIO = f"{BASE}/res/audio-1.0"


def http_get(url: str, referer: str = BASE + "/") -> bytes:
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126",
            "Referer": referer,
            "Accept": "*/*",
        },
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        return resp.read()


def load_game_js() -> str:
    html = http_get(f"{BASE}/").decode("utf-8", "ignore")
    match = re.search(rf"{GAME}/js/all\.min\.[0-9.]+\.js", html)
    if not match:
        raise SystemExit("找不到游戏脚本路径")
    js_url = "https://www.gamestolearnenglish.com/" + match.group(0)
    print(f"脚本: {js_url}")
    return http_get(js_url).decode("utf-8", "ignore")


def parse_words(js: str) -> dict[int, list[str]]:
    words: dict[int, dict[int, str]] = {}
    for set_id, idx, word in re.findall(r'i\[(\d+)\]\[(\d+)\]="([^"]+)"', js):
        words.setdefault(int(set_id), {})[int(idx)] = word
    return {k: [v[i] for i in sorted(v)] for k, v in sorted(words.items())}


def parse_frames(js: str) -> dict[int, dict]:
    pattern = re.compile(
        r"\bs\[(\d+)\]=\{smallImages:(\[\[.*?\]\]),bigImages:(\[\[.*?\]\])\}",
        re.S,
    )
    frames: dict[int, dict] = {}
    for set_id, small, big in pattern.findall(js):
        frames[int(set_id)] = {
            "small": json.loads(small),
            "big": json.loads(big),
        }
    return frames


def parse_audio_durations(js: str) -> dict[int, list[float]]:
    """在 getAudioSprite 函数后面找 i[setId]=[d0,d1,...] 的时长表。"""
    anchor = js.find("t.getAudioSprite=")
    if anchor < 0:
        return {}
    chunk = js[anchor: anchor + 6000]
    result: dict[int, list[float]] = {}
    for set_id, values in re.findall(r"i\[(\d+)\]=\[([\d.,]+)\]", chunk):
        result[int(set_id)] = [float(v) for v in values.split(",") if v]
    return result


def slug(word: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", word.lower()).strip("-")


def crop(sheet, frame):
    x, y, w, h = frame[0], frame[1], frame[2], frame[3]
    piece = sheet.crop((x, y, x + w, y + h))
    if len(frame) > 4:
        ox, oy, sw, sh = frame[4], frame[5], frame[6], frame[7]
        canvas = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
        canvas.paste(piece, (ox, oy))
        return canvas
    return piece


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", default="tmp_assets")
    parser.add_argument("--sets", nargs="*", type=int, default=None)
    parser.add_argument("--skip-audio", action="store_true")
    args = parser.parse_args()

    js = load_game_js()
    words = parse_words(js)
    frames = parse_frames(js)
    durations = parse_audio_durations(js)
    print(f"解析到 {len(words)} 个 set / {len(frames)} 份图集 / {len(durations)} 份音频时长")

    targets = args.sets if args.sets else sorted(words.keys())
    os.makedirs(args.out, exist_ok=True)
    audio_dir = os.path.join(args.out, "audio")
    os.makedirs(audio_dir, exist_ok=True)

    manifest: dict[str, dict] = {}

    for set_id in targets:
        if set_id not in words or set_id not in frames:
            print(f"跳过 set {set_id}：缺少词表或坐标")
            continue

        blob = http_get(f"{RES_IMG}/set-{set_id}.0@1.3x.webp")
        sheet = Image.open(io.BytesIO(blob)).convert("RGBA")

        folder = os.path.join(args.out, f"set-{set_id}")
        os.makedirs(folder, exist_ok=True)
        big = frames[set_id]["big"]
        small = frames[set_id]["small"]

        entries = []
        for index, word in enumerate(words[set_id]):
            if index >= len(big):
                break
            crop(sheet, big[index]).save(os.path.join(folder, f"{index}-{slug(word)}.png"))
            entries.append(
                {
                    "index": index,
                    "word": word,
                    "big": f"set-{set_id}/{index}-{slug(word)}.png",
                }
            )

        audio_info = None
        if not args.skip_audio and set_id in durations:
            audio_name = f"audio-{set_id}.ogg"
            audio_path = os.path.join(audio_dir, audio_name)
            if not os.path.exists(audio_path):
                with open(audio_path, "wb") as fp:
                    fp.write(http_get(f"{RES_AUDIO}/audio-{set_id}.ogg"))
            # 每段时长累加得到 [start, end] 毫秒区间
            spans, cursor = [], 0.0
            for value in durations[set_id][: len(words[set_id])]:
                start = int(round(cursor * 1000))
                end = int(round((cursor + value) * 1000))
                spans.append([start, end])
                cursor += value
            audio_info = {"file": f"audio/{audio_name}", "spans": spans}

        manifest[str(set_id)] = {"words": entries, "audio": audio_info}
        print(f"set {set_id}: {len(entries)} 词，图集 {sheet.size}")

    with open(os.path.join(args.out, "manifest.json"), "w", encoding="utf-8") as fp:
        json.dump(manifest, fp, ensure_ascii=False, indent=1)
    print(f"完成 → {args.out}")


if __name__ == "__main__":
    main()
