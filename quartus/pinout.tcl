

;#------------------------------------------------------------
;# Clock - 25MHz
;#------------------------------------------------------------
;# Single ended Xtal clock
set_location_assignment PIN_L2 -to clk25

;#------------------------------------------------------------
;# LEDs
;#------------------------------------------------------------

;# Green LED on CR1
set_location_assignment PIN_AA19 -to leds[0]
;# Green LED on CR3
set_location_assignment PIN_AB19 -to leds[1]
;# Green LED    on CR5
;# Green YELLOW on CR5
set_location_assignment PIN_AA15 -to leds[2]
set_location_assignment PIN_AB15 -to leds[3]

;#------------------------------------------------------------
;# Miscellanous
;#------------------------------------------------------------

# Goes to the EN pin of the MIC37032 LDO. When high, this creates
# the 3.3V power rail for the PCI Express Mini card from the 5V of the HWIC connector.
# Always assign this to 0 unless you want to use the PCI Express Mini card.
set_location_assignment PIN_AB17 -to pci_mini_33v_ena

# The ISP1564HL chip requires a 12MHz clock on its XTAL1 pin. 
# This clock is generated by the FPGA.
set_location_assignment PIN_D5   -to pci_xtal

# These are pins that are set as output in the BSCAN dump and
# that are toggling while the card is in the router.
set_location_assignment PIN_C2 -to misc_outputs[0]

;#------------------------------------------------------------
;# RS-232
;#------------------------------------------------------------
set_location_assignment PIN_N6   -to uart_drv_ena_
set_location_assignment PIN_P6   -to uart_drv_sd_
set_location_assignment PIN_D6   -to uart_txd
set_location_assignment PIN_F4   -to uart_rxd
set_location_assignment PIN_G6   -to uart_rts
set_location_assignment PIN_H6   -to uart_cts

;#------------------------------------------------------------
;# DDR DRAM
;#------------------------------------------------------------
;# Name as used by the Quartus MegaCore with 'ddr' prefix
set_location_assignment PIN_B20  -to ddr_dq[0]
set_location_assignment PIN_A20  -to ddr_dq[1]
set_location_assignment PIN_B19  -to ddr_dq[2]
set_location_assignment PIN_A19  -to ddr_dq[3]
set_location_assignment PIN_D16  -to ddr_dq[4]
set_location_assignment PIN_E15  -to ddr_dq[5]
set_location_assignment PIN_D15  -to ddr_dq[6]
set_location_assignment PIN_C14  -to ddr_dq[7]
set_location_assignment PIN_A17  -to ddr_dqs[0]
set_location_assignment PIN_E14  -to ddr_dm[0]
set_location_assignment PIN_D14  -to ddr_dq[8]
set_location_assignment PIN_F14  -to ddr_dq[9]
set_location_assignment PIN_F13  -to ddr_dq[10]
set_location_assignment PIN_B16  -to ddr_dq[11]
set_location_assignment PIN_A16  -to ddr_dq[12]
set_location_assignment PIN_B15  -to ddr_dq[13]
set_location_assignment PIN_A15  -to ddr_dq[14]
set_location_assignment PIN_F12  -to ddr_dq[15]
set_location_assignment PIN_A13  -to ddr_dqs[1]
set_location_assignment PIN_A14  -to ddr_dm[1]
set_location_assignment PIN_D9   -to ddr_a[12]
set_location_assignment PIN_D11  -to ddr_a[11]
set_location_assignment PIN_A3   -to ddr_a[10]
set_location_assignment PIN_B5   -to ddr_a[9]
set_location_assignment PIN_B6   -to ddr_a[8]
set_location_assignment PIN_B7   -to ddr_a[7]
set_location_assignment PIN_B8   -to ddr_a[6]
set_location_assignment PIN_B9   -to ddr_a[5]
set_location_assignment PIN_B10  -to ddr_a[4]
set_location_assignment PIN_B11  -to ddr_a[3]
set_location_assignment PIN_A7   -to ddr_a[2]
set_location_assignment PIN_A9   -to ddr_a[1]
set_location_assignment PIN_A10  -to ddr_a[0]
set_location_assignment PIN_C18  -to ddr_ba[1]
set_location_assignment PIN_C17  -to ddr_ba[0]
set_location_assignment PIN_B17  -to ddr_cas_n
set_location_assignment PIN_C9   -to ddr_cke[0]
set_location_assignment PIN_A5   -to ddr_cs_n[0]
set_location_assignment PIN_B18  -to ddr_ras_n
set_location_assignment PIN_B14  -to ddr_we_n
set_location_assignment PIN_A4   -to clk_to_sdram[0]
set_location_assignment PIN_B4   -to clk_to_sdram_n[0]

