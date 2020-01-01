
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import spinal.lib.bus.misc.BusSlaveFactory
import spinal.lib.graphic.{RgbConfig, Rgb}

import cc._

class Hub75Streamer(conf: Hub75Config, rgbConfig: RgbConfig) extends Component {

    val io = new Bundle {
        val softReset         = in(Bool()) default(False)
        val frameStart        = out(Bool)

        val pixels            = slave(Stream(Rgb(rgbConfig))

        val rgb               = master(Stream(Bits(7 bits)))

        val error             = out(Bool)
    }

    val output_fifo_wr = Stream(Bits(7 bits))

    //============================================================
    // Pixel fetching counters
    //============================================================
    val col_cntr = Counter(conf.panel_cols)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)

    //============================================================
    // Generic interface to receive pixels from DMA block
    //============================================================
    def feedWith(that: Stream[Fragment[Rgb]], resync : Bool = False): Unit = {
        val error = RegInit(False)
        val waitStartOfFrame = RegInit(False)

        io.pixels << that.toStreamOfFragment.throwWhen(error).haltWhen(waitStartOfFrame)

        when(io.frameStart){
            waitStartOfFrame  := False
        }
        when(that.fire && that.last){
            error := False
            waitStartOfFrame := error
        }
        when(!waitStartOfFrame && !error){
            when(io.error || resync){
                error := True
            }
        }
    }

    val fetch_ongoing = Reg(Bool) init(False)
    val fetch_phase   = Reg(Bool) init(False)

    val PENABLE     = Bool
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
