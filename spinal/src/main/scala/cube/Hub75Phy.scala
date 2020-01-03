
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._


class Hub75Phy(oscSpeedMHz: Int, conf: Hub75Config) extends Component {

    def osc_clk_mhz   = oscSpeedMHz
    def refresh_rate  = 120        // frame per second

    val sclk          = (conf.panels.size * conf.panel_rows * conf.panel_cols / conf.pixels_per_clk)  * (1 << conf.bpc) * refresh_rate
    val clk_ratio     = osc_clk_mhz * 1000000 / sclk

    println(s"Desired sclk: $sclk")
    println(s"Clock ratio:  $clk_ratio")

    val io = new Bundle {
        val rgb             = slave(Stream(Bits(7 bits)))
        val hub75           = out(Hub75Intfc(nr_row_bits = 3))
    }

    val clk_div_cntr  = Counter(clk_ratio, True)
    val col_cntr      = Counter((conf.panels.size * conf.panel_cols)+3,   clk_div_cntr.willOverflow)
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

    val col_active_phase = col_cntr.value < (conf.panels.size * conf.panel_cols)

    val need_data = bin_dec_phase === 0 && col_active_phase && clk_div_cntr === 0
    val need_sof  = (col_cntr === 0 && row_cntr === 0 && bit_cntr === 0 && bin_dec_phase === 0)

    val force_clear_no_data = False
    val force_clear_desync = False

    io.rgb.ready   := False
    when(need_data){
        when(!io.rgb.valid){
            force_clear_no_data := True
        }
        .elsewhen(io.rgb.valid && ((io.rgb.payload(6) && !need_sof) || (!io.rgb.payload(6) && need_sof) )){
            force_clear_desync := True
        }
        .otherwise{
            io.rgb.ready   := True
        }
    }

    when(force_clear_no_data || force_clear_desync){
        clk_div_cntr.clear()
        col_cntr.clear()
        bin_dec_phase.clear()
        bit_cntr.clear()
        row_cntr.clear()
    }

    io.hub75.clk      := RegNext(bin_dec_phase === 0 &&  col_active_phase && (clk_div_cntr >= clk_ratio/2)) init(False)
    io.hub75.oe_      := RegNext(bin_dec_phase === 0 && !col_active_phase) init(True)
    io.hub75.lat      := RegNext(bin_dec_phase === 0 && col_cntr === (conf.panels.size * conf.panel_cols)+1) init(False)

    io.hub75.r0       := RegNextWhen(io.rgb.payload(0), io.rgb.valid && io.rgb.ready)
    io.hub75.g0       := RegNextWhen(io.rgb.payload(1), io.rgb.valid && io.rgb.ready)
    io.hub75.b0       := RegNextWhen(io.rgb.payload(2), io.rgb.valid && io.rgb.ready)
    io.hub75.r1       := RegNextWhen(io.rgb.payload(3), io.rgb.valid && io.rgb.ready)
    io.hub75.g1       := RegNextWhen(io.rgb.payload(4), io.rgb.valid && io.rgb.ready)
    io.hub75.b1       := RegNextWhen(io.rgb.payload(5), io.rgb.valid && io.rgb.ready)

    io.hub75.row      := RegNextWhen(row_cntr.value, col_cntr.willOverflow) init(0)

}




