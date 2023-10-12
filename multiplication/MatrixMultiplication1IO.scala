package madd

import chisel3._
import chisel3.util._

class MatrixMultiplication1IO(M: Int, N: Int, P: Int) extends Bundle {
  val a = Input(Vec(M * N, SInt(32.W)))
  val b = Input(Vec(N * P, SInt(32.W)))

  val out = Output(Vec(M * P, SInt(32.W)))

  override def cloneType =
    new MatrixMultiplication1IO(M, N, P).asInstanceOf[this.type]
}