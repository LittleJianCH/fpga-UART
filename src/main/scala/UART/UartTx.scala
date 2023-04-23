package UART

import chisel3._
import chisel3.util._
import chisel3.experimental._

class UartTx(clock_freq: Int, uart_bps: Int) extends Module {
  val bps_cnt = clock_freq / uart_bps

  object State extends ChiselEnum {
    val idle, start, txData = Value
  }

  val io = IO(new Bundle {
    val en = Input(Bool())
    val data = Input(UInt(8.W))
    val tx = Output(Bool())
  })

  val state = RegInit(State.idle)
  val cntClock = RegInit(0.U(log2Ceil(bps_cnt).W))
  val cntBit = RegInit(0.U(3.W))

  io.tx := MuxCase(1.B, Array(
    (state === State.idle) -> 1.B,
    (state === State.start) -> 0.B,
    (state === State.txData) -> io.data(cntBit)
  ))

  switch (state) {
    is (State.idle) {
      when (io.en) {
        state := State.start
        cntClock := 0.U
      }
    }

    is (State.start) {
      when (cntClock === (bps_cnt - 1).U) {
        state := State.txData
        cntBit := 0.U
      }
    }

    is (State.txData) {
      when (cntClock === (bps_cnt - 1).U) {
        when (cntBit === 7.U) {
          state := State.idle
        } .otherwise {
          cntBit := cntBit + 1.U
        }
      }
    }
  }

  cntClock := Mux(cntClock === (bps_cnt - 1).U, 0.U, cntClock + 1.U)
}