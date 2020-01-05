
#RICKS_ORIG = rick_32x24-0.8bpp.rgb \
#		rick_32x24-1.8bpp.rgb \
#		rick_32x24-2.8bpp.rgb \
#		rick_32x24-3.8bpp.rgb \
#		rick_32x24-4.8bpp.rgb \
#		rick_32x24-5.8bpp.rgb \
#		rick_32x24-6.8bpp.rgb \
#		rick_32x24-7.8bpp.rgb \
#		rick_32x24-8.8bpp.rgb \
#		rick_32x24-9.8bpp.rgb \
#		rick_32x24-10.8bpp.rgb \
#		rick_32x24-11.8bpp.rgb \
#		rick_32x24-12.8bpp.rgb \
#		rick_32x24-13.8bpp.rgb \
#		rick_32x24-14.8bpp.rgb \
#		rick_32x24-15.8bpp.rgb \
#		rick_32x24-16.8bpp.rgb \
#		rick_32x24-17.8bpp.rgb \
#		rick_32x24-18.8bpp.rgb \
#		rick_32x24-19.8bpp.rgb \
#		rick_32x24-20.8bpp.rgb \
#		rick_32x24-21.8bpp.rgb \
#		rick_32x24-22.8bpp.rgb \
#		rick_32x24-23.8bpp.rgb \
#		rick_32x24-24.8bpp.rgb \
#		rick_32x24-25.8bpp.rgb \
#		rick_32x24-26.8bpp.rgb \
#		rick_32x24-27.8bpp.rgb \
#		rick_32x24-28.8bpp.rgb \
#		rick_32x24-29.8bpp.rgb \
#		rick_32x24-30.8bpp.rgb \
#		rick_32x24-31.8bpp.rgb

RICKS_ORIG = rick_32x24-00 \
		rick_32x24-01 \
		rick_32x24-02 \
		rick_32x24-03 \
		rick_32x24-04 \
		rick_32x24-05 \
		rick_32x24-06 \
		rick_32x24-07 \
		rick_32x24-08 \
		rick_32x24-09 \
		rick_32x24-10 \
		rick_32x24-11 \
		rick_32x24-12 \
		rick_32x24-13 \
		rick_32x24-14 \
		rick_32x24-15 \
		rick_32x24-16 \
		rick_32x24-17 \
		rick_32x24-18 \
		rick_32x24-19 \
		rick_32x24-20

RICKS_ORIG_PNG  = $(RICKS_ORIG:=.png)
RICKS_ORIG_RGB  = $(RICKS_ORIG:=.rgb)

RICKS_FINAL = $(RICKS_ORIG)

RICKS_FINAL_IDX  = $(RICKS_FINAL:=.idx)

all: ricks.h palette.h

# Convert into 1 file to force unique palette
ricks_joined.gif: $(RICKS_ORIG_PNG)
	convert $(RICKS_ORIG_PNG) -append -colors 64 $@

.PRECIOUS: %.rgb
%.rgb: ricks_joined.gif
	convert $< -crop 32x24 rick_32x24-%02d.rgb

%.idx: %.rgb palette.info
	./palette_to_c.py palette.info $< $@

ricks.h: ricks.bin
	xxd --include $< > $@

ricks.bin: $(RICKS_FINAL_IDX)
	cat $^ > $@

%.rgb: %.png
	convert $< $@

%.png: rick_32x24.gif
	convert -coalesce rick_32x24.gif rick_32x24-%02d.png

rick_32x24.gif: rick_orig.gif
	convert -resize 32x32 rick_orig.gif rick_32x24.gif

palette.info: ricks_joined.gif
	convert ricks_joined.gif -format %c -depth 8 histogram:info:- > $@

palette.bin: palette.info
	./palette_to_c.py $< dummy dummy $@

palette.h: palette.bin
	xxd --include $< > $@

clean: 
	rm -f rick_32x24.gif *.png *.rgb ricks.* *.idx ricks_joined.gif palette.*
