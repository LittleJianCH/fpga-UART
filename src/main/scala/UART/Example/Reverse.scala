package UART.Example

import chisel3._
import chisel3.util._

import UART._
import UART.util._

class Reverse(maxLen: Int) extends Module {
  val clock_freq = 12_000_000
  val uart_bps = 115200

  val io = IO(new Bundle {
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  withReset(!reset.asBool) {
    val uartRx = Module(new UartRx(clock_freq, uart_bps))
    val writeBuff = Module(new WriteBuff(maxLen, clock_freq, uart_bps))

    val ptr = RegInit(0.U(6.W))
    val dataBuff = Reg(Vec(maxLen, UInt(8.W)))

    val lenR = RegInit(0.U(log2Ceil(maxLen).W))
    val buffR = Reg(Vec(maxLen, UInt(8.W)))
    val enR = RegInit(false.B)

    writeBuff.io.buff := buffR
    writeBuff.io.len := lenR
    writeBuff.io.en := enR

    val doneNext = RegNext(uartRx.io.done, false.B)

    uartRx.io.rx := io.rx
    io.tx := writeBuff.io.tx

    when(writeBuff.io.idle && uartRx.io.done && !doneNext) {
      val data = uartRx.io.data
      when(data === '\r'.asUInt) {
        buffR := MuxLookup(ptr, VecInit(Seq.fill(maxLen)(0.U(8.W))),
          (1 until (maxLen - 2)).map(i =>
            i.U -> VecInit(dataBuff.take(i).reverse ++
                           Seq('\n', '\r').map(_.asUInt) ++
                           Seq.fill(maxLen - i - 2)(0.U))
          )
        )
        enR := true.B
        lenR := ptr + 2.U
        ptr := 0.U
      } .otherwise {
        dataBuff(ptr) := data
        ptr := ptr + 1.U
      }
    } .otherwise {
      enR := false.B
    }
  }
}

object ReverseGen extends App {
  chisel3.emitVerilog(new Reverse(16), Array("--target-dir", "gen"))
}
