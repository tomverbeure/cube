
package led_matrix

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.misc.SizeMapping

import scala.collection.mutable.ArrayBuffer
import spinal.lib.com.uart._

import cc._

case class CpuTop() extends Component {

    val io = new Bundle {
        val led_red     = out(Bool)
        val led_green   = out(Bool)
        val led_blue    = out(Bool)

        val led_mem_rd      = in(Bool)
        val led_mem_rd_addr = in(UInt(9 bits))
        val led_mem_rd_data = out(Bits(24 bits))

        val led_streamer_apb  = master(Apb3(LedStreamer.getApb3Config()))
    }

    val cpuConfig = CpuComplexConfig.default.copy(onChipRamBinFile = "../sw/progmem4k.bin")
    //val cpuConfig = CpuComplexConfig.default

    val u_cpu = CpuComplex(cpuConfig)
    u_cpu.io.externalInterrupt <> False

    val apbMapping = ArrayBuffer[(Apb3, SizeMapping)]()

    //============================================================
    // Timer
    //============================================================

    val u_timer = new CCApb3Timer()
    u_timer.io.interrupt        <> u_cpu.io.timerInterrupt
    apbMapping += u_timer.io.apb -> (0x00000, 4 kB)

    //============================================================
    // GPIO control, bits:
    // 0 - Green LED
    // 1 - Blue LED
    // 2 - Red LED  (write only: hardware limitation)
    // 3 - Pano button
    //============================================================

    val u_led_ctrl = Apb3Gpio(3, withReadSync = true)
    u_led_ctrl.io.gpio.write(0)             <> io.led_red
    u_led_ctrl.io.gpio.write(1)             <> io.led_green
    u_led_ctrl.io.gpio.write(2)             <> io.led_blue
    u_led_ctrl.io.gpio.read(0)              := io.led_red
    u_led_ctrl.io.gpio.read(1)              := io.led_green
    u_led_ctrl.io.gpio.read(2)              := io.led_blue

    apbMapping += u_led_ctrl.io.apb -> (0x10000, 4 kB)

    //============================================================
    // LED memory
    //============================================================

    val u_led_mem = new LedMem()
    u_led_mem.io.led_mem_rd         <> io.led_mem_rd
    u_led_mem.io.led_mem_rd_addr    <> io.led_mem_rd_addr
    u_led_mem.io.led_mem_rd_data    <> io.led_mem_rd_data

    val led_mem_apb = Apb3(LedMem.getApb3Config())
    val led_mem_apb_regs = u_led_mem.driveFrom(Apb3SlaveFactory(led_mem_apb), 0x0)

    apbMapping += led_mem_apb -> (0x20000, 16 kB)

    //============================================================
    // External APBs
    //============================================================

    apbMapping += io.led_streamer_apb       -> (0x30000, 256)

    //============================================================
    // Local APB decoder
    //============================================================
    val apbDecoder = Apb3Decoder(
      master = u_cpu.io.apb,
      slaves = apbMapping
    )

}

