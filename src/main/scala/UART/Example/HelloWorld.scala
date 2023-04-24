package UART.Example

import chisel3._
import chisel3.util._
import chisel3.experimental._

import UART._

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
    val uartTx = Module(new UartTx(12_000_000, 115200))

    val state = RegInit(State.idle)

    val ptr = RegInit(0.U(4.W))

    val enR = RegInit(false.B)
    val dataR = RegInit(0.U(8.W))

    val delay = RegInit(0.U(1.W))

    uartTx.io.en := enR
    uartTx.io.data := dataR
    io.tx := uartTx.io.tx

    delay := delay + 1.U
    // add a delay to make sure the UART is ready

    when(delay === 0.U) {
      switch(state) {
        is(State.idle) {
          when(!io.en) {
            state := State.run
            ptr := 0.U
          }.otherwise {
            state := State.idle
          }
        }

        is(State.run) {
          enR := false.B

          when(uartTx.io.idle) {
            when(ptr === message.length.U) {
              state := State.idle
            }.otherwise {
              state := State.run
              enR := true.B
              dataR := message(ptr)
              ptr := ptr + 1.U
            }
          }
        }
      }
    }
  }
}

object HelloWorldGen extends App {
  chisel3.emitVerilog(new HelloWorld, Array("--target-dir", "gen"))
}