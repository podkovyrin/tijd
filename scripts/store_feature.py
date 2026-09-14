#!/usr/bin/env python3
"""Render a simple vector-style Play feature graphic; requires Pillow and macOS fonts."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
SCALE = 2
image = Image.new('RGB', (1024*SCALE, 500*SCALE), '#fff9ec')
draw = ImageDraw.Draw(image)
font = '/System/Library/Fonts/Helvetica.ttc'
def text(value, x, y, size, color='#173d30', bold=False):
    draw.text((x*SCALE, y*SCALE), value, font=ImageFont.truetype(font, size*SCALE, index=1 if bold else 0), fill=color)
text('Tijd', 64, 91, 76, bold=True)
text('Time in words.', 67, 197, 30)
text('Your language. Your style.', 67, 247, 23)
text('WORD CLOCK WIDGET', 67, 411, 13, bold=True)
draw.rounded_rectangle((555*SCALE,104*SCALE,957*SCALE,396*SCALE),radius=34*SCALE,fill='#173d30')
for word, y in [('quarter',144),('past',211),('ten',278)]:
    text(word,587,y,48,'#fff9ec')
out=Path('assets/store/feature-graphic.png')
out.parent.mkdir(parents=True,exist_ok=True)
image.resize((1024,500),Image.Resampling.LANCZOS).save(out)
print(out)
