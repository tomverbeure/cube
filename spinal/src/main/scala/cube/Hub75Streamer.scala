
package cube

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

object Hub75Streamer {
    def getApb3Config() = Apb3Config(addressWidth = 12, dataWidth = 32)
}

class Hub75Streamer(conf: Hub75Config, ledMemConf: LedMemConfig) extends Component {

    val io = new Bundle {
        val rgb               = master(Stream(Bits(13 bits)))

        val panel_infos       = in(Vec(PanelInfoHW(conf), conf.panels.size))

        val enable            = in(Bool)
        val start             = in(Bool)
        val eof               = out(Bool)

        val r_dim             = in(UInt(8 bits))
        val g_dim             = in(UInt(8 bits))
        val b_dim             = in(UInt(8 bits))

        val led_mem_rd        = out(Bool)
        val led_mem_rd_addr   = out(UInt(ledMemConf.addrBits bits))
        val led_mem_rd_data   = in(Bits(ledMemConf.dataBits bits))

        val cur_buffer_nr     = in(UInt(1 bits))
        val cur_panel_nr      = out(UInt(log2Up(conf.panels.size/2) bits))
        val cur_row_nr        = out(UInt(log2Up(conf.panel_rows/2) bits))
        val cur_bit_nr        = out(UInt(log2Up(conf.bpc) bits))

        val frame_cntr        = out(UInt(24 bits))
    }

    val output_fifo_depth = 2 * conf.panel_cols * conf.panels.size/2
    val output_fifo_wr = Stream(Bits(io.rgb.payload.getWidth bits))
    val output_fifo_rd = Stream(Bits(io.rgb.payload.getWidth bits))
    val output_fifo_availability = UInt(log2Up(output_fifo_depth+1) bits)
       
    val col_cntr        = Counter(conf.panel_cols)
    val panel_cntr      = Counter(conf.panels.size/2, col_cntr.willOverflow)
    val bit_cntr        = Counter(conf.bpc, panel_cntr.willOverflow)
    val row_cntr        = Counter(conf.panel_rows/2, bit_cntr.willOverflow)
    val frame_cntr      = Counter(io.frame_cntr.getWidth bits, row_cntr.willOverflow)

    io.cur_panel_nr  := panel_cntr.value
    io.cur_row_nr    := row_cntr.value
    io.cur_bit_nr    := bit_cntr.value
    io.eof           := row_cntr.willOverflow

    io.frame_cntr     := frame_cntr.value

    val cur_panel_info_0  = io.panel_infos(panel_cntr.value.resize(log2Up(conf.panels.size)))
    val cur_panel_info_1  = io.panel_infos(panel_cntr.value.resize(log2Up(conf.panels.size)) + U(conf.panels.size/2))

    object FsmState extends SpinalEnum {
        val FetchPhase0_0      = newElement()
        val FetchPhase0_1      = newElement()
        val FetchPhase1_0      = newElement()
        val FetchPhase1_1      = newElement()
    }

    val cur_state = Reg(FsmState()) init(FsmState.FetchPhase0_0)

    val led_mem_rd        = RegInit(False)
    val led_mem_rd_addr   = Reg(SInt(ledMemConf.addrBits+1 bits)) init(0)
    val led_mem_phase     = Reg(UInt(2 bits)) init(0)

    val active            = RegInit(False).setWhen(io.start).clearWhen(io.eof || !io.enable)

    when(!active){
        col_cntr.clear()
        panel_cntr.clear()
        row_cntr.clear()
        bit_cntr.clear()
    }

    val ph0_addr = Reg(UInt(ledMemConf.addrBits bits)) init(0)
    val ph1_addr = Reg(UInt(ledMemConf.addrBits bits)) init(0)


    val cur_memAddrStart   =  ((cur_state === FsmState.FetchPhase0_0) ? cur_panel_info_0.memAddrStartPh0 |
                              ((cur_state === FsmState.FetchPhase0_1) ? cur_panel_info_0.memAddrStartPh1 |
                              ((cur_state === FsmState.FetchPhase1_0) ? cur_panel_info_1.memAddrStartPh0 |
                                                                        cur_panel_info_1.memAddrStartPh1 )))

