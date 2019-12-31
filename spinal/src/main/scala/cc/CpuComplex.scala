
package cc

import vexriscv.plugin._
import vexriscv._
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.apb._
import spinal.lib.bus.amba4.axi._
import spinal.lib.com.jtag.Jtag
import spinal.lib.com.uart.{Apb3UartCtrl, Uart, UartCtrlGenerics, UartCtrlMemoryMappedConfig}
import spinal.lib.graphic.RgbConfig
import spinal.lib.io.TriStateArray
import spinal.lib.misc.HexTools
import spinal.lib.soc.pinsec.{PinsecTimerCtrl, PinsecTimerCtrlExternal}
import spinal.lib.system.debugger.{JtagAxi4SharedDebugger, JtagBridge, SystemDebugger, SystemDebuggerConfig}

import scala.collection.mutable.ArrayBuffer

case class CpuComplexConfig(
                      cpuFrequency        : HertzNumber,
                      onChipRamSize       : BigInt,
                      onChipRamBinFile    : String,
                      periphApbConfig     : Apb3Config,
//                      dmaAxiConfig        : Axi4Config,
                      cpuPlugins          : ArrayBuffer[Plugin[VexRiscv]])

object CpuComplexConfig{

    def default = {
        val config = CpuComplexConfig(
            cpuFrequency              = 25 MHz,
            onChipRamSize             = 32 kB,
            onChipRamBinFile          = null,
            cpuPlugins = ArrayBuffer(
                new PcManagerSimplePlugin(0x00000000l, false),
                new IBusCachedPlugin(
                    resetVector             = 0x00000000l,
                    prediction              = STATIC,
                    config = InstructionCacheConfig(
                        cacheSize = 1024,
                        bytePerLine =32,
                        wayCount = 1,
                        addressWidth = 32,
                        cpuDataWidth = 32,
                        memDataWidth = 32,
                        catchIllegalAccess = true,
                        catchAccessFault = true,
                        asyncTagMemory = false,
                        twoCycleRam = true,
                        twoCycleCache = true
                    )
                ),
                new DBusCachedPlugin(
                    config = new DataCacheConfig(
                        cacheSize         = 1024,
                        bytePerLine       = 32,
                        wayCount          = 1,
                        addressWidth      = 32,
                        cpuDataWidth      = 32,
                        memDataWidth      = 32,
                        catchAccessError  = true,
                        catchIllegal      = true,
                        catchUnaligned    = true
                    ),
                    memoryTranslatorPortConfig = null
                ),
                new StaticMemoryTranslatorPlugin(
                    ioRange      = _(31 downto 28) === 0xF
                ),
                new DecoderSimplePlugin(
                    catchIllegalInstruction = true
                ),
                new RegFilePlugin(
                    regFileReadyKind = plugin.SYNC,
                    zeroBoot = false
                ),
                new IntAluPlugin,
                new SrcPlugin(
                    separatedAddSub = false,
                    executeInsertion = true
                ),
                new FullBarrelShifterPlugin,
                new MulPlugin,
                new DivPlugin,
                new HazardSimplePlugin(
                    bypassExecute           = true,
                    bypassMemory            = true,
                    bypassWriteBack         = true,
                    bypassWriteBackBuffer   = true,
                    pessimisticUseSrc       = false,
                    pessimisticWriteRegFile = false,
                    pessimisticAddressMatch = false
                ),
                new BranchPlugin(
                    earlyBranch = false,
                    catchAddressMisaligned = true
                ),
                new CsrPlugin(
                    config = CsrPluginConfig(
                        catchIllegalAccess = false,
                        mvendorid      = null,
                        marchid        = null,
                        mimpid         = null,
                        mhartid        = null,
                        misaExtensionsInit = 66,
                        misaAccess     = CsrAccess.NONE,
                        mtvecAccess    = CsrAccess.NONE,
                        mtvecInit      = 0x80000020l,
                        mepcAccess     = CsrAccess.READ_WRITE,
                        mscratchGen    = false,
                        mcauseAccess   = CsrAccess.READ_ONLY,
                        mbadaddrAccess = CsrAccess.READ_ONLY,
                        mcycleAccess   = CsrAccess.NONE,
                        minstretAccess = CsrAccess.NONE,
                        ecallGen       = false,
                        wfiGenAsWait   = false,
                        ucycleAccess   = CsrAccess.NONE
                    )
                ),
                new YamlPlugin("cpu0.yaml")
            ),
            periphApbConfig = Apb3Config(
                addressWidth  = 20,
                dataWidth     = 32
            )
//            dmaAxiConfig = Axi4Config(
//                addressWidth          = 32,
//                dataWidth             = 32
//            ),
        )
        config
    }
}

