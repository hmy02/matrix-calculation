package madd

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class MatrixMultiplication2(M: Int, N: Int, P: Int)
    extends Module
    with CurrentCycle {
  val io = IO(new MatrixMultiplication2IO(M, P))

  io.ina.ready := false.B
  io.inb.ready := false.B

  io.out.bits := DontCare
  io.out.valid := false.B

  private val sLoadA :: sLoadB :: sLoad :: sCompute :: sStore :: Nil = Enum(5)

  private val state = RegInit(sLoadA)

  val regA = Reg(Vec(M * N, SInt(32.W)))
  val regB = Reg(Vec(N * P, SInt(32.W)))
  val regOut = Reg(Vec(M * P, SInt(32.W)))
  var regSum = Reg(SInt(32.W))
  
  regSum := 0.S

  val i = Counter(M)
  val j = Counter(P)
  val k = Counter(N)

  switch(state) {
    is(sLoadA) {
      io.ina.ready := true.B
        when(io.ina.fire()) {
            regA(i.value * N.U + k.value) := io.ina.bits.a
            state := sLoadA
            when(k.inc()) {
              when(i.inc()){
                state := sLoadB
              }
            }
        }
    }
    is(sLoadB) {
      io.inb.ready := true.B
        when(io.inb.fire()) {
            regB(k.value * P.U + j.value) := io.inb.bits.b
            state := sLoadB
            when(j.inc()) {
              when(k.inc()){
                state := sLoad
              }
            }
        }
    }
    is(sLoad){
      regSum := regSum + regA(i.value * N.U + k.value) * regB(k.value * P.U + j.value)
      state := sLoad
      when(k.inc){
        state := sCompute
      }
    }
    is(sCompute) {
      regOut(i.value * P.U + j.value) := regSum
      regSum := 0.S
      state := sLoad

      when(j.inc()) {
        when(i.inc()) {
          state := sStore
        }
      }
    }
    is(sStore) {
      io.out.bits := regOut
      io.out.valid := true.B
    }
  }

  chisel3.printf(
    p"[$currentCycle] state: $state\n"
  )
  
}

object MatrixMultiplication2 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new MatrixMultiplication2(8, 8, 8))
    )
  )
}