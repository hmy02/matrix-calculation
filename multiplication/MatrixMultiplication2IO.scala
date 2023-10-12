package madd

import chisel3._
import chisel3.util._

class MatrixMultiplication2IO(M: Int, P: Int) extends Bundle {
  val ina = Flipped(DecoupledIO(new Bundle {
    val a = SInt(32.W)
  }))
  val inb = Flipped(DecoupledIO(new Bundle {
    val b = SInt(32.W)
  }))
  val out = ValidIO(Vec(M * P, SInt(32.W)))

  override def cloneType =
    new MatrixMultiplication2IO(M, P).asInstanceOf[this.type]
}
