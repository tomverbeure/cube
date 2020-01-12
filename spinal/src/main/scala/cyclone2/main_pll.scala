
package cyclone2

import spinal.core._

class main_pll() extends BlackBox {

    val io = new Bundle {
        val inclk0          = in(Bool)
        val c0              = out(Bool)
        val locked          = out(Bool)
    }

    noIoPrefix()
}


