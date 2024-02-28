package freechips.rocketchip.zzguardrr
//i am very handsome
import chisel3._
import chisel3.util._

class zzguardrr extends Module{
  val io = IO(new Bundle{
    //val addr        =   Input(UInt(2.W))
    val valid       =   Input(Bool())
    val din_pc      =   Input(UInt(40.W))
    val din_ins     =   Input(UInt(32.W))
    val din_wdata   =   Input(UInt(64.W))
  })
  
  dontTouch(io)

  val table = Module(new look_table_rom)
  table.io.addr    := io.din_ins(6,5)
  
  val cat = Module(new instruction_cat)
  cat.io.in_1  := io.din_pc
  cat.io.in_2  := io.din_ins
  cat.io.in_3  := io.din_wdata
  cat.io.sel   := table.io.data_out
  
  val dis = Module(new dis_fsm)
  dis.io.sel := table.io.data_out
  //io.num  := dis.io.num

  val en_valid = WireDefault(false.B)
  en_valid := MuxCase(false.B, Array(
     (table.io.data_out === 0.U) -> false.B,
     (table.io.data_out === 1.U && io.valid) -> true.B,
     (table.io.data_out === 2.U && io.valid) -> true.B,
     (table.io.data_out === 3.U) -> false.B
   ))
  

  //val num_r = RegInit(0.U(3.W))
  //num_r := dis.io.num
  //例化6个fifo
  val fifo = VecInit(Seq.fill(6)(Module(new RegFifo(UInt(104.W),3)).io))
  //例化6个zzz
  val zz = VecInit(Seq.fill(6)(Module(new zzz).io))

  for(i <- 0 to 5){
    //fifo的deq端接zzz
    fifo(i).deq.ready   := zz(i).ready
    zz(i).valid         := fifo(i).deq.valid
    zz(i).din           := fifo(i).deq.bits

    //instruction_cat接fifo的enq端
    fifo(i).enq.bits  := cat.io.out
    cat.io.ready      := fifo(i).enq.ready
    

    //num对应的fifo才valid
    when(i.asUInt === dis.io.num && en_valid){
      fifo(i).enq.valid := true.B
    }
    .otherwise{
      fifo(i).enq.valid := false.B
    }

  }






}

