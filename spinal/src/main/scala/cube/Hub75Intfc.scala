
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

    val topLeftMemAddr          = UInt(log2Up(12) bits)

    val xIncr                   = SInt(2 bits)
    val yIncr                   = SInt(2 bits)
    val zIncr                   = SInt(2 bits)
}

case class PanelInfo(
        topLeftXCoord           : Int,
        topLeftYCoord           : Int,
        topLeftZCoord           : Int,

        topLeftMemAddr          : Int,
        xIncr                   : Int,
        yIncr                   : Int,
        zIncr                   : Int
    )
{

    def toPanelInfoHW(conf: Hub75Config) : PanelInfoHW = {

        val piHW = PanelInfoHW(conf)

        piHW.topLeftXCoord     := topLeftXCoord
        piHW.topLeftYCoord     := topLeftYCoord
        piHW.topLeftZCoord     := topLeftZCoord
        piHW.topLeftMemAddr    := topLeftMemAddr
        piHW.xIncr             := xIncr
        piHW.yIncr             := yIncr
        piHW.zIncr             := zIncr

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

