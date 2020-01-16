package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

import cyclone2._

object LedMem {
    def getApb3Config() = Apb3Config(addressWidth = 16, dataWidth = 32)
}

case class LedMemConfig(
      memWords      : Int,
      bpc           : Int
  )
{
    def addrBits  = log2Up(memWords)
    def dataBits  = 3 * bpc
}

class LedMem(conf: LedMemConfig, isSim: Boolean = true) extends Component {

    import conf._

    val io = new Bundle {
        val led_mem_a_req     = in(Bool)
        val led_mem_a_addr    = in(UInt(conf.addrBits bits))
        val led_mem_a_wr      = in(Bool)
        val led_mem_a_wr_data = in(Bits(conf.dataBits bits))
        val led_mem_a_rd_data = out(Bits(conf.dataBits bits))

        val led_mem_b_req     = in(Bool)
        val led_mem_b_addr    = in(UInt(conf.addrBits bits))
        val led_mem_b_wr      = in(Bool)
        val led_mem_b_wr_data = in(Bits(conf.dataBits bits))
        val led_mem_b_rd_data = out(Bits(conf.dataBits bits))
    }

    println(s"LedMem: memWords: ${conf.memWords}")

    if (isSim){
        val u_led_ram = Mem(UInt(conf.dataBits bits), conf.memWords).addAttribute("ramstyle", "no_rw_check")

        io.led_mem_a_rd_data := u_led_ram.readWriteSync(
            enable    = io.led_mem_a_req,
            address   = io.led_mem_a_addr,
            write     = io.led_mem_a_wr, 
            data      = io.led_mem_a_wr_data.asUInt
            ).asBits
    
        io.led_mem_b_rd_data := u_led_ram.readWriteSync(
            enable    = io.led_mem_b_req,
            address   = io.led_mem_b_addr,
            write     = io.led_mem_b_wr, 
            data      = io.led_mem_b_wr_data.asUInt
            ).asBits
    }
    else {
        val u_led_ram = new led_ram()

        u_led_ram.io.clock_a          := ClockDomain.current.readClockWire
        u_led_ram.io.address_a        := io.led_mem_a_addr
        u_led_ram.io.wren_a           := io.led_mem_a_req && io.led_mem_a_wr
        u_led_ram.io.data_a           := io.led_mem_a_wr_data
        io.led_mem_a_rd_data          := u_led_ram.io.q_a

        u_led_ram.io.clock_b          := ClockDomain.current.readClockWire
        u_led_ram.io.address_b        := io.led_mem_b_addr
        u_led_ram.io.wren_b           := io.led_mem_b_req && io.led_mem_b_wr
        u_led_ram.io.data_b           := io.led_mem_b_wr_data
        io.led_mem_b_rd_data          := u_led_ram.io.q_b
    }


    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {
        val mapping = SizeMapping(0x0, conf.memWords * 4)

        val led_mem_a_wr_addr = busCtrl.writeAddress(mapping) >> 2
        val led_mem_a_rd_addr = busCtrl.readAddress(mapping) >> 2

        val bus_a_wr_data = Bits(24 bits)
        busCtrl.nonStopWrite(bus_a_wr_data, 0)

        val mem_a_wr_data = bus_a_wr_data(23 downto 24-conf.bpc) ## 
                            bus_a_wr_data(15 downto 16-conf.bpc) ## 
                            bus_a_wr_data( 7 downto  8-conf.bpc)

        io.led_mem_a_req      := False
        io.led_mem_a_wr       := False
        io.led_mem_a_addr     := led_mem_a_wr_addr

        busCtrl.onWritePrimitive(mapping, true, null){
            io.led_mem_a_req  := True
            io.led_mem_a_wr   := True
            io.led_mem_a_addr := led_mem_a_wr_addr
        }
        io.led_mem_a_wr_data  := mem_a_wr_data

        busCtrl.multiCycleRead(mapping, 2)
        busCtrl.onReadPrimitive(mapping, false, null){
            io.led_mem_a_req  := True
            io.led_mem_a_wr   := False
            io.led_mem_a_addr := led_mem_a_rd_addr
        }
        busCtrl.readPrimitive(io.led_mem_a_rd_data, mapping, 0, null)
    }

}
