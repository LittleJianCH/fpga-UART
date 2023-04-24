package UART

import chisel3._
import chisel3.util._
import chisel3.experimental._

class UartRx(clock_freq: Int, uart_bps: Int) extends Module {
  val bps_cnt = clock_freq / uart_bps

  object State extends ChiselEnum {
    val idle, start, rxData = Value
  }

  val io = IO(new Bundle {
    val rx = Input(Bool())
    val data = Output(UInt(8.W))
    val done = Output(Bool())
  })

  val rxNext = RegNext(io.rx, false.B)

  val dataReg = Reg(Vec(8, UInt(1.W)))
  val doneReg = RegInit(false.B)

  io.data := dataReg.asUInt
  io.done := doneReg

  val cntClock = RegInit(0.U(log2Ceil(bps_cnt).W))
  val cntBit = RegInit(0.U(3.W))
  val state = RegInit(State.idle)

  val nextCntClock = Mux(cntClock === (bps_cnt - 1).U, 0.U, cntClock + 1.U)

  switch (state) {
    is (State.idle) {
      when (rxNext && !io.rx) {
        state := State.start
        doneReg := false.B
        cntClock := 0.U
      } .otherwise {
        cntClock := nextCntClock
      }
    }

    is (State.start) {
      when (cntClock === (bps_cnt - 1).U) {
        state := State.rxData
        cntBit := 0.U
      }
      cntClock := nextCntClock
    }

    is (State.rxData) {
      when (cntClock === (bps_cnt / 2).U) {
        dataReg(cntBit) := io.rx
      } .elsewhen(cntClock === (bps_cnt - 1).U) {
        when (cntBit === 7.U) {
          state := State.idle
          doneReg := true.B
        } .otherwise {
          cntBit := cntBit + 1.U
        }
      }
      cntClock := nextCntClock
    }
  }
}