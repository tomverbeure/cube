
package cube

import scala.collection.mutable.ArrayBuffer

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.apb._

object LedGpu {
    def getApb3Config() = Apb3Config(addressWidth = 8, dataWidth = 32)
}

class LedGpu(conf: Hub75Config, ledMemConf: LedMemConfig) extends Component {

    val io = new Bundle {

    }

//    val panel_info_vec  = Vec(conf.panels.map(_.toPanelInfoHW(conf)))
//    val cur_panel_info  = panel_info_vec(panel_cntr.value)

    /*
    // Loop through all pixels of the cube
    //
    // Plan:
    // A launch FIFO with: current value of pixel, coordinates, other attributes.
    // Filling up this FIFO is lowest priority.
    //
    // Instructions to load data from surrounding pixels on the same surface?
    //  - load from FROM buffer to TO buffer
    //  - 8 surrounding locations
    //  - overflow behavior: use pixel on other face or clamp?

    // - F.rgb    : FROM color of current pixel
    // - P.xyz    : position of current pixel (x,y,z)
    // - C0.xyz   : Constant attribute
    // - C1.xyz   : Constant attribute
    // - C2.xyz   : Constant attribute
    // - T0.xyz   : temporary stronage
    // - T1.xyz   : temporary stronage
    //
    // Instructions:
    // MUL.xyz <DEST, SRC0, SRC1> : DEST.x = SRC0.x * SRC1.x, ...
    // ADD.xyz <DEST, SRC0, SRC1> : DEST.x = SRC0.x + SRC1.x, ...
    // SUB.xyz <DEST, SRC0, SRC1> : DEST.x = SRC0.x + SRC1.x, ...
    // DOT.xyz <DEST, SRC0, SRC1> : DEST.x = SRC0.x + SRC1.y, ...

    // Function: (x-cx)^2 * (1/rx*rx) + (y-cy)^2 * (1/ry*ry) + (z-cz)^2 * (1/rz*rz) - 1 > 0 or < 0
    // C0: center of sphere
    // C1: 1/radius of sphere (different x,y,z sizes)
    // C2: 1/(radius+1) of sphere (different x,y,z sizes)
    SUB.xyz T0.xyz, P.xyz, C0.xyz
    MUL.xyz T0.xyz, T0.xyz, T0.xyz
    MUL.xyz T1.xyz, T0.xyz, C2.xyz
    MUL.xyz T0.xyz, T0.xyz, C1.xyz
    RADD T0.x, T0.xyz
    RADD T0.y, T1.xyz
    LD T1.xy, #1.0
    SUB T0.xy, T1.xy

    */

}



