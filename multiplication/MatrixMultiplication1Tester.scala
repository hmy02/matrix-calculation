package madd

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

class MatrixMultiplication1Tester(dut: MatrixMultiplication1)
    extends PeekPokeTester(dut) {
  for (i <- 0 until 3 * 2) {
    poke(dut.io.a(i), i)
  }

  for (i <- 0 until 2 * 4) {
    poke(dut.io.b(i), 1)
  }

  for (i <- 0 until 3) {
    for (j <- 0 until 4){
      expect(dut.io.out(i * 4 + j), i * 4 + 1)
    }
    
  }
}

object MatrixMultiplication1Tester extends App {
  chisel3.iotesters.Driver(() => new MatrixMultiplication1(3, 2, 4)) { dut =>
    new MatrixMultiplication1Tester(dut)
  }
}