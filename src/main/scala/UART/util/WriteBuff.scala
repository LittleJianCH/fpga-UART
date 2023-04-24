package UART.util

import chisel3._
import chisel3.util._
import chisel3.experimental._

import UART._

class WriteBuff(maxLen: Int, clock_freq: Int, uart_bps: Int) extends Module {
  object State extends ChiselEnum {
    val idle, run = Value
  }

  val io = IO(new Bundle {
    val buff = Input(Vec(maxLen, UInt(8.W)))
    val len = Input(UInt(log2Ceil(maxLen).W))
    val en = Input(Bool())
    val tx = Output(Bool())
    val idle = Output(Bool())
  })

  val uartTx = Module(new UartTx(clock_freq, uart_bps))

  val state = RegInit(State.idle)

  val ptr = RegInit(0.U(log2Ceil(maxLen + 1).W))

  val enR = RegInit(false.B)
  val dataR = RegInit(0.U(8.W))

  val delay = RegInit(true.B)

  delay := !delay

  uartTx.io.en := enR
  uartTx.io.data := dataR

  io.idle := (state === State.idle)
  io.tx := uartTx.io.tx

  when (delay) {
    switch (state) {
      is (State.idle) {
        when (io.en) {
          state := State.run
          ptr := 0.U
        } .otherwise {
          state := State.idle
        }
      }

      is (State.run) {
        enR := false.B

        when (uartTx.io.idle) {
          when (ptr === io.len) {
            state := State.idle
          } .otherwise {
            state := State.run
            enR := true.B
            dataR := io.buff(ptr)
            ptr := ptr + 1.U
          }
        }
      }
    }
  }
}
