

package ice40

import spinal.core._

class SB_I2C(
        i2c_slave_init_addr : String = "0b1111100001",
        bus_addr74          : String = "0b0001"
    ) extends BlackBox {

    val generic = new Generic {
        val I2C_SLAVE_INIT_ADDR = i2c_slave_init_addr
        val BUS_ADDR74          = bus_addr74
    }

    val io = new Bundle {
        val SBCLKI    = in(Bool)
        val SBRWI     = in(Bool)
        val SBSTBI    = in(Bool)
        val SBADRI7   = in(Bool)
        val SBADRI6   = in(Bool)
        val SBADRI5   = in(Bool)
        val SBADRI4   = in(Bool)
        val SBADRI3   = in(Bool)
        val SBADRI2   = in(Bool)
        val SBADRI1   = in(Bool)
        val SBADRI0   = in(Bool)
        val SBDATI7   = in(Bool)
        val SBDATI6   = in(Bool)
        val SBDATI5   = in(Bool)
        val SBDATI4   = in(Bool)
        val SBDATI3   = in(Bool)
        val SBDATI2   = in(Bool)
        val SBDATI1   = in(Bool)
        val SBDATI0   = in(Bool)
        val SCLI      = in(Bool)
        val SDAI      = in(Bool)

        val SBDATO7   = out(Bool)
        val SBDATO6   = out(Bool)
        val SBDATO5   = out(Bool)
        val SBDATO4   = out(Bool)
        val SBDATO3   = out(Bool)
        val SBDATO2   = out(Bool)
        val SBDATO1   = out(Bool)
        val SBDATO0   = out(Bool)
        val SBACKO    = out(Bool)
        val I2CIRQ    = out(Bool)
        val I2CWKUP   = out(Bool)
        val SCLO      = out(Bool)
        val SCLOE     = out(Bool)
        val SDAO      = out(Bool)
        val SDAOE     = out(Bool)
    }

    noIoPrefix()
}


