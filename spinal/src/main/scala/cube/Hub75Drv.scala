
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

case class Hub75Config(
              nr_panels       : Int, 
              panel_rows      : Int, 
              panel_cols      : Int,
              nr_row_bits     : Int, 
              pixels_per_clk  : Int, 
              row_offset      : Int, 
              bpc             : Int,
              ram_addr_bits   : Int,
              ram_data_bits   : Int
  ) 
{
    def row_nr_bits = log2Up(panel_rows / pixels_per_clk)
}


case class Hub75(c: Hub75Config) extends Bundle {
    val clk         = Bool
    val lat         = Bool
    val oe_         = Bool
    val row         = UInt(c.row_nr_bits bits)
    val r           = Bits(c.pixels_per_clk bits)
    val g           = Bits(c.pixels_per_clk bits)
    val b           = Bits(c.pixels_per_clk bits)
}

class Hub75Drv(hub75Config: Hub75Config) extends Component {

    def osc_clk_mhz   = 12

    val ledMemBusConfig = PipelinedMemoryBusConfig(
        addressWidth  = hub75Config.ram_addr_bits, 
        dataWidth     = hub75Config.ram_data_bits
    )

    val io = new Bundle {
        val hub75           = out(Hub75(hub75Config))
        val led_mem_rd      = out(Bool)
        val led_mem_rd_addr = out(UInt(hub75Config.ram_addr_bits bits))
        val led_mem_rd_data = in(Bits(hub75Config.ram_data_bits bits))
    }

    val hubClkCntr = Reg(UInt(5 bits)) init(0)
    hubClkCntr := (hubClkCntr =/= 10) ? (hubClkCntr + 1) | 0


    val panel_cntr        = Reg(UInt(log2Up(hub75Config.nr_panels) bits)) init(0)
    val panel_start_addr  = Reg(UInt(hub75Config.ram_addr_bits bits)) init(0)

    val led_cntr          = Reg(UInt(log2Up(hub75Config.panel_rows * hub75Config.panel_cols)/hub75Config.pixels_per_clk bits)) init(0)
    val phase_cntr        = Reg(UInt(log2Up(hub75Config.pixels_per_clk) bits)) init(0)
    val bit_cntr          = Reg(UInt(log2Up(hub75Config.bpc) bits)) init(0)

    object FsmState extends SpinalEnum {
        val Idle            = newElement()
        val PrepNewPanel    = newElement()
        val PrepNewLed      = newElement()
        val ReqLedVal       = newElement()
        val GetLedVal       = newElement()
    }

    val mem_valid = Reg(Bool) init(False)
    val mem_addr  = Reg(UInt(hub75Config.ram_addr_bits bits)) init(0)

    io.led_mem_rd             := mem_valid
    io.led_mem_rd_addr        := mem_addr

    val cur_state = Reg(FsmState()) init(FsmState.Idle)

    val r = Reg(Bits(hub75Config.pixels_per_clk bits))
    val g = Reg(Bits(hub75Config.pixels_per_clk bits))
    val b = Reg(Bits(hub75Config.pixels_per_clk bits))

    switch(cur_state){
        is(FsmState.Idle){
            when(True){

                led_cntr    := 0
                panel_cntr  := 0
                phase_cntr  := 0
                bit_cntr    := 0

                cur_state   := FsmState.PrepNewPanel
            }
        }
        is(FsmState.PrepNewPanel){
            panel_start_addr    := panel_cntr * (hub75Config.panel_rows * hub75Config.panel_cols)

            cur_state           := FsmState.PrepNewLed
        }
        is(FsmState.PrepNewLed){
            mem_addr            := panel_start_addr + led_cntr
            phase_cntr          := 0

            cur_state           := FsmState.ReqLedVal
        }
        is(FsmState.ReqLedVal){
            mem_valid           := True

            cur_state           := FsmState.GetLedVal
        }
        is(FsmState.GetLedVal){
            r(phase_cntr)       := io.led_mem_rd_data(phase_cntr)
            g(phase_cntr)       := io.led_mem_rd_data(phase_cntr +     hub75Config.bpc)
            b(phase_cntr)       := io.led_mem_rd_data(phase_cntr + 2 * hub75Config.bpc)

            cur_state           := FsmState.ReqLedVal
        }
    }

}




