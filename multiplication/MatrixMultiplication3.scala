package madd

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class MatrixMultiplication3(M: Int, N: Int, P: Int)
    extends Module
    with CurrentCycle {
  val io = IO(new MatrixMultiplication3IO(M, P))

  io.ina.ready := false.B
  io.inb.ready := false.B

  io.out.bits := DontCare
  io.out.valid := false.B


  val regA = Reg(Vec(M * N, SInt(32.W)))
  val regB = Reg(Vec(N * P, SInt(32.W)))
  val regOut = Reg(Vec(M * P, SInt(32.W)))

  val regLoadAEnabled = RegInit(true.B)
  val regLoadBEnabled = RegInit(false.B)
  val regLoadEnabled = RegInit(false.B)
  val regComputeEnabled = RegInit(false.B)
  val regStoreEnabled = RegInit(false.B)
  


  val i = Counter(M)
  val j = Counter(P)
  val k = Counter(N)


  io.ina.ready := regLoadAEnabled
  io.inb.ready := regLoadBEnabled

  when(io.ina.fire()) {
        regA(i.value * N.U + k.value) := io.ina.bits.a
        when(k.inc()) {
            when(i.inc()){
                regLoadAEnabled := false.B
                regLoadBEnabled := true.B
            }
        }
    }

    when(io.inb.fire()) {
        regB(k.value * P.U + j.value) := io.inb.bits.b
        when(j.inc()) {
            when(k.inc()){
                regLoadBEnabled := false.B
                regLoadEnabled := true.B
            }
        }
    }
  
  when(regLoadEnabled){
    regOut(0) := 0.S
    regLoadEnabled := false.B
    regComputeEnabled := true.B
  }

  when(regComputeEnabled) {
    regOut(i.value * P.U + j.value) := regOut(i.value * P.U + j.value) + regA(i.value * N.U + k.value) * regB(k.value * P.U + j.value)
    when(i.value * P.U + j.value < M.U * P.U - 1.U) {
      regOut(i.value * P.U + j.value + 1.U) := 0.S
    }
    when(k.inc()){
      when(j.inc()){
        when(i.inc()){
            regComputeEnabled := false.B
            regStoreEnabled := true.B
        }
      }
    }
  }

  when(regStoreEnabled) {
      io.out.bits := regOut
      io.out.valid := true.B
  }


  chisel3.printf(
    p"[$currentCycle] io.ina/b.fire(): ${io.ina.fire()}/${io.inb.fire()}, regLoadAEnabled: $regLoadAEnabled, regLoadBEnabled: $regLoadBEnabled, regLoadEnabled: $regLoadEnabled, regComputeEnabled: $regComputeEnabled, regStoreEnabled: $regStoreEnabled, io.out.fire(): ${io.out.fire()}\n"
  )
  
}

object MatrixMultiplication3 extends App {
  (new ChiselStage).execute(
    Array("-X", "verilog", "-td", "source/"),
    Seq(
      ChiselGeneratorAnnotation(() => new MatrixMultiplication3(8, 8, 8))
    )
  )
}