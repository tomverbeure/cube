
package cube

import spinal.core._
import spinal.lib._
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._

case class Hub75Config(
              nr_panels       : Int, 
              panel_rows      : Int, 
              panel_cols      : Int,
              row_offset      : Int, 
              bpc             : Int,
              ram_addr_bits   : Int,
              ram_data_bits   : Int
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

