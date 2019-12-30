
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import cc._

object Hub75Streamer {
    def getApb3Config() = Apb3Config(addressWidth = 4, dataWidth = 32)
}

class Hub75Streamer(conf: Hub75Config) extends Component {

    val io = new Bundle {
        val dmaApb            = master(Apb3(CpuComplexConfig.default.dmaApbConfig))
        val rgb               = master(Stream(Bits(7 bits)))
    }

    val output_fifo_wr = Stream(Bits(7 bits))

    val col_cntr = Counter(conf.panel_cols)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    val fetch_ongoing = Reg(Bool) init(False)
    val fetch_phase   = Reg(Bool) init(False)

    val PENABLE     = RegInit(False)
    val PADDR       = Reg(UInt(io.dmaApb.PADDR.getWidth bits)) init(0)

    io.dmaApb.PENABLE   := PENABLE
    io.dmaApb.PADDR     := PADDR
    io.dmaApb.PSEL      := (default -> True)
    io.dmaApb.PWRITE    := False
    io.dmaApb.PWDATA    := 0

    val bytes_per_pixel = 4

    val r0 = Reg(Bool)
    val g0 = Reg(Bool)
    val b0 = Reg(Bool)

    val r1 = Reg(Bool)
    val g1 = Reg(Bool)
    val b1 = Reg(Bool)

    val rgb_valid = RegInit(False)


    rgb_valid := False
    when(!fetch_ongoing && output_fifo_wr.ready){
        PENABLE       := True
        PADDR         := (U(0x4000, io.dmaApb.PADDR.getWidth bits)
                          + (fetch_phase ? U(conf.panel_cols * conf.panel_rows/2 * bytes_per_pixel) | U(0))
                          + row_cntr * conf.panel_cols * bytes_per_pixel
                          + col_cntr * bytes_per_pixel)

        fetch_ongoing := True
    }
    .elsewhen(io.dmaApb.PREADY){
        when(!fetch_phase){
            PADDR         := (U(0x4000, io.dmaApb.PADDR.getWidth bits)
                              + (fetch_phase ? U(conf.panel_cols * conf.panel_rows/2 * bytes_per_pixel) | U(0)) 
                              + row_cntr * conf.panel_cols * bytes_per_pixel
                              + col_cntr * bytes_per_pixel)

            r0 := (io.dmaApb.PRDATA( 7 downto  0)  >> (bit_cntr.value+(8-conf.bpc)))(0)
            g0 := (io.dmaApb.PRDATA(15 downto  8)  >> (bit_cntr.value+(8-conf.bpc)))(0)
            b0 := (io.dmaApb.PRDATA(23 downto  16) >> (bit_cntr.value+(8-conf.bpc)))(0)

            fetch_phase   := True
        }
        .otherwise{
            r1 := (io.dmaApb.PRDATA( 7 downto  0)  >> (bit_cntr.value+(8-conf.bpc)))(0)
            g1 := (io.dmaApb.PRDATA(15 downto  8)  >> (bit_cntr.value+(8-conf.bpc)))(0)
            b1 := (io.dmaApb.PRDATA(23 downto  16) >> (bit_cntr.value+(8-conf.bpc)))(0)

            fetch_phase   := False
            PENABLE       := False
            fetch_ongoing := False

            rgb_valid     := True

            col_cntr.increment()
        }
    }

    val col_offset = Counter(conf.panel_cols * 2, row_cntr.willOverflow)
    val col_mul_row = (col_cntr.value.resize(log2Up(conf.panel_cols)) + (col_offset).resize(log2Up(conf.panel_cols))) * row_cntr.value

    val r = UInt(8 bits) 
    r := ((col_mul_row)(col_mul_row.getWidth-1 downto col_mul_row.getWidth-8))

    output_fifo_wr.valid      := rgb_valid
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
