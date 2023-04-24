package UART.Example

import chisel3._
import chisel3.util._
import chisel3.experimental._

import UART._
import UART.util._

class HelloWorld extends Module {
  object State extends ChiselEnum {
    val idle, run = Value
  }

  val io = IO(new Bundle {
    val en = Input(Bool())
    val tx = Output(Bool())
  })

  val message = VecInit("Hello World!\n\r".map(_.toInt.asUInt(8.W)))

  withReset(!reset.asBool) {
    val writeBuff = Module(new WriteBuff(message.length, 12_000_000, 115200))

    writeBuff.io.buff := message
    writeBuff.io.len := message.length.U
    writeBuff.io.en := !io.en

    io.tx := writeBuff.io.tx
  }
}

object HelloWorldGen extends App {
  chisel3.emitVerilog(new HelloWorld, Array("--target-dir", "gen"))
}