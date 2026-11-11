"""
把 tmp_assets 整理成 App 资源包：图片（双尺寸 WebP）+ 原声片段（ogg）+ 词表。

产物：
  app/src/main/assets/game/food/<slug>.webp         200px
  app/src/main/assets/game/food/hd/<slug>.webp      384px
  app/src/main/assets/game/audio/audio-<set>.ogg    原声（按区间播放，不切分）
  app/src/main/assets/game/food_pack.json           词表（含图片与原声区间）
"""

import json
import os
import shutil

from PIL import Image

SRC = "tmp_assets"
ASSETS = "app/src/main/assets/game"
DEST_IMG = os.path.join(ASSETS, "food")
DEST_AUDIO = os.path.join(ASSETS, "audio")
DEST_JSON = os.path.join(ASSETS, "food_pack.json")

SIZES = ((200, DEST_IMG), (320, os.path.join(DEST_IMG, "hd")))

ZH = {
    # set 0 快餐
    "burger": "汉堡", "bread": "面包", "taco": "墨西哥卷饼", "ice": "冰块",
    "fries": "薯条", "instant noodles": "方便面", "sushi": "寿司", "donut": "甜甜圈",
    # set 1 主食
    "hot dog": "热狗", "rice": "米饭", "ice cream": "冰淇淋", "egg": "鸡蛋",
    "popcorn": "爆米花", "water": "水", "candy": "糖果", "fried chicken": "炸鸡",
    # set 2 水果（一）
    "apple": "苹果", "blueberries": "蓝莓", "kiwi fruit": "猕猴桃", "strawberries": "草莓",
    "orange": "橙子", "lemon": "柠檬", "mango": "芒果", "watermelon": "西瓜",
    # set 3 水果（二）
    "peach": "桃子", "grapefruit": "葡萄柚", "pear": "梨", "grapes": "葡萄",
    "banana": "香蕉", "tangerines": "橘子", "cherries": "樱桃", "lime": "青柠",
    # set 4 肉类（一）
    "chicken": "鸡肉", "lamb": "羊肉", "beef": "牛肉", "pork": "猪肉",
    "fish": "鱼肉", "sausage": "香肠", "steak": "牛排", "lobster": "龙虾",
    # set 5 肉类（二）
    "ham": "火腿", "roast beef": "烤牛肉", "chicken nuggets": "鸡块", "bacon": "培根",
    "chicken wing": "鸡翅", "chicken leg": "鸡腿", "pork chop": "猪排", "shrimp": "虾",
    # set 6 饮品（一）
    "coffee": "咖啡", "tea": "茶", "hot chocolate": "热巧克力", "ice coffee": "冰咖啡",
    "ice tea": "冰茶", "orange juice": "橙汁", "lemonade": "柠檬水", "cola": "可乐",
    # set 7 饮品（二）
    "espresso": "浓缩咖啡", "cafe latte": "拿铁", "cappuccino": "卡布奇诺",
    "black coffee": "黑咖啡", "lemon tea": "柠檬茶", "banana milkshake": "香蕉奶昔",
    "fruit smoothie": "水果冰沙", "chocolate milkshake": "巧克力奶昔",
    # set 8 蔬菜（一）
    "tomatoes": "西红柿", "mushrooms": "蘑菇", "garlic": "大蒜", "olives": "橄榄",
    "lettuce": "生菜", "broccoli": "西兰花", "cucumber": "黄瓜", "potatoes": "土豆",
    # set 9 蔬菜（二）
    "bell peppers": "甜椒", "peas": "豌豆", "corn": "玉米", "chilies": "辣椒",
    "onions": "洋葱", "eggplant": "茄子", "carrots": "胡萝卜", "cabbage": "卷心菜",
    # set 10 餐点（一）
    "burger and fries": "汉堡薯条", "rice and vegetables": "蔬菜饭", "fish and chips": "炸鱼薯条",
    "fried rice": "炒饭", "salad": "沙拉", "chicken and rice": "鸡肉饭",
    "scrambled egg on toast": "吐司炒蛋", "pizza": "披萨",
    # set 11 餐点（二）
    "English breakfast": "英式早餐", "chips and dip": "薯条蘸酱", "steak and chips": "牛排薯条",
    "beans on toast": "吐司焗豆", "soup": "汤", "curry and rice": "咖喱饭",
    "spaghetti bolognese": "肉酱意面", "burrito": "墨西哥卷饼",
    # set 12 烘焙
    "toast": "吐司", "croissant": "可颂", "bread rolls": "面包卷", "baguette": "法棍",
    "pretzel": "椒盐卷饼", "bagel": "贝果", "cheese toastie": "芝士吐司", "panini": "帕尼尼",
    # set 13 甜点
    "cake": "蛋糕", "cup cake": "纸杯蛋糕", "muffin": "玛芬", "brownies": "布朗尼",
    "pie": "派", "cookie": "曲奇", "chocolate": "巧克力",
    # set 14 蔬果（二）
    "plum": "李子", "pineapple": "菠萝", "dragon fruit": "火龙果", "ginger": "姜",
    "cauliflower": "花椰菜", "celery": "芹菜", "pumpkin": "南瓜", "avocado": "牛油果",
    # set 15 小吃
    "wrap": "卷饼", "dumplings": "饺子", "samosas": "咖喱角", "spring rolls": "春卷",
    "cheese burger": "芝士汉堡", "sandwich": "三明治", "pancakes": "松饼",
    # set 16 早餐（二）
    "waffles": "华夫饼", "spaghetti": "意大利面", "pasta": "意面", "boiled eggs": "水煮蛋",
    "butter": "黄油", "cheese": "奶酪", "milk": "牛奶", "yogurt": "酸奶",
    # set 17 零食
    "potato chips": "薯片", "pet food": "宠物食品", "cereal": "麦片", "biscuits": "饼干",
    "instant coffee": "速溶咖啡", "mustard": "芥末", "tuna": "金枪鱼", "beans": "豆子",
    # set 18 调味
    "apple juice": "苹果汁", "honey": "蜂蜜", "peanut butter": "花生酱",
    "sugar": "糖", "ketchup": "番茄酱", "salt": "盐",
    # set 19 杂项
    "sweetcorn": "甜玉米", "nuts": "坚果", "mints": "薄荷糖", "noodles": "面条",
    "guacamole": "鳄梨酱",
}


