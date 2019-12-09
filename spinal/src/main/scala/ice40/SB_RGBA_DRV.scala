

package ice40

import spinal.core._

class SB_RGBA_DRV(
        current_mode : String = "0b0",
        rgb0_current : String = "0b000000",
        rgb1_current : String = "0b000000",
        rgb2_current : String = "0b000000"
    ) extends BlackBox {

    val generic = new Generic {
        val CURRENT_MODE  = current_mode
        val RGB0_CURRENT  = rgb0_current
        val RGB1_CURRENT  = rgb1_current
        val RGB2_CURRENT  = rgb2_current
    }

    val io = new Bundle {
        val CURREN    = in(Bool)
        val RGBLEDEN  = in(Bool)
        val RGB0PWM   = in(Bool)
        val RGB1PWM   = in(Bool)
        val RGB2PWM   = in(Bool)

        val RGB0      = out(Bool)
        val RGB1      = out(Bool)
        val RGB2      = out(Bool)
    }

    noIoPrefix()
}


