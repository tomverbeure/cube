
#RICKS_ORIG = rick_32x23-0.8bpp.rgb \
#		rick_32x23-1.8bpp.rgb \
#		rick_32x23-2.8bpp.rgb \
#		rick_32x23-3.8bpp.rgb \
#		rick_32x23-4.8bpp.rgb \
#		rick_32x23-5.8bpp.rgb \
#		rick_32x23-6.8bpp.rgb \
#		rick_32x23-7.8bpp.rgb \
#		rick_32x23-8.8bpp.rgb \
#		rick_32x23-9.8bpp.rgb \
#		rick_32x23-10.8bpp.rgb \
#		rick_32x23-11.8bpp.rgb \
#		rick_32x23-12.8bpp.rgb \
#		rick_32x23-13.8bpp.rgb \
#		rick_32x23-14.8bpp.rgb \
#		rick_32x23-15.8bpp.rgb \
#		rick_32x23-16.8bpp.rgb \
#		rick_32x23-17.8bpp.rgb \
#		rick_32x23-18.8bpp.rgb \
#		rick_32x23-19.8bpp.rgb \
#		rick_32x23-20.8bpp.rgb \
#		rick_32x23-21.8bpp.rgb \
#		rick_32x23-22.8bpp.rgb \
#		rick_32x23-23.8bpp.rgb \
#		rick_32x23-24.8bpp.rgb \
#		rick_32x23-25.8bpp.rgb \
#		rick_32x23-26.8bpp.rgb \
#		rick_32x23-27.8bpp.rgb \
#		rick_32x23-28.8bpp.rgb \
#		rick_32x23-29.8bpp.rgb \
#		rick_32x23-30.8bpp.rgb \
#		rick_32x23-31.8bpp.rgb

RICKS_ORIG = rick_32x23-00 \
		rick_32x23-02 \
		rick_32x23-04 \
		rick_32x23-06 \
		rick_32x23-08 \
		rick_32x23-10 \
		rick_32x23-12 \
		rick_32x23-14 \
		rick_32x23-16 \
		rick_32x23-18 \
		rick_32x23-20 \
		rick_32x23-22 \
		rick_32x23-24 \
		rick_32x23-26 \
		rick_32x23-28 \
		rick_32x23-30

RICKS_ORIG_PNG  = $(RICKS_ORIG:=.png)
RICKS_ORIG_RGB  = $(RICKS_ORIG:=.rgb)

RICKS_FINAL = rick_32x23-00 \
			  rick_32x23-01 \
			  rick_32x23-02 \
			  rick_32x23-03 \
			  rick_32x23-04 \
			  rick_32x23-05 \
			  rick_32x23-06 \
			  rick_32x23-07 \
			  rick_32x23-08 \
			  rick_32x23-09 \
			  rick_32x23-10 \
			  rick_32x23-11 \
			  rick_32x23-12 \
			  rick_32x23-13 \
			  rick_32x23-14 \
			  rick_32x23-15

RICKS_FINAL_IDX  = $(RICKS_FINAL:=.idx)

all: ricks_compr.h palette.h

# Convert into 1 file to force unique palette
ricks_joined.gif: $(RICKS_ORIG_PNG)
	convert $(RICKS_ORIG_PNG) -append -colors 16 $@

.PRECIOUS: %.rgb
%.rgb: ricks_joined.gif
	convert $< -crop 32x23 rick_32x23-%02d.rgb

%.idx: %.rgb palette.info
	./palette_to_c.py palette.info $< $@

ricks_compr.h: ricks_compr.bin
	xxd --include $< > $@

ricks.bin: $(RICKS_FINAL_IDX)
	cat $^ > $@

ricks_compr.bin: ricks.bin
	./nibbles_to_bytes.py $< $@

%.rgb: %.png
	convert $< $@

%.png: rick_32x23.gif
	convert -coalesce rick_32x23.gif rick_32x23-%02d.png

rick_32x23.gif: rick_orig.gif
	convert -resize 32x23 rick_orig.gif rick_32x23.gif

palette.info: ricks_joined.gif
	convert ricks_joined.gif -format %c -depth 8 histogram:info:- > $@

palette.bin: palette.info
	./palette_to_c.py $< dummy dummy $@

palette.h: palette.bin
	xxd --include $< > $@

clean: 
	rm -f rick_32x23.gif *.png *.rgb ricks.* *.idx ricks_joined.gif palette.* ricks_compr.*
