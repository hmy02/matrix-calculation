package madd

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

class MatrixMultiplication3Tester(dut: MatrixMultiplication3)
    extends PeekPokeTester(dut) {
  poke(dut.io.ina.valid, true)
  poke(dut.io.inb.valid, true)

  for (i <- 0 until 8 * 8) {
    while (peek(dut.io.ina.ready) == BigInt(0)) {
      step(1)
    }

    poke(dut.io.ina.bits.a, i)

    step(1)
  }
  
  for (j <- 0 until 8 * 8) {
      while (peek(dut.io.inb.ready) == BigInt(0)) {
        step(1)
    }

    poke(dut.io.inb.bits.b, 1)

    step(1)
  }

  poke(dut.io.ina.valid, false)
  poke(dut.io.inb.valid, false)

  while (peek(dut.io.out.valid) == BigInt(0)) {
    step(1)
  }

  for (i <- 0 until 8) {
    for (j <- 0 until 8){
      expect(dut.io.out.bits(i * 8 + j), i * 64 + 28)
    }
  }
}

object MatrixMultiplication3Tester extends App {
  chisel3.iotesters.Driver(() => new MatrixMultiplication3(8, 8, 8)) { dut =>
    new MatrixMultiplication3Tester(dut)
  }
}