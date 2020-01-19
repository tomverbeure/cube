
package cube

//import scala.collection.immutable.Array

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

case class PanelInfoHW(conf: Hub75Config) extends Bundle {
    val topLeftXCoord           = SInt(2 bits)
    val topLeftYCoord           = SInt(2 bits)
    val topLeftZCoord           = SInt(2 bits)

    val memAddrStartPh0         = UInt(log2Up(conf.total_nr_pixels+1) bits)
    val memAddrStartPh1         = UInt(log2Up(conf.total_nr_pixels+1) bits)
    val memAddrColMul           = SInt(log2Up(conf.panel_cols)+2 bits)
    val memAddrRowMul           = SInt(log2Up(conf.panel_cols)+2 bits)

    val xIncr                   = SInt(2 bits)
    val yIncr                   = SInt(2 bits)
    val zIncr                   = SInt(2 bits)
}

case class PanelInfo(
        topLeftXCoord           : Int,
        topLeftYCoord           : Int,
        topLeftZCoord           : Int,

        side                    : Int,
        sideTop                 : Boolean,
        sideRotation            : Int,

        xIncr                   : Int,
        yIncr                   : Int,
        zIncr                   : Int
    )
{

    def toPanelInfoHW(conf: Hub75Config) : PanelInfoHW = {

        val piHW = PanelInfoHW(conf)

        piHW.topLeftXCoord      := topLeftXCoord
        piHW.topLeftYCoord      := topLeftYCoord
        piHW.topLeftZCoord      := topLeftZCoord

        var memAddrStartPh0     = side * 2 * conf.pixels_per_panel
        var memAddrStartPh1     = side * 2 * conf.pixels_per_panel
        var memAddrColMul       = 1
        var memAddrRowMul       = 1

        if (sideRotation == 0){
            memAddrStartPh1     += conf.panel_rows/2 * conf.panel_cols

            if (!sideTop){
                memAddrStartPh0 += conf.panel_rows * conf.panel_cols
                memAddrStartPh1 += conf.panel_rows * conf.panel_cols
            }
            memAddrColMul       = 1
            memAddrRowMul       = conf.panel_cols
        }
        else if (sideRotation == 90) {
            memAddrStartPh0     += conf.panel_cols -1
            memAddrStartPh1     += conf.panel_cols -1 - conf.panel_rows/2

            if (!sideTop){
                memAddrStartPh0 -= conf.panel_cols/2
                memAddrStartPh1 -= conf.panel_cols/2
            }
            memAddrColMul       = conf.panel_cols
            memAddrRowMul       = -1
        }
        else{
            memAddrStartPh0     += conf.panel_cols -1 + (conf.panel_rows*2 -1) * conf.panel_cols
            memAddrStartPh1     += memAddrStartPh0 - (conf.panel_rows/2 * conf.panel_cols)

            if (!sideTop){
                memAddrStartPh0 -= conf.panel_rows * conf.panel_cols
                memAddrStartPh1 -= conf.panel_rows * conf.panel_cols
            }
            memAddrColMul       = -1
            memAddrRowMul       = -conf.panel_cols
        }

        println(s"Side: $side")
        println(s"Top: $sideTop")
        println(s"Rot: $sideRotation")
        println(s"Ph0: $memAddrStartPh0")
        println(s"Ph1: $memAddrStartPh1")

        piHW.memAddrStartPh0    := memAddrStartPh0
        piHW.memAddrStartPh1    := memAddrStartPh1
        piHW.memAddrColMul      := memAddrColMul
        piHW.memAddrRowMul      := memAddrRowMul

        piHW.xIncr              := xIncr
        piHW.yIncr              := yIncr
        piHW.zIncr              := zIncr

        piHW
    }
}

case class Hub75Config(
              panel_rows      : Int, 
              panel_cols      : Int,
              bpc             : Int,
              panels          : Array[PanelInfo]
  ) 
{
    def pixels_per_clk    = 2
    def nr_row_bits       = log2Up(panel_rows / pixels_per_clk)
    def total_nr_pixels   = panels.size * panel_rows * panel_cols
    def pixels_per_panel  = panel_rows * panel_cols
}

case class Hub75Intfc(nr_row_bits : Int) extends Bundle {
    val clk         = Bool
    val lat         = Bool
    val oe_         = Bool
    val row         = UInt(nr_row_bits bits)
    val r0          = Bool
    val g0          = Bool
    val b0          = Bool
    val r1          = Bool
    val g1          = Bool
    val b1          = Bool
}

