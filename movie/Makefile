
#RICKS = rick_32x23-0.8bpp.rgb \
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

RICKS = rick_32x23-0.8bpp.rgb \
		rick_32x23-2.8bpp.rgb \
		rick_32x23-4.8bpp.rgb \
		rick_32x23-6.8bpp.rgb \
		rick_32x23-8.8bpp.rgb \
		rick_32x23-10.8bpp.rgb \
		rick_32x23-12.8bpp.rgb \
		rick_32x23-14.8bpp.rgb \
		rick_32x23-16.8bpp.rgb \
		rick_32x23-18.8bpp.rgb \
		rick_32x23-20.8bpp.rgb \
		rick_32x23-22.8bpp.rgb \
		rick_32x23-24.8bpp.rgb \
		rick_32x23-26.8bpp.rgb \
		rick_32x23-28.8bpp.rgb \
		rick_32x23-30.8bpp.rgb

all: 24bpp_to_8bpp ricks.h

ricks.h: ricks.bin
	xxd --include $< > $@

ricks.bin: $(RICKS)
	cat $(RICKS) > $@

%.8bpp.rgb: %.rgb
	./24bpp_to_8bpp $< $@

#rick_32x23-0.8bpp.rgb: rick_32x23-0.rgb 24bpp_to_8bpp
#	./24bpp_to_8bpp $< $@

#rick_32x23-0.rgb: rick_orig.gif
%.rgb: %.png
	convert $< $@

%.png: rick_32x23.gif
	convert rick_32x23.gif rick_32x23.png

rick_32x23.gif: rick_orig.gif
	convert -resize 32x23 rick_orig.gif rick_32x23.gif

24bpp_to_8bpp: 24bpp_to_8bpp.c
	gcc -g -O0 $< -o $@

clean: 
	rm -f rick_32x23.gif *.png *.rgb 24bpp_to_8bpp ricks.bin ricks.h
