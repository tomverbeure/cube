package cube

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

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

class LedMem(conf: LedMemConfig) extends Component {

    import conf._

    val io = new Bundle {
        val led_mem_wr        = in(Bool)
        val led_mem_wr_addr   = in(UInt(conf.addrBits bits))
        val led_mem_wr_data   = in(Bits(conf.dataBits bits))

        val led_mem_rd        = in(Bool)
        val led_mem_rd_addr   = in(UInt(conf.addrBits bits))
        val led_mem_rd_data   = out(Bits(conf.dataBits bits))
    }

    println("LedMem: memWords: %d", conf.memWords)

    val u_led_mem = Mem(UInt(conf.dataBits bits), conf.memWords)

    u_led_mem.write(
        enable    = io.led_mem_wr,
        address   = io.led_mem_wr_addr,
        data      = io.led_mem_wr_data.asUInt
        )

    io.led_mem_rd_data := u_led_mem.readSync(
        enable    = io.led_mem_rd,
        address   = io.led_mem_rd_addr
        ).asBits

    def driveFrom(busCtrl: BusSlaveFactory, baseAddress: BigInt) = new Area {
        val mapping = SizeMapping(0x0, conf.memWords * 4)

        val led_mem_wr_addr = busCtrl.writeAddress(mapping) >> 2

        val bus_wr_data = Bits(24 bits)
        busCtrl.nonStopWrite(bus_wr_data, 0)

        val mem_wr_data = bus_wr_data(23 downto 24-conf.bpc) ## 
                          bus_wr_data(15 downto 16-conf.bpc) ## 
                          bus_wr_data( 7 downto  8-conf.bpc)

        io.led_mem_wr       := False
        io.led_mem_wr_addr  := led_mem_wr_addr
        io.led_mem_wr_data  := mem_wr_data

        busCtrl.onWritePrimitive(mapping, true, null){
            io.led_mem_wr   := True
        }
    }

}
