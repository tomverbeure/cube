
package led_matrix

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

object LedStreamer {
    def getApb3Config() = Apb3Config(addressWidth = 4, dataWidth = 32)
}

class LedStreamer extends Component {

    val io = new Bundle {
        val start             = in(Bool)
        val active            = out(Bool)

        val led_stream        = master(Stream(Bits(24 bits)))

        val led_mem_rd        = out(Bool)
        val led_mem_rd_addr   = out(UInt(9 bits))
        val led_mem_rd_data   = in(Bits(24 bits))
    }

    object FsmState extends SpinalEnum {
        val Idle            = newElement()
        val FetchLedVal     = newElement()
        val ReadyLedVal     = newElement()
        val SendLedVal      = newElement()
        val ShiftLedTh      = newElement()
        val ShiftLedTl      = newElement()
        val LedReset        = newElement()
    }

    val cur_state = Reg(FsmState()) init(FsmState.Idle)

    val led_cntr  = Reg(UInt(9 bits))

    val led_mem_addr  = Reg(UInt(9 bits))
    val led_val       = Reg(Bits(24 bits))

    io.led_stream.valid   := False
    io.led_stream.payload := led_val

    io.led_mem_rd         := False
    io.led_mem_rd_addr    := led_mem_addr.resize(io.led_mem_rd_addr.getWidth)

    io.active             := True

    switch(cur_state){
        is(FsmState.Idle){
            io.led_stream.valid   := False
            io.active             := False

            led_cntr              := 383
            led_mem_addr          := 0

            when(io.start && !RegNext(io.start)){
                cur_state         := FsmState.FetchLedVal
            }
        }

        is(FsmState.FetchLedVal){
            io.led_mem_rd         := True
            led_mem_addr          := led_mem_addr + 1
            cur_state             := FsmState.ReadyLedVal
        }

        is(FsmState.ReadyLedVal){
            led_val               := io.led_mem_rd_data

            cur_state             := FsmState.SendLedVal
        }

        is(FsmState.SendLedVal){

            io.led_stream.valid   := True
            io.led_stream.payload := led_val

            io.led_mem_rd         := True

            when(io.led_stream.ready){
                when(led_cntr === 0){
                    cur_state     := FsmState.Idle
                }
                .otherwise{
                    led_cntr      := led_cntr - 1
                    led_mem_addr  := led_mem_addr + 1
                    led_val       := io.led_mem_rd_data
                    cur_state     := FsmState.SendLedVal
                }
            }
        }
    }

    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {
        val start = busCtrl.createReadAndWrite(io.start, 0x0) init(False)
        val active = busCtrl.createReadOnly(io.active, 0x4)

        io.start := start
        active := io.active
    }

}