;#------------------------------------------------------------
;# ISP1564HL PCI interface
;#------------------------------------------------------------
set_location_assignment PIN_AA5  -to pci_inta_
set_location_assignment PIN_AB18 -to pci_rst_
set_location_assignment PIN_E19  -to pci_clk
set_location_assignment PIN_J1   -to pci_gnt_
set_location_assignment PIN_AB4  -to pci_req_
set_location_assignment PIN_W2   -to pci_irdy_
set_location_assignment PIN_W3   -to pci_trdy_
set_location_assignment PIN_W4   -to pci_devsel_
set_location_assignment PIN_V1   -to pci_stop_
set_location_assignment PIN_V2   -to pci_perr_
set_location_assignment PIN_V4   -to pci_serr_

set_location_assignment PIN_C1   -to pci_ad[31]
set_location_assignment PIN_D2   -to pci_ad[30]
set_location_assignment PIN_D1   -to pci_ad[29]
set_location_assignment PIN_E1   -to pci_ad[28]
set_location_assignment PIN_D4   -to pci_ad[27]
set_location_assignment PIN_E4   -to pci_ad[26]
set_location_assignment PIN_E3   -to pci_ad[25]
set_location_assignment PIN_E2   -to pci_ad[24]
set_location_assignment PIN_F3   -to pci_ad[23]
set_location_assignment PIN_F2   -to pci_ad[22]
set_location_assignment PIN_F1   -to pci_ad[21]
set_location_assignment PIN_G5   -to pci_ad[20]
set_location_assignment PIN_G3   -to pci_ad[19]
set_location_assignment PIN_H4   -to pci_ad[18]
set_location_assignment PIN_H2   -to pci_ad[17]
set_location_assignment PIN_H1   -to pci_ad[16]
set_location_assignment PIN_J4   -to pci_ad[15]
set_location_assignment PIN_J2   -to pci_ad[14]
set_location_assignment PIN_N4   -to pci_ad[13]
set_location_assignment PIN_N3   -to pci_ad[12]
set_location_assignment PIN_N2   -to pci_ad[11]
set_location_assignment PIN_N1   -to pci_ad[10]
set_location_assignment PIN_P5   -to pci_ad[9]
set_location_assignment PIN_P2   -to pci_ad[8]
set_location_assignment PIN_P1   -to pci_ad[7]
set_location_assignment PIN_R5   -to pci_ad[6]
set_location_assignment PIN_R2   -to pci_ad[5]
set_location_assignment PIN_R1   -to pci_ad[4]
set_location_assignment PIN_T5   -to pci_ad[3]
set_location_assignment PIN_U3   -to pci_ad[2]              ;# Could be swapped with pci_ad[3]
set_location_assignment PIN_T3   -to pci_ad[1]
set_location_assignment PIN_T2   -to pci_ad[0]

set_location_assignment PIN_Y4   -to pci_cbe_[3]
set_location_assignment PIN_Y3   -to pci_cbe_[2]
set_location_assignment PIN_Y2   -to pci_cbe_[1]
set_location_assignment PIN_Y1   -to pci_cbe_[0]

set_location_assignment PIN_W1   -to pci_frame_
set_location_assignment PIN_U2   -to pci_par

;# Same pin as pci_ad[24]
;# set_location_assignment PIN_E2   -to pci_IDSEL

;#------------------------------------------------------------
;# NOR Flash
;#------------------------------------------------------------
;# A NOR flash can be soldered on the board, though it not present by default.
;#
;# The pinout below is for a 32Mbit Cypress S29JL032J in 8 bit mode.
;# Datasheet: https://www.cypress.com/file/217481/download

set_location_assignment PIN_Y13  -to flash_reset_

