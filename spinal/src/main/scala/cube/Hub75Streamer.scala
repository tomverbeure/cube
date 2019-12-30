
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
        val rgb               = master(Stream(Bits(7 bits)))

//        val led_mem_rd        = out(Bool)
//        val led_mem_rd_addr   = out(UInt(9 bits))
//        val led_mem_rd_data   = in(Bits(24 bits))
    }

    val output_fifo_wr = Stream(Bits(7 bits))

    val col_cntr = Counter(conf.panel_cols, output_fifo_wr.ready)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    val col_offset = Counter(conf.panel_cols * 2, row_cntr.willOverflow)
    val col_mul_row = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * row_cntr.value

    val r = UInt(8 bits) 
    r := ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))

    output_fifo_wr.valid      := True
    output_fifo_wr.payload(0) := (r >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(1) := False
    output_fifo_wr.payload(2) := False
    output_fifo_wr.payload(3) := (r >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(4) := False
    output_fifo_wr.payload(5) := False
    output_fifo_wr.payload(6) := col_cntr === 0 && row_cntr === 0 && bit_cntr === 0

    val u_output_fifo = StreamFifo(
                            dataType  = Bits(7 bits),
                            depth     = conf.panel_cols
                        )
    u_output_fifo.io.push   << output_fifo_wr
    u_output_fifo.io.pop    >> io.rgb


}
