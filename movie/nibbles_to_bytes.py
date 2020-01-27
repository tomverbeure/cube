#! /usr/bin/env python3

import sys
import struct
from array import array 

import pprint


pp = pprint.PrettyPrinter(indent=4)

bytes_out = []

with open(sys.argv[1], "rb") as nibble_file:
    while True:
        bytes_input = nibble_file.read(2)

        if bytes_input:
            byte_out = bytes_input[0] + bytes_input[1] * 16
            bytes_out.append(byte_out)
        else:
            break

with open(sys.argv[2], "wb") as bytes_out_file:
    bytes = bytearray(bytes_out)
    bytes_out_file.write(bytes)

