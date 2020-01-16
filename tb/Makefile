
MEM_WORDS       = 6144

CREATE_MIF = ../misc/create_mif.rb
HEX2BIN    = ../misc/hex2bin.py

ROM_FILES = CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.bin \
		    CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.bin \
		    CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.bin \
		    CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.bin \
			CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_r.bin \
			CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_g.bin \
			CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_b.bin

all: tb $(ROM_FILES)
	./tb

tb: tb.v ../spinal/CubeTop.sim.v
	iverilog $^ -o $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.bin: CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.hex
	$(HEX2BIN) < $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.bin: CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.hex
	$(HEX2BIN) < $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.bin: CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.hex
	$(HEX2BIN) < $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.bin: CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.hex
	$(HEX2BIN) < $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol0.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d $(MEM_WORDS) -w 8 -o 0 -i 4 $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol1.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d $(MEM_WORDS) -w 8 -o 1 -i 4 $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol2.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d $(MEM_WORDS) -w 8 -o 2 -i 4 $< > $@

CubeTop.v_toplevel_core_u_cpu_u_cpu_ram_ram_symbol3.hex: ../sw/progmem.bin
	$(CREATE_MIF) -f hex -d $(MEM_WORDS) -w 8 -o 3 -i 4 $< > $@

waves:
	gtkwave waves.vcd waves.gtkw &

CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_r.bin \
CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_g.bin \
CubeTop.v_toplevel_core_u_hub75_streamer_gamma_rom_b.bin :
	cp ../spinal/*gamma_rom*.bin .

clean:
	\rm -f *.bin *.hex tb *.vcd

