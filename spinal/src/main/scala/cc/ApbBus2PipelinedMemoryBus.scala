
package cc

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.simple._

object ApbBus2PipelinedMemoryBus {
}

class ApbBus2PipelinedMemoryBus(baseAddr: Int, apb3Config: Apb3Config, pipelinedMemoryBusConfig: PipelinedMemoryBusConfig) extends Component {

    val io = new Bundle {
        val src         = slave(Apb3(apb3Config))
        val dest        = master(PipelinedMemoryBus(pipelinedMemoryBusConfig))
    }

    val xfer_req_pending = RegInit(False).clearWhen(io.dest.cmd.ready)
    val xfer_rsp_pending = RegInit(False).clearWhen(io.dest.rsp.valid && !io.src.PWRITE)

    val update_addr = False
    when((io.src.PENABLE && io.src.PSEL.orR).rise){
        xfer_req_pending  := True
        xfer_rsp_pending  := !io.src.PWRITE
        update_addr       := True
    }

    val addr = RegNextWhen(io.src.PADDR.resize(io.dest.cmd.address.getWidth) + baseAddr, update_addr)

    io.dest.cmd.valid     := xfer_req_pending
    io.dest.cmd.address   := addr
    io.dest.cmd.write     := io.src.PWRITE
    io.dest.cmd.mask      := (default -> True)
    io.dest.cmd.data      := io.src.PWDATA

    io.src.PREADY         := io.src.PWRITE ? io.dest.cmd.ready | io.dest.rsp.valid
    io.src.PRDATA         := io.dest.rsp.data
}

