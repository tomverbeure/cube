
package cube

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import cyclone2._

class CubeTop(isSim : Boolean = true) extends Component {

    val panels = ArrayBuffer[PanelInfo]()

    if (!isSim){
        panels += PanelInfo(-1, 1,-1,       5, true,    0,     0,-1, 1)
        panels += PanelInfo(-1, 0,-1,       5, false,   0,     0,-1, 1)

        // top face
        panels += PanelInfo(-1, 1,-1,       4, true,    0,     1, 0, 1)
        panels += PanelInfo(-1, 1, 0,       4, false,   0,     1, 0, 1)

        // bottom face
        panels += PanelInfo(-1,-1, 1,       3, true,    0,     1, 0,-1)
        panels += PanelInfo(-1,-1, 0,       3, false,   0,     1, 0,-1)

        panels += PanelInfo( 1, 1,-1,       2, true,    0,    -1,-1, 0)
        panels += PanelInfo( 1, 0,-1,       2, false,   0,    -1,-1, 0)

        panels += PanelInfo( 1, 1, 1,       1, true,    90,   0,-1,-1)
        panels += PanelInfo( 1, 0, 1,       1, false,   90,   0,-1,-1)
    }


    //                              Side   Top      Rot 
    panels += PanelInfo(-1, 1, 1,     0,   true,    180,    1,-1, 0)
    panels += PanelInfo(-1, 0, 1,     0,   true,    0,      1,-1, 0)

    val hub75Config = Hub75Config(
                        panel_rows    = 16,
                        panel_cols    = 32,
                        bpc           = if (isSim) 4 else 6,
                        panels        = panels.toArray
                      )

    val ledMemConfig = LedMemConfig(memWords = 2 * 6 * 32 * 32, bpc = 6)

    val io = new Bundle {
        val clk25       = in(Bool)

        val hub75       = out(Hub75Intfc(hub75Config.nr_row_bits))

        val leds        = out(Bits(4 bits))
    }

    noIoPrefix()

    val main_clk = Bool
    val main_clk_speed = if (isSim) 2 MHz else 50 MHz

    val osc_src = if (isSim) new Area {
        main_clk    := io.clk25
    }
    else new Area {
        val u_main_clk = new main_pll()
        u_main_clk.io.inclk0  <> io.clk25
        u_main_clk.io.c0      <> main_clk
    }

    val mainClkRawDomain = ClockDomain(
        clock = main_clk,
        frequency = FixedFrequency(main_clk_speed),
        config = ClockDomainConfig(
                    resetKind = BOOT
        )
    )

    //============================================================
    // Create main clock reset
    //============================================================
    val main_reset_ = Bool

    val main_reset_gen = new ClockingArea(mainClkRawDomain) {
        val reset_unbuffered_ = True

        val reset_cntr = Reg(UInt(5 bits)) init(0)
        when(reset_cntr =/= U(reset_cntr.range -> true)){
            reset_cntr := reset_cntr + 1
            reset_unbuffered_ := False
        }

        main_reset_ := RegNext(reset_unbuffered_)
    }


    val mainClkDomain = ClockDomain(
        clock = main_clk,
        reset = main_reset_,
        config = ClockDomainConfig(
            resetKind = SYNC,
            resetActiveLevel = LOW
        )
    )

    //============================================================
    // General Logic
    //============================================================

    val debug_leds = new ClockingArea(mainClkDomain) {

        val led_counter = Reg(UInt(24 bits))
        led_counter := led_counter + 1

        io.leds(3) := led_counter.msb
    }

    val core = new ClockingArea(mainClkDomain) {

        val u_cpu = new CpuTop()

        //============================================================
        // LED memory
        //============================================================

        val u_led_mem = new LedMem(ledMemConfig, isSim)
        u_led_mem.io.led_mem_b_wr       := False
        u_led_mem.io.led_mem_b_wr_data  := 0

        val led_mem_apb_regs = u_led_mem.driveFrom(Apb3SlaveFactory(u_cpu.io.led_mem_apb), 0x0)

        //============================================================
        // HUB75 Streamer
        //============================================================

        val u_hub75_streamer = new Hub75Streamer(hub75Config, ledMemConfig)
        u_hub75_streamer.io.led_mem_rd        <> u_led_mem.io.led_mem_b_req
        u_hub75_streamer.io.led_mem_rd_addr   <> u_led_mem.io.led_mem_b_addr
        u_hub75_streamer.io.led_mem_rd_data   <> u_led_mem.io.led_mem_b_rd_data

        val hub75_streamer_regs = u_hub75_streamer.driveFrom(Apb3SlaveFactory(u_cpu.io.hub75_streamer_apb), 0x0)

        //============================================================
        // HUB75 Phy
        //============================================================

        val u_hub75phy = new Hub75Phy(main_clk_speed, hub75Config)
        u_hub75phy.io.rgb   <> u_hub75_streamer.io.rgb
        u_hub75phy.io.hub75 <> io.hub75

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
