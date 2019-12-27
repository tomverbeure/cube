
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import ice40._

class CubeShim(internalOsc : Boolean = true) extends Component {

    val io = new Bundle {
        val clk25       = in(Bool)
        val hwic_3      = out(Bool)
        val hwic_11     = out(Bool)
        val hwic_13     = out(Bool)
        val hwic_15     = out(Bool)
        val hwic_19     = out(Bool)
        val hwic_21     = out(Bool)
        val hwic_25     = out(Bool)

        val leds        = out(Bits(4 bits))
    }

    noIoPrefix()

    val cubeTop = new CubeTop(false)
    cubeTop.io.clk25        <> io.clk25
    cubeTop.io.leds         <> io.leds
    cubeTop.io.hub75.clk    <> io.hwic_11
    cubeTop.io.hub75.lat    <> io.hwic_13
    cubeTop.io.hub75.oe_    <> io.hwic_3
    cubeTop.io.hub75.row(0) <> io.hwic_19
    cubeTop.io.hub75.row(1) <> io.hwic_21
    cubeTop.io.hub75.row(2) <> io.hwic_15
    cubeTop.io.hub75.r0     <> io.hwic_25
}


//Generate the MyTopLevel's Verilog
object CubeShimVerilogSim {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new CubeShim(internalOsc = false))
    }
}

object CubeShimVerilogSyn {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new CubeShim(internalOsc = true))
    }
}


//Define a custom SpinalHDL configuration with synchronous reset instead of the default asynchronous one. This configuration can be resued everywhere
object CubeShimSpinalConfig extends SpinalConfig(defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC))


//Generate the MyTopLevel's Verilog using the above custom configuration.
object CubeShimVerilogWithCustomConfig {
    def main(args: Array[String]) {
        CubeShimSpinalConfig.generateVerilog(new CubeShim)
    }
}
