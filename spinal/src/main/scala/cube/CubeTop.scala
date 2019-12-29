
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import ice40._

class CubeTop(isSim : Boolean = true) extends Component {

    val hub75Config = Hub75Config(
                        nr_panels     = 2, 
                        panel_rows    = 16, 
                        panel_cols    = if (isSim) 32 else 64, 
                        row_offset    = 0, 
                        bpc           = if (isSim) 4 else 7, 
                        ram_addr_bits = 10, 
                        ram_data_bits = 24
                      )
    
    val io = new Bundle {
        val clk25       = in(Bool)

        val hub75       = out(Hub75Intfc(hub75Config.nr_row_bits))

        val leds        = out(Bits(4 bits))
    }

    noIoPrefix()

    val osc_clk = io.clk25

    val oscClkRawDomain = ClockDomain(
        clock = osc_clk,
        frequency = FixedFrequency(25 MHz),
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

    //============================================================
    // General Logic
    //============================================================

    val debug_leds = new ClockingArea(oscClkDomain) {

        val led_counter = Reg(UInt(24 bits))
        led_counter := led_counter + 1

        io.leds(3) := led_counter.msb
    }

    val core = new ClockingArea(oscClkDomain) {

        val u_cpu = new CpuTop()

/*
        val led_stream = Stream(Bits(24 bits))

        val u_led_streamer = new LedStreamer()
        u_led_streamer.io.led_stream      <> led_stream
        u_led_streamer.io.led_mem_rd      <> u_cpu.io.led_mem_rd
        u_led_streamer.io.led_mem_rd_addr <> u_cpu.io.led_mem_rd_addr
        u_led_streamer.io.led_mem_rd_data <> u_cpu.io.led_mem_rd_data

        val led_streamer_apb_regs = u_led_streamer.driveFrom(Apb3SlaveFactory(u_cpu.io.led_streamer_apb), 0x0)
*/

        val u_hub75drv = new Hub75Simple(if (isSim) 2 else 25, hub75Config)
        io.hub75 <> u_hub75drv.io.hub75

    }


    val leds = new Area {
        io.leds(2) := False
        io.leds(1) := core.u_cpu.io.led_green
        io.leds(0) := core.u_cpu.io.led_blue
    }


}


//Generate the MyTopLevel's Verilog
object CubeTopVerilogSim {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new CubeTop(isSim = true))
    }
}

object CubeTopVerilogSyn {
    def main(args: Array[String]) {

        val config = SpinalConfig(anonymSignalUniqueness = true)
        config.generateVerilog(new CubeTop(isSim = false))
    }
}


//Define a custom SpinalHDL configuration with synchronous reset instead of the default asynchronous one. This configuration can be resued everywhere
object MySpinalConfig extends SpinalConfig(defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC))


//Generate the MyTopLevel's Verilog using the above custom configuration.
object CubeTopVerilogWithCustomConfig {
    def main(args: Array[String]) {
        MySpinalConfig.generateVerilog(new CubeTop)
    }
}
