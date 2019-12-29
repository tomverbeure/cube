
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._


class Hub75Simple(oscSpeedMHz: Int, conf: Hub75Config) extends Component {

    def osc_clk_mhz   = oscSpeedMHz
    def refresh_rate  = 180        // frame per second

    val sclk          = (conf.panel_rows * conf.panel_cols / conf.pixels_per_clk)  * (1 << conf.bpc) * refresh_rate
    val clk_ratio     = osc_clk_mhz * 1000000 / sclk

    println(s"Desired sclk: $sclk")
    println(s"Clock ratio:  $clk_ratio")

    val io = new Bundle {
        val hub75           = out(Hub75Intfc(nr_row_bits = 3))
    }

    val clk_div_cntr  = Counter(clk_ratio, True)
    val col_cntr      = Counter(conf.panel_cols+3,   clk_div_cntr.willOverflow)
    val bin_dec_phase = Counter(1 << conf.bpc)
    val bit_cntr      = Counter(conf.bpc)
    val row_cntr      = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    val bin_dec_phase_max = UInt(conf.bpc bits)

    bin_dec_phase_max := ((U(1, 1 bits) << bit_cntr)-1).resize(conf.bpc)

    when(col_cntr.willOverflow){
        when(bin_dec_phase === bin_dec_phase_max){
            bit_cntr.increment()
            bin_dec_phase.clear()
        }
        .otherwise{
            bin_dec_phase.increment()
        }
    }

    val col_offset = Counter(conf.panel_cols * 2, row_cntr.willOverflow)
    val col_mul_row = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * row_cntr.value

    val r = UInt(8 bits) 
    //r := ((col_offset>>1) === col_cntr.value) ? U(0, 8 bits) | ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))
    r := ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))

    val col_active_phase = col_cntr.value < conf.panel_cols

    io.hub75.clk      := RegNext(bin_dec_phase === 0 &&  col_active_phase && (clk_div_cntr >= clk_ratio/2)) init(False)
    io.hub75.oe_      := RegNext(bin_dec_phase === 0 && !col_active_phase) init(True)
    io.hub75.lat      := RegNext(bin_dec_phase === 0 && col_cntr === conf.panel_cols+1) init(False)
    io.hub75.r0       := RegNext(bin_dec_phase === 0 &&  col_active_phase && (r >> (bit_cntr.value+(8-conf.bpc)))(0)) init(False)
    io.hub75.g0       := False
    io.hub75.b0       := False
    io.hub75.r1       := RegNext(bin_dec_phase === 0 &&  col_active_phase && (r >> (bit_cntr.value+(8-conf.bpc)))(0)) init(False)
    io.hub75.g1       := False
    io.hub75.b1       := False

    io.hub75.row      := RegNextWhen(row_cntr.value, col_cntr.willOverflow) init(0)

    /*
    val hubClkCntr = Reg(UInt(5 bits)) init(0)
    hubClkCntr := (hubClkCntr =/= 10) ? (hubClkCntr + 1) | 0

    val panel_cntr        = Reg(UInt(log2Up(conf.nr_panels) bits)) init(0)
    val panel_start_addr  = Reg(UInt(conf.ram_addr_bits bits)) init(0)

    val led_cntr          = Reg(UInt(log2Up(conf.panel_rows * conf.panel_cols)/conf.pixels_per_clk bits)) init(0)
    val phase_cntr        = Reg(UInt(log2Up(conf.pixels_per_clk) bits)) init(0)
    val bit_cntr          = Reg(UInt(log2Up(conf.bpc) bits)) init(0)

    object FsmState extends SpinalEnum {
        val Idle            = newElement()
        val PrepNewPanel    = newElement()
        val PrepNewLed      = newElement()
        val ReqLedVal       = newElement()
        val GetLedVal       = newElement()
    }

    val mem_valid = Reg(Bool) init(False)
    val mem_addr  = Reg(UInt(conf.ram_addr_bits bits)) init(0)

    io.led_mem_rd             := mem_valid
    io.led_mem_rd_addr        := mem_addr

    val cur_state = Reg(FsmState()) init(FsmState.Idle)

    val r = Reg(Bits(conf.pixels_per_clk bits))
    val g = Reg(Bits(conf.pixels_per_clk bits))
    val b = Reg(Bits(conf.pixels_per_clk bits))

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
            panel_start_addr    := panel_cntr * (conf.panel_rows * conf.panel_cols)

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
            g(phase_cntr)       := io.led_mem_rd_data(phase_cntr +     conf.bpc)
            b(phase_cntr)       := io.led_mem_rd_data(phase_cntr + 2 * conf.bpc)

            cur_state           := FsmState.ReqLedVal
        }
    }
    */

}



