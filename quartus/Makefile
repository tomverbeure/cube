
QUARTUS_DIR = /home/tom/altera/13.0sp1/quartus/bin/

CPU_RAM0 = $(wildcard ./db/*ram0_CCPipelinedMemory*.mif)
CPU_RAM1 = $(wildcard ./db/*ram1_CCPipelinedMemory*.mif)
CPU_RAM2 = $(wildcard ./db/*ram2_CCPipelinedMemory*.mif)
CPU_RAM3 = $(wildcard ./db/*ram3_CCPipelinedMemory*.mif)

CPU_RAMS = $(CPU_RAM0) $(CPU_RAM1) $(CPU_RAM2) $(CPU_RAM3)

all: 
	$(QUARTUS_DIR)/quartus_map --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_fit --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_asm --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_sta hwic-3g-cdma -c hwic-3g-cdma

update_ram: $(CPU_RAMS)
	$(QUARTUS_DIR)/quartus_cdb hwic-3g-cdma -c hwic-3g-cdma --update_mif
	$(QUARTUS_DIR)/quartus_asm --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma

$(CPU_RAM0): ../sw/progmem0.mif
	cp $< $@

$(CPU_RAM1): ../sw/progmem1.mif
	cp $< $@

$(CPU_RAM2): ../sw/progmem2.mif
	cp $< $@

$(CPU_RAM3): ../sw/progmem3.mif
	cp $< $@
