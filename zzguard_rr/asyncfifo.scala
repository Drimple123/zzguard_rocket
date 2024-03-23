package freechips.rocketchip.zzguardrr

import chisel3._
import chisel3.util._


// class ClockDividerN(div: Int) extends BlackBox(Map("DIV" -> div)) with HasBlackBoxResource {
//   require(div > 0);
//   val io = IO(new Bundle {
//     val clk_out = Output(Clock())
//     val clk_in  = Input(Clock())
//   })
//   addResource("vsrc/ClockDividerN.sv")
// }

class asyncfifo(depth: Int, width: Int) extends Module{
  val io = IO(new Bundle{
    val clk_r   = Input(Clock())
    val wen     = Input(Bool())
    val wdata   = Input(UInt(width.W))
    //val full    = Output(Bool())
    val ren     = Input(Bool())
    val rdata   = Output(UInt(width.W))
    //val empty   = Output(Bool())
  })

  //分下clock
//   val clk_div = Module(new ClockDividerN(2))
//   val clk_r = Wire(Clock())  //读时钟
//   clk_div.io.clk_in := clock
//   clk_r := clk_div.io.clk_out
  

  dontTouch(io)
  val pw = log2Ceil(depth) + 1 //指针的位数
  val num = WireDefault(0.U(pw.W))
  val num_r = RegInit(0.U(pw.W))
  dontTouch(num)
  dontTouch(num_r)
  // val rpw = WireDefault(0.U(pw.W))
  
  // rpw(pw-1) := rpg_2(pw-1)
  // for(i<-(pw-2) to 0){
  //   rpw(i) := 
  // }

  //val mem = VecInit(Seq.fill(depth)(Reg(UInt(width.W))))//搞一组寄存器
  val mem = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))
  dontTouch(mem)
  val wp    = RegInit(0.U(pw.W))//写指针
  val wp_n  = WireDefault(0.U(pw.W))
  val full  = WireDefault(false.B)
  dontTouch(full)
  val wpg   = WireDefault(0.U(pw.W))

  val rp_n  = WireDefault(0.U(pw.W))
  val rpg   = WireDefault(0.U(pw.W))
  withClock(io.clk_r){
    val rp    = RegInit(0.U(pw.W))//读指针
    
    val empty = WireDefault(true.B)
    //val empty_r = RegInit(true.B)
    dontTouch(empty)

    rp_n := Mux((io.ren && !empty), rp + 1.U, rp)//如果读了, rp+1
    rp := rp_n

    //将写指针转换为格雷码
    rpg := (rp >> 1.U) ^ rp

    //读fifo, 读地址要忽略rp的最高位
    when(io.ren && !empty){
      io.rdata := mem(rp(pw-2, 0))
    }
    .otherwise{
      io.rdata := 0.U
    }
    //io.rdata := mem(rp(pw-2, 0))

    //wpg打两拍
    val wpg_1 = RegNext(wpg, 0.U)
    val wpg_2 = RegNext(wpg_1, 0.U)

    //判断是否空
    when(rpg === wpg_2){
        empty := true.B
    }
    .otherwise{
        empty := false.B
    }
  }

  wp_n := Mux((io.wen && !full), wp + 1.U, wp)//如果写了, wp+1
  wp := wp_n

  //将写指针转换为格雷码
  wpg := (wp >> 1.U) ^ wp

  //写fifo ,写地址要忽略wp的最高位
  when(io.wen && !full){
    mem(wp(pw-2, 0)) := io.wdata
  }

  //rpg打两拍
  val rpg_1 = RegNext(rpg, 0.U)
  val rpg_2 = RegNext(rpg_1, 0.U)

  //rp打两拍
  val rp_1 = RegNext(rp_n, 0.U)
  val rp_2 = RegNext(rp_1, 0.U)

  //计算fifo中数据量
  when(wp >= rp_2){
    num := wp - rp_2
  }
  .otherwise{
    num := wp + (1.U << pw.U) - rp_2
  }

  when(wp >= rp_2){
    num_r := wp - rp_2
  }
  .otherwise{
    num_r := wp + (1.U << pw.U) - rp_2
  }



  //判断是否满
  when(wpg === Cat(~rpg_2(pw-1, pw-2), rpg_2(pw-3, 0))){
    full := true.B
  }
  .otherwise{
    full := false.B
  }

  //读时钟域

}

// class asyncfifo_out(depth: Int, width: Int) extends Module{
//   val io = IO(new Bundle{
//     val wen     = Input(Bool())
//     val wdata   = Input(UInt(width.W))
//     //val full    = Output(Bool())
//     val ren     = Input(Bool())
//     val rdata   = Output(UInt(width.W))
//     //val empty   = Output(Bool())
//   })
//   val fifo = Module(new asyncfifo(depth, width))
  
//   val clk_div = Module(new ClockDividerN(2))
//   val clk_r = Wire(Clock())  //读时钟
//   clk_div.io.clk_in := fifo.clock

//   fifo.io.clk_r := clk_div.io.clk_out
//   fifo.io.wen := io.wen
//   fifo.io.wdata := io.wdata
//   fifo.io.ren := io.ren
//   io.rdata := fifo.io.rdata
// }

