
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

object Hub75Streamer {
    def getApb3Config() = Apb3Config(addressWidth = 4, dataWidth = 32)
}



class Hub75Streamer(conf: Hub75Config, ledMemConf: LedMemConfig) extends Component {

    val io = new Bundle {
        val rgb               = master(Stream(Bits(7 bits)))

        val led_mem_rd        = out(Bool)
        val led_mem_rd_addr   = out(UInt(ledMemConf.addrBits bits))
        val led_mem_rd_data   = in(Bits(ledMemConf.dataBits bits))
    }

    val output_fifo_wr = Stream(Bits(7 bits))

    val col_cntr = Counter(conf.panel_cols, output_fifo_wr.ready)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    val col_offset = Counter(conf.panel_cols * 2, row_cntr.willOverflow)
    val col_mul_row_0 = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * row_cntr.value
    val col_mul_row_1 = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * (row_cntr.value + 8)

    val r0 = UInt(8 bits) 
    val r1 = UInt(8 bits) 
    r0 := ((col_mul_row_0)(col_mul_row_0.getWidth-1 downto col_mul_row_0.getWidth-8))
    r1 := ((col_mul_row_1)(col_mul_row_1.getWidth-1 downto col_mul_row_1.getWidth-8))

    val g0 = UInt(8 bits)
    val g1 = UInt(8 bits)
    g0 := U(255, 8 bits) - (row_cntr.value << 4).resize(g0.getWidth)
    g1 := U(255, 8 bits) - ((U(8, 4 bits) + (row_cntr.value)) << 4)

    val b0 = UInt(8 bits)
    val b1 = UInt(8 bits)
    b0 := (col_offset<<1).resize(b0.getWidth)
    b1 := (col_offset<<1).resize(b1.getWidth)

    output_fifo_wr.valid      := True
    output_fifo_wr.payload(0) := (r0 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(1) := (g0 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(2) := (b0 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(3) := (r1 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(4) := (g1 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(5) := (b1 >> (bit_cntr.value+(8-conf.bpc)))(0)
    output_fifo_wr.payload(6) := col_cntr === 0 && row_cntr === 0 && bit_cntr === 0

    val u_output_fifo = StreamFifo(
                            dataType  = Bits(7 bits),
                            depth     = conf.panel_cols
                        )
    u_output_fifo.io.push   << output_fifo_wr
    u_output_fifo.io.pop    >> io.rgb

    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {
//        val start = busCtrl.createReadAndWrite(io.start, 0x0) init(False)
//        val active = busCtrl.createReadOnly(io.active, 0x4)
//
//        io.start := start
//        active := io.active
    }

}
