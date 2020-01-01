
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import spinal.lib.bus.misc.BusSlaveFactory
import spinal.lib.graphic.{RgbConfig, Rgb}

import cc._

case class Hub75DmaConfig(
              addressWidth        : Int,
              dataWidth           : Int,
              beatPerAccess       : Int,
              sizeWidth           : Int,
              pendingRequestMax   : Int,
              fifoSize            : Int,
              pixelSize           : Int
              )
{
    def getAxi4ReadOnlyConfig = Axi4Config(
              addressWidth        = addressWidth + log2Up(dataWidth/8) + log2Up(beatPerAccess),
              dataWidth           = dataWidth,
              useId               = false,
              useRegion           = false,
              useBurst            = false,
              useLock             = false,
              useQos              = false,
              useResp             = false
              )
}

case class Hub75DmaMem(c: Hub75DmaConfig) extends Bundle with IMasterSlave {

    val cmd     = Stream(UInt(c.addressWidth bits))
    val rsp     = Flow Fragment(Bits(c.dataWidth bits))

    override def asMaster(): Unit = {
        master(cmd)
        slave(rsp)
    }

    def toAxi4ReadOnly : Axi4ReadOnly = {
        val ret = Axi4ReadOnly(c.getAxi4ReadOnlyConfig)

        ret.readCmd.valid     := this.cmd.valid
        ret.readCmd.addr      := this.cmd.payload << log2Up(c.dataWidth/8) + log2Up(c.beatPerAccess)
        ret.readCmd.prot      := "010"
        ret.readCmd.cache     := "1111"
        ret.readCmd.len       := c.beatPerAccess-1
        ret.readCmd.size      := log2Up(c.dataWidth/8)
        this.cmd.ready        := ret.readCmd.ready

        this.rsp.valid        := ret.readRsp.valid
        this.rsp.last         := ret.readRsp.last
        this.rsp.fragment     := ret.readRsp.data
        ret.readRsp.ready     := True

        ret
    }
    
}

case class Hub75Dma(c: Hub75DmaConfig) extends Commponent {

    import c._
    require(dataWidth >= pixelSize)

    val io = new Bundle{
        val star
    }
}

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

    object FsmState extends SpinalEnum{
        val Idle              = newElement()
        val RGB0ApbSetup      = newElement()
        val RGB0ApbWaitReady  = newElement()
        val RGB1ApbSetup      = newElement()
        val RGB1ApbWaitReady  = newElement()
    }

    val cur_state = Reg(FsmState()) init(FsmState.Idle)

    rgb_valid := False
    PENABLE   := False
    switch(cur_state){
        is(FsmState.Idle){
            when(True){
                PENABLE     := False

                PADDR       := (U(0x4000, io.dmaApb.PADDR.getWidth bits)
                                + row_cntr * conf.panel_cols * bytes_per_pixel
                                + col_cntr * bytes_per_pixel)

                cur_state   := FsmState.RGB0ApbSetup
            }
        }
        is(FsmState.RGB0ApbSetup){
            PENABLE         := False
            cur_state       := FsmState.RGB0ApbWaitReady
        }
        is(FsmState.RGB0ApbWaitReady){
            PENABLE         := True

            when(io.dmaApb.PREADY){
                r0 := (io.dmaApb.PRDATA( 7 downto  0)  >> (bit_cntr.value+(8-conf.bpc)))(0)
                g0 := (io.dmaApb.PRDATA(15 downto  8)  >> (bit_cntr.value+(8-conf.bpc)))(0)
                b0 := (io.dmaApb.PRDATA(23 downto  16) >> (bit_cntr.value+(8-conf.bpc)))(0)

                PADDR       := PADDR + U(conf.panel_cols * conf.panel_rows/2 * bytes_per_pixel)
                cur_state   := FsmState.RGB1ApbSetup
            }
        }
        is(FsmState.RGB1ApbSetup){
            PENABLE         := False
            cur_state       := FsmState.RGB1ApbWaitReady
        }
        is(FsmState.RGB1ApbWaitReady){
            PENABLE         := True

            when(io.dmaApb.PREADY){
                r1 := (io.dmaApb.PRDATA( 7 downto  0)  >> (bit_cntr.value+(8-conf.bpc)))(0)
                g1 := (io.dmaApb.PRDATA(15 downto  8)  >> (bit_cntr.value+(8-conf.bpc)))(0)
                b1 := (io.dmaApb.PRDATA(23 downto  16) >> (bit_cntr.value+(8-conf.bpc)))(0)

                rgb_valid     := True
                col_cntr.increment()

                when(col_cntr.value === conf.panel_cols-1){
                    cur_state   := FsmState.Idle
                }
                .otherwise{
                    PADDR       := PADDR - U(conf.panel_cols * conf.panel_rows/2 * bytes_per_pixel) + U(bytes_per_pixel)
                    cur_state   := FsmState.RGB0ApbSetup
                }
            }
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
