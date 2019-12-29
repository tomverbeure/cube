
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._


class Hub75Simple(oscSpeedMHz: Int, hub75Config: Hub75Config) extends Component {

    def osc_clk_mhz   = oscSpeedMHz
    def refresh_rate  = 60        // frame per second

    val sclk          = (hub75Config.panel_rows * hub75Config.panel_cols / hub75Config.pixels_per_clk)  * (1 << hub75Config.bpc) * refresh_rate
    val clk_ratio     = osc_clk_mhz * 1000000 / sclk

    println(s"Desired sclk: $sclk")
    println(s"Clock ratio:  $clk_ratio")

    val io = new Bundle {
        val hub75           = out(Hub75Intfc(nr_row_bits = 3))
    }

    val clk_div_cntr  = Counter(clk_ratio, True)
    val col_cntr      = Counter(hub75Config.panel_cols,     clk_div_cntr.willOverflow)
    val row_cntr      = Counter(hub75Config.panel_rows/2)
    val bit_cntr      = Counter(hub75Config.bpc,            row_cntr.willOverflow)

    val bin_dec_phase = Counter(1 << hub75Config.bpc)
    val bin_dec_phase_max = UInt(hub75Config.bpc bits)

    bin_dec_phase_max := ((U(1, 1 bits) << bit_cntr)-1).resize(hub75Config.bpc)

    when(col_cntr.willOverflow){
        when(bin_dec_phase === bin_dec_phase_max){
            row_cntr.increment()
            bin_dec_phase.clear()
        }
        .otherwise{
            bin_dec_phase.increment()
        }
    }

    val lat = Reg(Bool)
    lat := (clk_div_cntr.willOverflow      ? (col_cntr.willOverflowIfInc && bin_dec_phase === 0) | 
           ((clk_div_cntr === clk_ratio/4) ? False                                               |
                                             lat))

    val col_offset = Counter(hub75Config.panel_cols * 2, bit_cntr.willOverflow)
    val col_mul_row = col_cntr.value * row_cntr.value

    val r = UInt(8 bits) 
    r := ((col_offset>>1) === col_cntr.value) ? U(0, 8 bits) | ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))


    io.hub75.clk      := RegNext((clk_div_cntr >= clk_ratio/2) && bin_dec_phase === 0)
    io.hub75.lat      := lat
    io.hub75.oe_      := lat
    io.hub75.row      := RegNextWhen(row_cntr.value, col_cntr.willOverflow)
    io.hub75.r0       := RegNext((r >> (bit_cntr.value+(8-hub75Config.bpc)))(0))
    io.hub75.g0       := False
    io.hub75.b0       := False
    io.hub75.r1       := RegNext((r >> (bit_cntr.value+(8-hub75Config.bpc)))(0))
    io.hub75.g1       := False
    io.hub75.b1       := False


    /*
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
    */

}




