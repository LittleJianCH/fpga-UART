package UART.Example

import chisel3._

import UART._

class WriteX extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val tx = Output(Bool())
  })

  val data = 'x'.toInt.asUInt(8.W)

  withReset(!reset.asBool) {
    val uartTx = Module(new UartTx(12_000_000, 115200))

    uartTx.io.data := data
    uartTx.io.en := !io.en

    io.tx := uartTx.io.tx
  }
}

object WriteXGen extends App {
  chisel3.emitVerilog(new WriteX, Array("--target-dir", "gen"))
}