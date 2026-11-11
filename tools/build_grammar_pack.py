"""
从 gamestolearnenglish.com 的 superlatives / comparatives 两个语法游戏里提取题库。

superlatives 脚本内嵌：
  i[set][n] = "Who is the tallest?"                      问句
  s[set][n] = {line:[...], alt1:[...], alt2:[...]}       正确句 + 两个干扰句

comparatives 脚本内嵌：
  i[set][n] = "tall"                                     形容词原级
  s[set][n] = ["taller","tall","tallest"]                三个候选（第一个正确）
  o[set][n] = ["He is", "than her."]                     句子框架

产物：app/src/main/assets/game/grammar_pack.json
"""

import json
import os
import re
import urllib.request

BASE = "https://www.gamestolearnenglish.com"
OUT = "app/src/main/assets/game/grammar_pack.json"


def http_get(url: str) -> bytes:
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126",
            "Referer": BASE + "/",
        },
    )
    with urllib.request.urlopen(req, timeout=40) as resp:
        return resp.read()


def load_js(game: str) -> str:
    html = http_get(f"{BASE}/{game}/").decode("utf-8", "ignore")
    match = re.search(rf"{game}/js/all\.min\.[0-9.]+\.js", html)
    if not match:
        raise SystemExit(f"{game}: 找不到脚本")
    print(f"{game}: {match.group(0)}")
    return http_get(BASE + "/" + match.group(0)).decode("utf-8", "ignore")


def to_list(raw: str) -> list[str]:
    return json.loads("[" + raw + "]")


def parse_superlatives(js: str) -> list[dict]:
    questions = {
        (int(s), int(n)): text
        for s, n, text in re.findall(r'i\[(\d+)\]\[(\d+)\]="([^"]+)"', js)
    }
    items: list[dict] = []
    pattern = re.compile(
        r's\[(\d+)\]\[(\d+)\]=\{(.*?)\}(?=,s\[|\};|,$)', re.S
    )
    for set_id, idx, body in pattern.findall(js):
        key = (int(set_id), int(idx))
        prompt = questions.get(key)
        if not prompt:
            continue
        parts = {
            name: to_list(values)
            for name, values in re.findall(
                r'(line|alt1|alt2):\[((?:"[^"]*")(?:,"[^"]*")*)\]', body
            )
        }
        correct = " ".join(parts.get("line", []))
        if not correct:
            continue
        options = [correct]
        for alt in ("alt1", "alt2"):
            text = " ".join(parts.get(alt, []))
            if text and text not in options:
                options.append(text)
        if len(options) < 3:
            continue

        # 站点的问句与句子池顺序并不完全对应，这里用最高级关键词做一致性校验，
        # 只保留「问 tallest 就答 tallest」的题目，避免出现问 shortest 答 fattest。
        marks = re.findall(r"\bthe (\w+)", prompt)
        if not marks:
            continue
        keyword = marks[-1]
        if keyword not in correct:
            continue
        if any(keyword in text for text in options[1:]):
            continue

        items.append(
            {
                "type": "superlative",
                "prompt": prompt,
                "hint": "",
                "answer": correct,
                "options": options,
            }
        )
    return items


def parse_comparatives(js: str) -> list[dict]:
    adjectives = {
        (int(s), int(n)): text
        for s, n, text in re.findall(r'i\[(\d+)\]\[(\d+)\]="([^"]+)"', js)
    }
    candidates = {
        (int(s), int(n)): to_list(raw)
        for s, n, raw in re.findall(
            r's\[(\d+)\]\[(\d+)\]=\[((?:"[^"]*")(?:,"[^"]*")*)\]', js
        )
    }
    frames = {
        (int(s), int(n)): to_list(raw)
        for s, n, raw in re.findall(
            r'o\[(\d+)\]\[(\d+)\]=\[((?:"[^"]*")(?:,"[^"]*")*)\]', js
        )
    }
    items: list[dict] = []
    for key, options in candidates.items():
        frame = frames.get(key)
        adjective = adjectives.get(key, "")
        if not frame or not options or len(options) < 3:
            continue
        prompt = f"{frame[0]} ___ {frame[1]}".replace("  ", " ").strip()
        items.append(
            {
                "type": "comparative",
                "prompt": prompt,
                "hint": adjective,
                "answer": options[0],
                "options": options[:3],
            }
        )
    return items


def main() -> None:
    super_items = parse_superlatives(load_js("superlatives"))
    comp_items = parse_comparatives(load_js("comparatives"))
    print(f"最高级 {len(super_items)} 题，比较级 {len(comp_items)} 题")

    seen: set[str] = set()
    merged: list[dict] = []
    for item in super_items + comp_items:
        key = item["prompt"] + "|" + item["answer"]
        if key in seen:
            continue
        seen.add(key)
        merged.append(item)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as fp:
        json.dump(
            {"source": "gamestolearnenglish.com/superlatives+comparatives", "items": merged},
            fp,
            ensure_ascii=False,
            indent=1,
        )
    print(f"合并去重后 {len(merged)} 题 → {OUT}")


if __name__ == "__main__":
    main()