    val cur_memAddrRowMul  =  ((cur_state === FsmState.FetchPhase0_0) ? cur_panel_info_0.memAddrRowMul |
                              ((cur_state === FsmState.FetchPhase0_1) ? cur_panel_info_0.memAddrRowMul |
                              ((cur_state === FsmState.FetchPhase1_0) ? cur_panel_info_1.memAddrRowMul |
                                                                        cur_panel_info_1.memAddrRowMul )))

    val cur_memAddrColMul  =  ((cur_state === FsmState.FetchPhase0_0) ? cur_panel_info_0.memAddrColMul |
                              ((cur_state === FsmState.FetchPhase0_1) ? cur_panel_info_0.memAddrColMul |
                              ((cur_state === FsmState.FetchPhase1_0) ? cur_panel_info_1.memAddrColMul |
                                                                        cur_panel_info_1.memAddrColMul )))

    val led_mem_rd_addr_comb = ((False ## (io.cur_buffer_nr * conf.total_nr_pixels)).resize(ledMemConf.addrBits+1).asSInt
                                  + cur_memAddrStart.resize(ledMemConf.addrBits+1).asSInt
                                  + ((False ## row_cntr.value).asSInt * cur_memAddrRowMul).resize(ledMemConf.addrBits+1)
                                  + ((False ## col_cntr.value).asSInt * cur_memAddrColMul).resize(ledMemConf.addrBits+1))

    switch(cur_state){
        is(FsmState.FetchPhase0_0){
          when(active && output_fifo_availability > 2){
                led_mem_rd        := True
                led_mem_rd_addr   := led_mem_rd_addr_comb
                led_mem_phase     := 0

                cur_state   := FsmState.FetchPhase0_1
            }
            .otherwise{
                led_mem_rd        := False
            }
        }
        is(FsmState.FetchPhase0_1){
            led_mem_rd        := True
            led_mem_phase     := 1
            led_mem_rd_addr   := led_mem_rd_addr_comb

            cur_state       := FsmState.FetchPhase1_0
        }
        is(FsmState.FetchPhase1_0){
            led_mem_rd        := True
            led_mem_phase     := 2
            led_mem_rd_addr   := led_mem_rd_addr_comb

            cur_state       := FsmState.FetchPhase1_1
        }
        is(FsmState.FetchPhase1_1){
            led_mem_rd        := True
            led_mem_phase     := 3
            led_mem_rd_addr   := led_mem_rd_addr_comb

            col_cntr.increment()
            cur_state       := FsmState.FetchPhase0_0
        }
    }

    io.led_mem_rd       := led_mem_rd
    io.led_mem_rd_addr  := led_mem_rd_addr.resize(ledMemConf.addrBits).asUInt

    val led_mem_rd_p1     = RegNext(led_mem_rd) init(False)
    val led_mem_phase_p1  = RegNext(led_mem_phase) init(0)
    val bit_cntr_p1       = Delay(bit_cntr.value, 2)
    val sof_p1            = Delay((col_cntr === 0 && panel_cntr === 0 && row_cntr === 0 && bit_cntr === 0), 2)

    // Convert from ledMemConf.bpc to conf.bpc
    val led_mem_r = io.led_mem_rd_data((ledMemConf.bpc * 1 -1) downto ledMemConf.bpc * 0) ## U(0, 8-ledMemConf.bpc bits) >> (8-conf.bpc)
    val led_mem_g = io.led_mem_rd_data((ledMemConf.bpc * 2 -1) downto ledMemConf.bpc * 1) ## U(0, 8-ledMemConf.bpc bits) >> (8-conf.bpc)
    val led_mem_b = io.led_mem_rd_data((ledMemConf.bpc * 3 -1) downto ledMemConf.bpc * 2) ## U(0, 8-ledMemConf.bpc bits) >> (8-conf.bpc)

    val gammaTable = for(index <-  0 to (1<<conf.bpc)-1) yield {
        val ratio = index.toFloat / ((1<<conf.bpc)-1).toFloat
        val gammaValue = Math.pow(ratio, 2.2) * ((1<<conf.bpc)-1)
        U(gammaValue.toInt, conf.bpc bits)
    }

    val gamma_rom_r = Mem(UInt(conf.bpc bits), initialContent = gammaTable)
    val gamma_rom_g = Mem(UInt(conf.bpc bits), initialContent = gammaTable)
    val gamma_rom_b = Mem(UInt(conf.bpc bits), initialContent = gammaTable)

    val r_gamma = gamma_rom_r.readAsync(U(led_mem_r))
    val g_gamma = gamma_rom_g.readAsync(U(led_mem_g))
    val b_gamma = gamma_rom_b.readAsync(U(led_mem_b))

    val r_dimmed = (r_gamma * io.r_dim)
    val g_dimmed = (g_gamma * io.g_dim)
    val b_dimmed = (b_gamma * io.b_dim)

    val r = (r_dimmed.asBits.resizeLeft(r_gamma.getWidth) >> bit_cntr_p1)(0)
    val g = (g_dimmed.asBits.resizeLeft(g_gamma.getWidth) >> bit_cntr_p1)(0)
    val b = (b_dimmed.asBits.resizeLeft(b_gamma.getWidth) >> bit_cntr_p1)(0)

    val r_vec = Reg(Bits(4 bits)) 
    val g_vec = Reg(Bits(4 bits)) 
    val b_vec = Reg(Bits(4 bits)) 

    when(led_mem_rd_p1){
        r_vec(led_mem_phase_p1) := r
        g_vec(led_mem_phase_p1) := g
        b_vec(led_mem_phase_p1) := b
    }

    val fifo_wr_p2      = RegNext(led_mem_rd_p1 && led_mem_phase_p1 === 3) init(False)
    val sof_p2          = RegNext(sof_p1) init(False)

    output_fifo_wr.valid  := fifo_wr_p2

    output_fifo_wr.payload( 0) := r_vec(0)
    output_fifo_wr.payload( 1) := g_vec(0)
    output_fifo_wr.payload( 2) := b_vec(0)
    output_fifo_wr.payload( 3) := r_vec(1)
    output_fifo_wr.payload( 4) := g_vec(1)
    output_fifo_wr.payload( 5) := b_vec(1)
    output_fifo_wr.payload( 6) := r_vec(2)
    output_fifo_wr.payload( 7) := g_vec(2)
    output_fifo_wr.payload( 8) := b_vec(2)
    output_fifo_wr.payload( 9) := r_vec(3)
    output_fifo_wr.payload(10) := g_vec(3)
    output_fifo_wr.payload(11) := b_vec(3)
    output_fifo_wr.payload(12) := sof_p2


    val u_output_fifo = StreamFifo(
                            dataType  = Bits(13 bits),
                            depth     = output_fifo_depth
                        )
    u_output_fifo.io.push         << output_fifo_wr
    u_output_fifo.io.pop          >> output_fifo_rd
    u_output_fifo.io.availability <> output_fifo_availability

    io.rgb.valid    := output_fifo_rd.valid && (!output_fifo_rd.payload(12) || output_fifo_availability <= 2)
    io.rgb.payload  := output_fifo_rd.payload
    output_fifo_rd.ready  := io.rgb.ready

    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {

          // All panel info parameters are programmable for fast iteration
          for(i <- 0 until conf.panels.length){
              val addr = 0x100 + i * 0x40  

              val info_hw = conf.panels(i).toPanelInfoHW(conf)

              io.panel_infos(i).topLeftXCoord := busCtrl.createReadAndWrite(PanelInfoHW(conf).topLeftXCoord, addr + 0x00, 0) init(info_hw.topLeftXCoord)
              io.panel_infos(i).topLeftYCoord := busCtrl.createReadAndWrite(PanelInfoHW(conf).topLeftXCoord, addr + 0x00, 2) init(info_hw.topLeftYCoord)
              io.panel_infos(i).topLeftZCoord := busCtrl.createReadAndWrite(PanelInfoHW(conf).topLeftXCoord, addr + 0x00, 4) init(info_hw.topLeftZCoord)

              io.panel_infos(i).xIncr := busCtrl.createReadAndWrite(PanelInfoHW(conf).xIncr, addr + 0x00, 8) init(info_hw.xIncr)
              io.panel_infos(i).yIncr := busCtrl.createReadAndWrite(PanelInfoHW(conf).yIncr, addr + 0x00, 10) init(info_hw.yIncr)
              io.panel_infos(i).zIncr := busCtrl.createReadAndWrite(PanelInfoHW(conf).zIncr, addr + 0x00, 12) init(info_hw.zIncr)

              io.panel_infos(i).memAddrStartPh0 := busCtrl.createReadAndWrite(PanelInfoHW(conf).memAddrStartPh0, addr + 0x04, 0) init(info_hw.memAddrStartPh0)
              io.panel_infos(i).memAddrStartPh1 := busCtrl.createReadAndWrite(PanelInfoHW(conf).memAddrStartPh1, addr + 0x08, 0) init(info_hw.memAddrStartPh1)

              io.panel_infos(i).memAddrColMul := busCtrl.createReadAndWrite(PanelInfoHW(conf).memAddrColMul, addr + 0x0c, 0) init(info_hw.memAddrColMul)
              io.panel_infos(i).memAddrRowMul := busCtrl.createReadAndWrite(PanelInfoHW(conf).memAddrRowMul, addr + 0x10, 0) init(info_hw.memAddrRowMul)
          }


          // Config 
          val enable              = busCtrl.createReadAndWrite(Bool,              0x0, 0) init(False)
          val start               = busCtrl.createReadAndWrite(Bool,              0x0, 1) init(False)
          val auto_restart        = busCtrl.createReadAndWrite(Bool,              0x0, 2) init(False)
          val buffer_nr           = busCtrl.createReadAndWrite(io.cur_buffer_nr,  0x0, 4) init(0)

          // Status
          val cur_panel_nr        = busCtrl.createReadOnly(panel_cntr.value,      0x04)
          val cur_row_nr          = busCtrl.createReadOnly(row_cntr.value,        0x04, 8)
          val cur_bit_nr          = busCtrl.createReadOnly(bit_cntr.value,        0x04, 24)
          val cur_buffer_nr_reg   = busCtrl.createReadOnly(io.cur_buffer_nr,      0x04, 31)

          // Total number of frame transmitted
          val frame_cntr          = busCtrl.createReadOnly(io.frame_cntr,         0x08, 0)

          // Global dimming factor to control power
          val r_dim               = busCtrl.createReadAndWrite(io.r_dim,          0xc,  0)  init(U(255))
          val g_dim               = busCtrl.createReadAndWrite(io.g_dim,          0xc,  8)  init(U(255))
          val b_dim               = busCtrl.createReadAndWrite(io.b_dim,          0xc,  16) init(U(255))

          val cur_buffer_nr = RegInit(U(0, 1 bits))
          when(io.eof){
              cur_buffer_nr := buffer_nr
          }

          val restart = RegNext(io.eof) && auto_restart

          io.enable         := enable
          io.start          := (start && !RegNext(start)) || restart
          io.cur_buffer_nr  := cur_buffer_nr

          io.r_dim          := r_dim
          io.g_dim          := g_dim
          io.b_dim          := b_dim

          cur_panel_nr      := io.cur_panel_nr
          cur_row_nr        := io.cur_row_nr
          cur_bit_nr        := io.cur_bit_nr
          cur_buffer_nr_reg := cur_buffer_nr

          frame_cntr        := io.frame_cntr
    }

}