def slug(word: str) -> str:
    return "".join(c if c.isalnum() else "-" for c in word.lower()).strip("-")


def main() -> None:
    with open(os.path.join(SRC, "manifest.json"), encoding="utf-8") as fp:
        manifest: dict[str, dict] = json.load(fp)

    os.makedirs(DEST_AUDIO, exist_ok=True)
    for _, folder in SIZES:
        os.makedirs(folder, exist_ok=True)

    seen: set[str] = set()
    missing: list[str] = []
    pack: list[dict] = []

    for set_id in sorted(manifest, key=int):
        block = manifest[set_id]
        audio = block.get("audio")

        if audio:
            src_audio = os.path.join(SRC, audio["file"])
            dst_audio = os.path.join(DEST_AUDIO, os.path.basename(audio["file"]))
            if os.path.exists(src_audio) and not os.path.exists(dst_audio):
                shutil.copyfile(src_audio, dst_audio)

        for entry in block["words"]:
            word = entry["word"]
            if word in seen:
                continue
            seen.add(word)
            zh = ZH.get(word)
            if not zh:
                missing.append(word)
                continue

            name = f"{slug(word)}.webp"
            with Image.open(os.path.join(SRC, entry["big"])) as raw:
                bbox = raw.getbbox()
                base = raw.crop(bbox) if bbox else raw
                for size, folder in SIZES:
                    img = base.copy()
                    ratio = size / max(img.width, img.height)
                    img = img.resize(
                        (max(1, int(img.width * ratio)), max(1, int(img.height * ratio))),
                        Image.LANCZOS,
                    )
                    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
                    canvas.paste(img, ((size - img.width) // 2, (size - img.height) // 2))
                    canvas.save(os.path.join(folder, name), "WEBP", quality=88, method=6)

            item = {
                "word": word,
                "zh": zh,
                "pos": "n.",
                "topic": "food",
                "level": 1 if len(word.split()) == 1 else (2 if len(word.split()) == 2 else 3),
                "image": f"game/food/{name}",
                "imageHd": f"game/food/hd/{name}",
            }
            if audio:
                span = audio["spans"][entry["index"]]
                item["audioFile"] = f"game/audio/{os.path.basename(audio['file'])}"
                item["audioStart"] = span[0]
                item["audioEnd"] = span[1]
            pack.append(item)

    pack.sort(key=lambda x: x["word"])
    with open(DEST_JSON, "w", encoding="utf-8") as fp:
        json.dump(
            {"source": "gamestolearnenglish.com/food", "words": pack},
            fp,
            ensure_ascii=False,
            indent=1,
        )

    imgs = os.listdir(DEST_IMG)
    img_bytes = sum(os.path.getsize(os.path.join(DEST_IMG, f)) for f in imgs if f.endswith(".webp"))
    hd_bytes = sum(
        os.path.getsize(os.path.join(DEST_IMG, "hd", f)) for f in os.listdir(os.path.join(DEST_IMG, "hd"))
    )
    audio_bytes = sum(os.path.getsize(os.path.join(DEST_AUDIO, f)) for f in os.listdir(DEST_AUDIO))
    print(f"词条 {len(pack)}，图片 {len([i for i in imgs if i.endswith('.webp')])} 张")
    print(f"图片 {img_bytes / 1024:.0f} KB + 高清 {hd_bytes / 1024:.0f} KB + 原声 {audio_bytes / 1024:.0f} KB")
    if missing:
        print(f"缺少中文释义：{missing}")


if __name__ == "__main__":
    main()