class CpuComplex(config: CpuComplexConfig) extends Component{

    //Legacy constructor
    def this(cpuFrequency: HertzNumber) {
        this(CpuComplexConfig.default.copy(cpuFrequency = cpuFrequency))
    }

    import config._
    val interruptCount = 4

    val io = new Bundle{

        val periphApb     = master(Apb3(config.periphApbConfig))
        val coreInterrupt = in Bool
//        val dmaAxi        = slave(Axi4ReadOnly(axi4Config))
    }

    val ram = Axi4SharedOnChipRam(
      dataWidth = 32,
      byteCount = onChipRamSize,
      idWidth = 4
    )

    val apbBridge = Axi4SharedToApb3Bridge(
      addressWidth = 20,
      dataWidth    = 32,
      idWidth      = 4
    )

    apbBridge.io.apb <> io.periphApb

    val core = new Area{

        val vexRiscvConfig = VexRiscvConfig(
            plugins = cpuPlugins
        )

        val cpu = new VexRiscv(vexRiscvConfig)
        var iBus : Axi4ReadOnly = null
        var dBus : Axi4Shared   = null
        for(plugin <- vexRiscvConfig.plugins) plugin match{
            case plugin : IBusSimplePlugin => iBus = plugin.iBus.toAxi4ReadOnly()
            case plugin : IBusCachedPlugin => iBus = plugin.iBus.toAxi4ReadOnly()
            case plugin : DBusSimplePlugin => dBus = plugin.dBus.toAxi4Shared()
            case plugin : DBusCachedPlugin => dBus = plugin.dBus.toAxi4Shared(true)
            case plugin : CsrPlugin        => {
                plugin.externalInterrupt  := BufferCC(io.coreInterrupt)
            }
            case _ =>
        }
    }


    val axiCrossbar = Axi4CrossbarFactory()

    axiCrossbar.addSlaves(
      ram.io.axi       -> (0x00000000L,   onChipRamSize),
      apbBridge.io.axi -> (0xF0000000L,   1 MB)
    )

    axiCrossbar.addConnections(
      core.iBus       -> List(ram.io.axi),
      core.dBus       -> List(ram.io.axi, apbBridge.io.axi)
//      vgaCtrl.io.axi  -> List(ram.io.axi)
    )


    axiCrossbar.addPipelining(apbBridge.io.axi)((crossbar,bridge) => {
      crossbar.sharedCmd.halfPipe() >> bridge.sharedCmd
      crossbar.writeData.halfPipe() >> bridge.writeData
      crossbar.writeRsp             << bridge.writeRsp
      crossbar.readRsp              << bridge.readRsp
    })

    axiCrossbar.addPipelining(ram.io.axi)((crossbar,ctrl) => {
      crossbar.sharedCmd.halfPipe()  >>  ctrl.sharedCmd
      crossbar.writeData            >/-> ctrl.writeData
      crossbar.writeRsp              <<  ctrl.writeRsp
      crossbar.readRsp               <<  ctrl.readRsp
    })

//    axiCrossbar.addPipelining(vgaCtrl.io.axi)((ctrl,crossbar) => {
//      ctrl.readCmd.halfPipe()    >>  crossbar.readCmd
//      ctrl.readRsp               <<  crossbar.readRsp
//    })

    axiCrossbar.addPipelining(core.dBus)((cpu,crossbar) => {
      cpu.sharedCmd             >>  crossbar.sharedCmd
      cpu.writeData             >>  crossbar.writeData
      cpu.writeRsp              <<  crossbar.writeRsp
      cpu.readRsp               <-< crossbar.readRsp //Data cache directly use read responses without buffering, so pipeline it for FMax
    })

    axiCrossbar.build()

}

