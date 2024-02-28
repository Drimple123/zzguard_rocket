package freechips.rocketchip.zzguardrr

import chisel3._
import chisel3.util._

class look_table_rom extends Module {
  val io = IO(new Bundle {
    val addr      = Input(UInt(2.W))
    val data_out  = Output(UInt(2.W))
  })

  val rom = VecInit(1.U, 2.U, 0.U, 3.U)

  //val data = RegInit(0.U(2.W))
  
  //data := rom(io.addr)

  //io.data_out := data

  io.data_out := rom(io.addr)
}

