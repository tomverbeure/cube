
CREATE_MIF = ../misc/create_mif.rb
HEX2BIN    = ../misc/hex2bin.py

ROM_FILES = LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.bin \
		    LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.bin \
		    LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.bin \
		    LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.bin

all: tb $(ROM_FILES)
	./tb

tb: tb.v ../spinal/LedMatrixTop.sim.v
	iverilog $^ -o $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.bin: LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.hex
	$(HEX2BIN) < $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.bin: LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.hex
	$(HEX2BIN) < $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.bin: LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.hex
	$(HEX2BIN) < $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.bin: LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.hex
	$(HEX2BIN) < $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d 1024 -w 8 -o 0 -i 4 $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d 1024 -w 8 -o 1 -i 4 $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d 1024 -w 8 -o 2 -i 4 $< > $@

LedMatrixTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d 1024 -w 8 -o 3 -i 4 $< > $@

waves:
	gtkwave waves.vcd waves.gtkw &

clean:
	\rm -f *.bin *.hex tb *.vcd

