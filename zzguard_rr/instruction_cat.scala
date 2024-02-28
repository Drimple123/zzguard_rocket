package freechips.rocketchip.zzguardrr

import chisel3._
import chisel3.util._

class instruction_cat extends Module{
  val io = IO(new Bundle{
    val in_1    =   Input(UInt(40.W))
    val in_2    =   Input(UInt(32.W))
    val in_3    =   Input(UInt(64.W))
    val sel     =   Input(UInt(2.W))
    val out     =   Output(UInt(104.W))

    val ready   =   Input(Bool())
    //val valid   =   Output(Bool())
  })
  
  val out_w = WireDefault(0.U(104.W))

  //val out_r = RegInit(0.U(4.W))

  when(io.sel === 1.U && io.ready){
    out_w := Cat(io.in_1, io.in_2)
    //out_r := Cat(io.in_1, io.in_2)
  }
  .elsewhen(io.sel === 2.U && io.ready){
    out_w := Cat(io.in_1, io.in_3)
    //out_r := Cat(io.in_1, io.in_3)
  }
  .otherwise{
    //out_w := 0.U
    //out_r := 0.U
  }

  //io.valid := true.B
  io.out   := out_w
  //io.out := out_r
  
}

