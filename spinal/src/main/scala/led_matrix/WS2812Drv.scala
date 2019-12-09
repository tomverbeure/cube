
package led_matrix

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._

class WS2812Drv extends Component {

    def osc_clk_mhz   = 12

    def led_t0l_ns    = 850
    def led_t0h_ns    = 400

    def led_t1l_ns    = 450
    def led_t1h_ns    = 800

    def reset_ns      = 280000

    def led_t0l_cyc   = led_t0l_ns * osc_clk_mhz / 1000
    def led_t0h_cyc   = led_t0h_ns * osc_clk_mhz / 1000

    def led_t1l_cyc   = led_t1l_ns * osc_clk_mhz / 1000
    def led_t1h_cyc   = led_t1h_ns * osc_clk_mhz / 1000

    def reset_cyc     = reset_ns * osc_clk_mhz / 1000


    val io = new Bundle {
        val led_stream    = slave(Stream(Bits(24 bits)))
        val led_din       = out(Bool)
    }

    object FsmState extends SpinalEnum {
        val Idle            = newElement()
        val LoadLedVal      = newElement()
        val ShiftLedTh      = newElement()
        val ShiftLedTl      = newElement()
        val LedReset        = newElement()
    }


    val cur_state = Reg(FsmState()) init(FsmState.Idle)

    val bit_cntr  = Reg(UInt(5 bits))
    val t_cntr    = Reg(UInt(13 bits))
    val led_shift = Reg(Bits(24 bits))

    io.led_stream.ready   := False
    io.led_din            := False

    switch(cur_state){
        is(FsmState.Idle){
            when(io.led_stream.valid){
                cur_state := FsmState.LoadLedVal
            }
        }

        is(FsmState.LoadLedVal){

            when(io.led_stream.valid){
                led_shift := io.led_stream.payload
                bit_cntr  := 23
                t_cntr    := 0
                cur_state := FsmState.ShiftLedTh
            }
            .otherwise{
                cur_state   := FsmState.LedReset
            }
        }

        is(FsmState.ShiftLedTh){
            io.led_din    := True
            t_cntr        := t_cntr + 1

            when ((led_shift(23) && t_cntr === led_t1h_cyc) || (!led_shift(23) && t_cntr === led_t0h_cyc)) {
                t_cntr    := 0
                cur_state := FsmState.ShiftLedTl
            }
        }

        is(FsmState.ShiftLedTl){
            io.led_din    := False
            t_cntr        := t_cntr + 1

            when ((led_shift(23) && t_cntr === led_t1l_cyc) || (!led_shift(23) && t_cntr === led_t0l_cyc)) {
                t_cntr    := 0

                when(bit_cntr =/= 0) {
                    bit_cntr    := bit_cntr -1
                    led_shift   := led_shift(22 downto 0) ## False
                    cur_state   := FsmState.ShiftLedTh
                }
                .otherwise{
                    io.led_stream.ready   := True
                    cur_state             := FsmState.LoadLedVal
                }
            }
        }

        is(FsmState.LedReset){
            t_cntr    := t_cntr + 1

            when (t_cntr === reset_cyc){
                cur_state   := FsmState.Idle
            }
        }

    }

}