set_location_assignment PIN_Y18  -to flash_ce_
set_location_assignment PIN_Y19  -to flash_oe_
set_location_assignment PIN_Y20  -to flash_we_

set_location_assignment PIN_U13  -to flash_a[20]
set_location_assignment PIN_U10  -to flash_a[19]
set_location_assignment PIN_U9   -to flash_a[18]
set_location_assignment PIN_U8   -to flash_a[17]
set_location_assignment PIN_Y17  -to flash_a[16]
set_location_assignment PIN_AA17 -to flash_a[15]
set_location_assignment PIN_W16  -to flash_a[14]
set_location_assignment PIN_V15  -to flash_a[13]
set_location_assignment PIN_W15  -to flash_a[12]
set_location_assignment PIN_V14  -to flash_a[11]
set_location_assignment PIN_W14  -to flash_a[10]
set_location_assignment PIN_Y14  -to flash_a[9]
set_location_assignment PIN_AA14 -to flash_a[8]
set_location_assignment PIN_AB14 -to flash_a[7]
set_location_assignment PIN_AA13 -to flash_a[6]
set_location_assignment PIN_AB13 -to flash_a[5]
set_location_assignment PIN_AA12 -to flash_a[4]
set_location_assignment PIN_V11  -to flash_a[3]
set_location_assignment PIN_W11  -to flash_a[2]
set_location_assignment PIN_AA11 -to flash_a[1]
set_location_assignment PIN_AA10 -to flash_a[0]

set_location_assignment PIN_AB10 -to flash_dq[15]
set_location_assignment PIN_V9   -to flash_dq[14]
set_location_assignment PIN_W9   -to flash_dq[13]
set_location_assignment PIN_AA9  -to flash_dq[12]
set_location_assignment PIN_AB9  -to flash_dq[11]
set_location_assignment PIN_V8   -to flash_dq[10]
set_location_assignment PIN_W8   -to flash_dq[9]
set_location_assignment PIN_AA8  -to flash_dq[8]
set_location_assignment PIN_AB8  -to flash_dq[7]
set_location_assignment PIN_W7   -to flash_dq[6]
set_location_assignment PIN_AA7  -to flash_dq[5]
set_location_assignment PIN_AB7  -to flash_dq[4]
set_location_assignment PIN_Y5   -to flash_dq[3]
set_location_assignment PIN_Y6   -to flash_dq[2]
set_location_assignment PIN_AA6  -to flash_dq[1]
set_location_assignment PIN_AB6  -to flash_dq[0]

;#set_location_assignment PIN_xx        -to flash_wp_           ;# pulled up to high
;#set_location_assignment PIN_yy        -to flash_ry_by_        ;# not connected to FPGA
;#set_location_assignment PIN_zz        -to flash_byte_         ;# pulled up to high


;#------------------------------------------------------------
;# HWIC Connector
;#------------------------------------------------------------
set_location_assignment PIN_G21     -to hwic_3                  ;# Toggling output in Cisco router
set_location_assignment PIN_G22     -to hwic_11
set_location_assignment PIN_F21     -to hwic_45
set_location_assignment PIN_E21     -to hwic_12
set_location_assignment PIN_E22     -to hwic_46
set_location_assignment PIN_D21     -to hwic_13
set_location_assignment PIN_D22     -to hwic_47
set_location_assignment PIN_C21     -to hwic_14
set_location_assignment PIN_C22     -to hwic_48
set_location_assignment PIN_J22     -to hwic_15
set_location_assignment PIN_N22     -to hwic_50
set_location_assignment PIN_T22     -to hwic_18
set_location_assignment PIN_U21     -to hwic_52
set_location_assignment PIN_V21     -to hwic_19
set_location_assignment PIN_V22     -to hwic_53
set_location_assignment PIN_W21     -to hwic_20
set_location_assignment PIN_W22     -to hwic_54
set_location_assignment PIN_Y21     -to hwic_21
set_location_assignment PIN_Y22     -to hwic_55
set_location_assignment PIN_N21     -to hwic_56
set_location_assignment PIN_T21     -to hwic_25
set_location_assignment PIN_A12     -to hwic_32
set_location_assignment PIN_L18     -to hwic_67
