
package cyclone2

import spinal.core._

class led_ram extends BlackBox {

    val io = new Bundle {
    	val clock_a         = in(Bool)
    	val address_a       = in(UInt(14 bits))
    	val wren_a          = in(Bool)
    	val data_a          = in(Bits(18 bits))
    	val q_a             = out(Bits(18 bits))
    
    	val clock_b         = in(Bool)
    	val address_b       = in(UInt(14 bits))
    	val wren_b          = in(Bool)
    	val data_b          = in(Bits(18 bits))
    	val q_b             = out(Bits(18 bits))
    }

    noIoPrefix()

    //addRTLPath("./quartus/altera_models/cpu_ram/cpu_ram_bb.v")
}

