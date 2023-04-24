package UART

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class UartTest extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
    val done = Output(Bool())
  })

  val uartTx1 = Module(new UartTx(5, 1))
  val uartRx1 = Module(new UartRx(5, 1))
  val uartTx2 = Module(new UartTx(5, 1))
  val uartRx2 = Module(new UartRx(5, 1))

  val reg = RegInit(0.U(2.W))

  switch (reg) {
    is (0.U) {
      when (uartRx1.io.done) {
        reg := 1.U
      }
    }

    is (1.U) {
      when (!uartRx2.io.done) {
        reg := 2.U
      }
    }

    is (2.U) {
      when (uartRx2.io.done) {
        reg := 3.U
      }
    }

    is (3.U) {
      when (!uartRx1.io.done) {
        reg := 0.U
      }
    }
  }

  val doneNext = RegNext(uartRx1.io.done, false.B)

  uartTx1.io.data := io.dataIn
  uartTx1.io.en := io.en

  uartRx1.io.rx := uartTx1.io.tx

  uartTx2.io.data := uartRx1.io.data
  uartTx2.io.en := uartRx1.io.done && !doneNext

  uartRx2.io.rx := uartTx2.io.tx

  io.dataOut := uartRx2.io.data
  io.done := (reg === 3.U)
}

object UartTestGen extends App {
  chisel3.emitVerilog(new UartTest, Array("--target-dir", "gen"))
}

class UartTestSpec extends AnyFreeSpec with ChiselScalatestTester {
  val TEST_TIMES = 200;

  "UartTest" in {
    test(new UartTest) { c =>
      for(i <- 0 until TEST_TIMES) {
        val data = scala.util.Random.nextInt(256)

        c.io.dataIn.poke(data.U)
        c.io.en.poke(true.B)

        do {
          c.clock.step(1)
        } while (c.io.done.peekBoolean())

        c.io.en.poke(false.B)

        do {
          c.clock.step(1)
        } while (!c.io.done.peekBoolean())

        c.io.dataOut.expect(data.U)
      }
    }
  }
}
