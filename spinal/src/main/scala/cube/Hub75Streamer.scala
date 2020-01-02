
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
    val output_fifo_occupancy = UInt(log2Up(conf.panel_cols+1) bits)

    val col_cntr = Counter(conf.panel_cols)
    val bit_cntr = Counter(conf.bpc, col_cntr.willOverflow)
    val row_cntr = Counter(conf.panel_rows/2, bit_cntr.willOverflow)


    object FsmState extends SpinalEnum {
        val FetchPhase0        = newElement()
        val FetchPhase1        = newElement()
    }

    val cur_state = Reg(FsmState()) init(FsmState.FetchPhase0)

    val led_mem_rd        = RegInit(False)
    val led_mem_rd_addr   = Reg(UInt(ledMemConf.addrBits bits)) init(0)
    val led_mem_phase     = RegInit(False)

    switch(cur_state){
        is(FsmState.FetchPhase0){
            when(output_fifo_occupancy < (conf.panel_cols-2)){
                led_mem_rd        := True
                led_mem_rd_addr   := (row_cntr * conf.panel_cols).resize(ledMemConf.addrBits) + col_cntr
                led_mem_phase     := False

                cur_state   := FsmState.FetchPhase1
            }
            .otherwise{
                led_mem_rd        := False
            }
        }
        is(FsmState.FetchPhase1){
            led_mem_rd      := True
            led_mem_rd_addr := led_mem_rd_addr + 8 * conf.panel_cols
            led_mem_phase   := True

            col_cntr.increment()
            cur_state       := FsmState.FetchPhase0
        }
    }

    io.led_mem_rd       := led_mem_rd
    io.led_mem_rd_addr  := led_mem_rd_addr

    val led_mem_rd_p1     = RegNext(led_mem_rd) init(False)
    val led_mem_phase_p1  = RegNext(led_mem_phase) init(False)
    val bit_cntr_p1       = Delay(bit_cntr.value, 2)
    val sof_p1            = Delay((col_cntr === 0 && row_cntr === 0 && bit_cntr === 0), 2)

    val r0  = RegInit(False)
    val g0  = RegInit(False)
    val b0  = RegInit(False)

    val r = (io.led_mem_rd_data((ledMemConf.bpc * 1 -1) downto ledMemConf.bpc * 0).resize(8) >> (bit_cntr_p1 + (8-conf.bpc)))(0)
    val g = (io.led_mem_rd_data((ledMemConf.bpc * 2 -1) downto ledMemConf.bpc * 1).resize(8) >> (bit_cntr_p1 + (8-conf.bpc)))(0)
    val b = (io.led_mem_rd_data((ledMemConf.bpc * 3 -1) downto ledMemConf.bpc * 2).resize(8) >> (bit_cntr_p1 + (8-conf.bpc)))(0)

    output_fifo_wr.valid    := False
    when(led_mem_rd_p1){
        when(!led_mem_phase_p1){
            r0  := r
            g0  := g
            b0  := b
        }
        .otherwise{
            output_fifo_wr.valid    := True
        }
    }


    /*
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
    */

    output_fifo_wr.payload(0) := r0
    output_fifo_wr.payload(1) := g0
    output_fifo_wr.payload(2) := b0
    output_fifo_wr.payload(3) := r
    output_fifo_wr.payload(4) := g
    output_fifo_wr.payload(5) := b
    output_fifo_wr.payload(6) := sof_p1

    val u_output_fifo = StreamFifo(
                            dataType  = Bits(7 bits),
                            depth     = conf.panel_cols
                        )
    u_output_fifo.io.push   << output_fifo_wr
    u_output_fifo.io.pop    >> io.rgb
    u_output_fifo.io.occupancy  <> output_fifo_occupancy

    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {
//        val start = busCtrl.createReadAndWrite(io.start, 0x0) init(False)
//        val active = busCtrl.createReadOnly(io.active, 0x4)
//
//        io.start := start
//        active := io.active
    }

}
