
# The Pixel Purse

Project MC2 was a short lived TV show for kids about a secret group of high school girls spies who 
had to protect the world by using science and technology. 

I had never heard about it until I saw this post on Twitter:

< Slide >

This fabulous Pixel Purse is, well, a purse with an RGB LED matrix with a standard HUB75 interface 
that can display preloaded images or custom images that the owner can design on a phone app and upload.

Originally priced at $60, Amazon had it on sale for the low low price of $6.18, including shipping.
That's really a steal if you know that just the LED panel cost around $15 on AliExpress!

I always wanted to make one of those fancy LED cubes that are now all the rage in some circles, so this was 
a perfect excuse to start acquiring the goods. A cube has 6 square faces, and since the pixel purse LED 
panel has a 2:1 aspect ratio, I needed 12 purses.

Amazon limited the number of purses per customer, so I had to recruit my colleagues to
organize a group buy.

3 days later, the following stack of boxes were dropped off at my cube at work.

There is a very confused data scientist at Amazon trying to understand the surge of pixel purse 
purchases at a major computer graphics company...

To prevent marital problems, the purses were carted in through the back door and immediately dismantled
and stripped into their bare components. In addition to the panel, I also got 48 AA batteries,
12 4Mbit serial flash PROMs, and a bunch of voltage regulators out of it.

After blog post, the LED panels ended up in a box in my garage.

# Cisco HWIC-3G-CDMA

My main hobby is playing with FPGAs. FPGA are expensive in low volumes.  One way around that is 
to find commercial products on eBay that use them, reverse-engineer the PCB and repurpose them 
for hobby usage.

One company that uses FPGAs by the truckload is Cisco: with their large markups, they don't care 
as much about the increased cost, and they want the ability to fix issues in the field with OS updates,
 which is something you can do with FPGAs.

Data communication protocols age pretty quickly, and obsolete products end up on eBay at ridiculously 
low prices.

All of that lead to the Cisco HWIC-3G-CDMA interface card: a WAN extension board that was used to connect
a LAN operated by a Cisco router to the now decommissioned 3G CDMA network.

These used to cost hundreds of dollars, but can now be bought for just $8 on eBay. 

This board is particularly attractive for a combination of reasons:

* a relatively large Intel Cyclone II FPGA is supported by the free (as in beer) version of its design software.
* the presence of a JTAG interface, which is needed for development
* lots of FPGA-controlled signals that are routed straight to the Cisco standard HWIC connector, which
  makes them easy to be used for your own purposes.
* a standard RS-232 connector, perfect for to drive a console
* some other interesting features, such a SDRAM, a socket for NOR flash, a USB interface

Armed with a older Cisco router ($10K 10 years ago, $60 today), I reverse-engineered the signals to
make the board do what I wanted.

I also designed a small new PCB for this Cisco board to transform it in an easy to use and dirt cheap FPGA
development board.

# The State of the PCB Industry

When I was a teenager, some 35 years ago, making your own PCBs was a very difficult affair: after transferring
your design on a transparent slide, you had apply some very dirty chemicals on a copper plate, illuminated it with
UV light, dip it into a bath with more dirty chemicals (that were hard to dispose off), and if you were
very lucky and experienced, you might up with something reasonably clean. It was sufficiently cumbersome that I
never actually created one.

Last year, I decided it was finally time to design my first PCB and have it prototyped at JLCPCB one of the most
visible Chinese PCB fabs.

It couldn't have been easier: I went from downloading KiCad to uploading my design for production in just a few hours. Three weeks later,
I received 10 high quality PCBs for the grant total amount of $2 + $5 shipping! Incredible.

PCBs are a great example of high precision manufacturing, with minimum trace widths and drilling holes of 0.2 mm.

# BangBangCon Badge

This gave me the idea about using PCBs not only for electronics but fields where you'd normally use a 3D printer
or a laser cutter.

As a first proof of concept, I decided to make a custom !!Con badge: I don't have any design talent, so I just
took a screenshot of the logo from their website, imported it into Inkscape, converted it into vector graphics,
and then used a tool called "svg2shenzhen" to convert the design into a KiCad PCB. From start to finish, it
took only a couple of hours, and that included installing Inkscape, learning to use it peculiarities etc.

You can see the first iteration here. After this proof of concept, Josh from the organizing committee took over
and improved the design into the official conference badge which you have all hanging around your neck.

# The Plan: a Cisco Powered LED Cube with PCB mechanicals

All of the things are now in place for the project that I'm showing here: a cheap LED cube built with obsolete or
repurposed electronics that abuses cheap PCBs for the mechanical construction.

The idea was to merge 3 smaller PCBs into corner pieces by using right angled pin header connectors. With a few strategic holes
to screw the LED matrix to the corner pieces, it should be possible to construct something that's solid.

There are no standard dimensions for LED panels, its PCB has integrated circuits components close to the border,  
so armed with a set of calipers, I came up with an L-shaped contraption that lookslike this:

...

JCLPCB quotes the same price as long as you stay within a 10cm x 10cm area, as long as you have multiple copies of
the same design. So using a technical called "panelization", I duplicated this basic form 4 times. The end results was
50 L-primitives for only $5!

I also designed a small interface board to connect the Cisco board to the standardized HUB75 LED interface.

Here's the results: all of this cost only $9 + shipping!

# Assembly

Here you can see the first try at making the brackets that will hold the cube together. The basic idea definitely
worked, but it turns out that millimeter gaps here and there add up quickly, and before you know it, you end up
with with 5mm gaps and something that's not very pleasing visually.

I was afraid that I had to go through another round of having updated L-shapes produced, but it turns out I could
salvage the design with some creative cutting of the pin headers. And now the result looks much better.

Seeing the whole thing come together was very pleasing. This picture has a major death star tunnel feeling to it.

Once done, it was time to get to the electronics and programming part.

# Hardware

LED panels has a standard HUB75 interface. It can daisy-chain different panels and make them into one
large shift register that controls and drives one row only. And an LED is either on or off. There is no inbetween state.
You get color shades with pulse modulation, and by rotating through the rows very quickly.

Driving this kind of interface with precision is a perfect job for FPGAs, so I ended up with an architecture that
converts a logic organization of pixel rectangles into a continous scan stream of pulse modulated values.
The hardware also does automatic gamma conversion of the colors (really important to make things look good!)
and since LEDs can consume tons of power, there are global dimming control mulipliers as well.

One tricky aspect about cubes is now to wrap a weirdly shaped 2D surface onto the faces of a 3D cube.
there are an number of different options. You can treat each pixel as a cartesian 3D coordinate, which works really
well with mathematical function. You could use spherical coordinates with the center located in the center of the
cube and project onto the faces.

I chose a method where there is a primary ring but when you go over or under the ring, you wrap into the faces
that don't belong to the ring. It's computationally low effort system that works very well in some cases.

To create content, there's a RISC-V CPU.

Here's a block diagram of it all. Initially, there was a RICK block as well, but that function was eventually moved
from hardware to software.

# Software

The possibilities are endless here. I chose a gaming theme for now, with Rick thrown in as a variation.

a 50MHz RISC-V CPU is fast enough do pretty much anything.

# Reveal

And here's the demo!

