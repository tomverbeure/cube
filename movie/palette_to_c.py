#! /usr/bin/env python3

import sys
import struct
from array import array 

import pprint


pp = pprint.PrettyPrinter(indent=4)

palette = []
palette_lookup = {}

def create_chunks(list_name, n):
    for i in range(0, len(list_name), n):
        yield list_name[i:i + n]


with open(sys.argv[1]) as palette_file:
    for line in palette_file:
        r = int(line[13:16])
        g = int(line[17:20])
        b = int(line[21:24])

        palette_lookup[r + g*256 + b * 65536] = len(palette)//3
        palette.append(r)
        palette.append(g)
        palette.append(b)

if len(sys.argv) > 4:
    with open(sys.argv[4], "wb") as palette_output_file:
        bytes = bytearray(palette)
        palette_output_file.write(bytes)
        sys.exit(0)

with open(sys.argv[2], 'rb') as bitmap_file:
    bitmap = bytearray(bitmap_file.read())
    pixels = list(create_chunks(bitmap, 3))

    output_buf = []

    for pixel in pixels:
        r = pixel[0]
        g = pixel[1]
        b = pixel[2]

        index = palette_lookup[r + g*256 + b * 65536]
        output_buf.append(index)

with open(sys.argv[3], "wb") as output_file:

        bytes = bytearray(output_buf)
        output_file.write(bytes)




#pp.pprint(palette)
#pp.pprint(palette_lookup)
#pp.pprint(pixels)
#pp.pprint(output_buf)


