
QUARTUS_DIR = /home/tom/altera/13.0sp1/quartus/bin/

all: 
	$(QUARTUS_DIR)/quartus_map --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_fit --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_asm --read_settings_files=on --write_settings_files=off hwic-3g-cdma -c hwic-3g-cdma
	$(QUARTUS_DIR)/quartus_sta hwic-3g-cdma -c hwic-3g-cdma


