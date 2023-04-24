package UART.Example

import chisel3._

import UART._

class Echo extends Module {
  val io = IO(new Bundle {
    val rx = Input(Bool())
    val tx = Output(Bool())
    val led = Output(Bool())
  })

  withReset(!reset.asBool) {
    val uartTx = Module(new UartTx(12_000_000, 115200))
    val uartRx = Module(new UartRx(12_000_000, 115200))

    val doneNext = RegNext(uartRx.io.done, false.B)

    uartTx.io.data := uartRx.io.data
    uartTx.io.en := uartRx.io.done && !doneNext

    io.tx := uartTx.io.tx
    uartRx.io.rx := io.rx

    io.led := !uartRx.io.done
  }
}

object EchoGen extends App {
  chisel3.emitVerilog(new Echo, Array("--target-dir", "gen"))
}
