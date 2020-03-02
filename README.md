
# LED Cube with Cisco Controller and Pixel Purse LED Panels

This repo needs a bit of cleanup for general public consumption,
but it had all the assets for my LED cube that’s built from
Project MC2 Pixel Purse LED panels.

The slides of BangBangCon West 2020 talk about this project
can be found [here](https://docs.google.com/presentation/d/1FYmVh-brx6SpZoJmzuIXrNegWdtriQ9k38EbrvDupg8).

## Current Measurements:

* 3.3V

0:

* All LEDs off: 0.6A

255:

* All LEDs red: 6.6A
* All LEDs green: 3.6A
* All LEDs blue: 2.8A

* All LEDs red + green: 6.8A
* All LEDs red + blue: 6.5A
* All LEDs green + blue: 4.2A

* All LEDs green + green + blue: 7.8A

128: (with gamma!)

* All LEDs red: 2.2A
* All LEDs green: 1.3A
* All LEDs blue: 1.2A

* All LEDs red + green: 2.5A
* All LEDs red + blue: 2.3A
* All LEDs green + blue: 1.6A

* All LEDs green + green + blue: 2.4A

64: (with gamma!)

* All LEDs red: 1.0A
* All LEDs green: 0.8A
* All LEDs blue: 0.7A

* All LEDs red + green: 1.1A
* All LEDs red + blue:
* All LEDs green + blue: 

* All LEDs green + green + blue: 1.1A


## LED Addressing

### LED Memory Physical Address map

LED mem has 2 buffers (for double-buffered rendering).
Each buffer has a size of 6 * 32 * 32 = 6144 words, so the total LED memory is 12288 words.

In LED mem, each side of the cube has a rectangle assigned to them.

The sides are numbered as follows:
   
```
    +---+
    | 4 |     
+---+---+---+---+
| 0 | 1 | 2 | 3 |
+---+---+---+---+
    | 5 |     
    +---+
```

Within a buffer, addressing is as follows:

* start address of each side is `side_nr * 32 * 32`
* start address corresponds to the top-left of each square above
* the pitch of each side is the same as the number of columns.

The corresponding naming convention: left, front, right, back, top, bottom.

```
    +---+
    | T |     
+---+---+---+---+
| L | F | R | Ba|
+---+---+---+---+
    | Bo|     
    +---+
```

The Hub75Streamer block has a `PANEL_INFO` registers that contains all the information to
translate from the LED mem addressing. If panels are rotated, it can compensate for that.

### LED Memory Locial Address Maps

To make life for the CPU easier, there are also logical address maps, which are
designed such that there is an intuitive overflow behavior wrt neighboring tile.

There is one logical map for each 'infinite ring', where, if you continue in the same
primary direction, you end up in the same place as before in a logical way.

Here is one such example:

```
+---+---+---+---+---+---+
| T | T | T | T | T | T |   
+---+---+---+---+---+---+
| L | F | R | Ba| L | F |
+---+---+---+---+---+---+
| Bo| Bo| Bo| Bo| Bo| Bo|
+---+---+---+---+---+---+
```

In this case, the ring consists of tile L-F-R-B. T and B are considered non-ring
overflow tiles.

When you start on the left side and keep in increasing the X coordinate, you'll wrap around
the cube and end up in the same place after 4 side.

The image above is laid out on a rectangular logical address map, with a pitch that is at least
6 wide. But it's easier to make it 8 wide, so it will look a bit like this:

```
+---+---+---+---+---+---+---+---+
| T | T | T | T | T | T | T | T |
+---+---+---+---+---+---+---+---+
| L | F | R | Ba| L | F | R | Ba|
+---+---+---+---+---+---+---+---+
| Bo| Bo| Bo| Bo| Bo| Bo| Bo| Bo|
+---+---+---+---+---+---+---+---+
```

With the arrangement above, you can blit any image that is 32 pixels height and up to
5 sides wide at any starting point of the ring and have correct roll-over behavior.
Since it makes no sense to have a image that's more than 4 sides wide (because the right
would overwrite the left), that's fine.

For the case above, the top and the bottom tiles will have different overflow
behavior for each for the 4 members of the ring. For example if you are in L and
you go up until you roll over into tile T, then the L(X,Y) coordinate converts into
T(X, Y). However, if you in F and you do the same thing, then F(X,Y) converts
into T(32-Y,X).


Here are different rings: 

F-Bo-Ba-T:

```
+---+---+---+---+---+---+---+---+
| R | R | R | R | R | R | R | R |
+---+---+---+---+---+---+---+---+
| F | Bo| Ba| T | F | Bo| Ba| T |
+---+---+---+---+---+---+---+---+
| L | L | L | L | L | L | L | L |
+---+---+---+---+---+---+---+---+
```

L-Bo-R-T:

```
+---+---+---+---+---+---+---+---+
| Ba| Ba| Ba| Ba| Ba| Ba| Ba| Ba|
+---+---+---+---+---+---+---+---+
| L | Bo| R | T | L | Bo| R | T |
+---+---+---+---+---+---+---+---+
| F | F | F | F | F | F | F | F |
+---+---+---+---+---+---+---+---+
```

Calculation for:

| L | F | R | Ba| L | F | R | Ba|

Given an address for a particular ring:

```
strip_size = 16 * 32 * 32
side_size  = 32 * 32

strip_y_nr = addr / strip_size
strip_x_nr = (addr % strip_size) / side_size

strip_class = [ L, F, R, Ba ][strip_x_nr & 3]
if (strip_y_nr == 0) 
    side_nr = T
    orient_class = { 
                     L  -> (32-X, Y),
                     F  -> (X, Y),  
                     R  -> (32-Y, X),
                     Ba -> (32-X, Y)
                   }[strip_class]
                     
else if (strip_y_nr == 1) 
    side_nr = strip_class
    orient_class = (X,Y)

else if (strip_y_nr == 2)
    side_nr = Bo
    orient_class = { 
                     L  -> (32-X, Y),
                     F  -> (X, Y),  
                     R  -> (32-Y, X),
                     Ba -> (32-X, Y)
                   }[strip_class]

led_mem_addr = (side_nr * 32 * 32) + orient_class[1] * 32 + orient_class[0]
```

Each ring occupies 8 x 3 = 24 sides, but we allocate 32 sides for that.
That gives 32 * 32 * 32 = 32k addresses per ring.
There are 3 rings, so we need 98k local addresses.

