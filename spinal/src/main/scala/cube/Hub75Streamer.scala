
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

object Hub75Streamer {
    def getApb3Config() = Apb3Config(addressWidth = 4, dataWidth = 32)
}

class Hub75Streamer(conf: Hub75Config) extends Component {

    val io = new Bundle {
        val rgb               = master(Stream(Bits(4 bits)))

//        val led_mem_rd        = out(Bool)
//        val led_mem_rd_addr   = out(UInt(9 bits))
//        val led_mem_rd_data   = in(Bits(24 bits))
    }

    val col_cntr = Counter(conf.panel_cols, io.rgb.ready)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    val col_offset = Counter(conf.panel_cols * 2, row_cntr.willOverflow)
    val col_mul_row = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * row_cntr.value

    val r = UInt(8 bits) 
    r := ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))

    io.rgb.valid      := True
    io.rgb.payload(0) := (r >> (bit_cntr.value+(8-conf.bpc)))(0)
    io.rgb.payload(1) := False
    io.rgb.payload(2) := False
    io.rgb.payload(3) := col_cntr === 0 && row_cntr === 0 && bit_cntr === 0

}
