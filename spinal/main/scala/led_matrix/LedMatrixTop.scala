
package led_matrix

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import ice40._

class LedMatrixTop(internalOsc : Boolean = true) extends Component {
    val io = new Bundle {
        val OSC_CLK_IN  = in(Bool)

        val MATRIX_DIN  = out(Bool)

        val LED_R_   = out(Bool)
        val LED_G_   = out(Bool)
        val LED_B_   = out(Bool)
    }

    noIoPrefix()

    val osc_clk = Bool

    val osc_src = if (internalOsc) new Area {
        val u_osc = new SB_HFOSC(clkhf_div = "0b10")
        u_osc.io.CLKHFPU    <> True
        u_osc.io.CLKHFEN    <> True
        u_osc.io.CLKHF      <> osc_clk
    }
    else new Area {
        osc_clk := io.OSC_CLK_IN
    }

    val oscClkRawDomain = ClockDomain(
        clock = osc_clk,
        frequency = FixedFrequency(12 MHz),
        config = ClockDomainConfig(
                    resetKind = BOOT
        )
    )

    //============================================================
    // Create osc clock reset
    //============================================================
    val osc_reset_ = Bool

    val osc_reset_gen = new ClockingArea(oscClkRawDomain) {
        val reset_unbuffered_ = True

        val reset_cntr = Reg(UInt(5 bits)) init(0)
        when(reset_cntr =/= U(reset_cntr.range -> true)){
            reset_cntr := reset_cntr + 1
            reset_unbuffered_ := False
        }

        osc_reset_ := RegNext(reset_unbuffered_)
    }


    val oscClkDomain = ClockDomain(
        clock = osc_clk,
        reset = osc_reset_,
        config = ClockDomainConfig(
            resetKind = SYNC,
            resetActiveLevel = LOW
        )
    )

    val led_red = Bool

    val core = new ClockingArea(oscClkDomain) {

        val led_counter = Reg(UInt(24 bits))
        led_counter := led_counter + 1

        led_red := led_counter.msb

        val u_cpu = new CpuTop()

        val led_stream = Stream(Bits(24 bits))

        val u_matrix_driver = new WS2812Drv()
        u_matrix_driver.io.led_din      <> io.MATRIX_DIN
        u_matrix_driver.io.led_stream   <> led_stream


        val u_led_streamer = new LedStreamer()
        u_led_streamer.io.led_stream      <> led_stream
        u_led_streamer.io.led_mem_rd      <> u_cpu.io.led_mem_rd
        u_led_streamer.io.led_mem_rd_addr <> u_cpu.io.led_mem_rd_addr
        u_led_streamer.io.led_mem_rd_data <> u_cpu.io.led_mem_rd_data

        val led_streamer_apb_regs = u_led_streamer.driveFrom(Apb3SlaveFactory(u_cpu.io.led_streamer_apb), 0x0)
    }


    val leds = new Area {
        io.LED_R_ := ~led_red
        io.LED_G_ := ~core.u_cpu.io.led_green
        io.LED_B_ := ~core.u_cpu.io.led_blue
    }

}


//Generate the MyTopLevel's Verilog
object LedMatrixTopVerilogSim {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new LedMatrixTop(internalOsc = false))
    }
}

object LedMatrixTopVerilogSyn {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new LedMatrixTop(internalOsc = true))
    }
}


//Define a custom SpinalHDL configuration with synchronous reset instead of the default asynchronous one. This configuration can be resued everywhere
object MySpinalConfig extends SpinalConfig(defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC))


//Generate the MyTopLevel's Verilog using the above custom configuration.
object LedMatrixTopVerilogWithCustomConfig {
    def main(args: Array[String]) {
        MySpinalConfig.generateVerilog(new LedMatrixTop)
    }
}
