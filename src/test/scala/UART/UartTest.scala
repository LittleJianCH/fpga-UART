package UART

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

class UartTest extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))
    val done = Output(Bool())
  })

  val uartTx = Module(new UartTx(5, 1))
  val uartRx = Module(new UartRx(5, 1))

  uartTx.io.data := io.dataIn
  uartTx.io.en := io.en

  io.dataOut := uartRx.io.data
  io.done := uartRx.io.done

  uartRx.io.rx := uartTx.io.tx
}

object UartTestGen extends App {
  chisel3.emitVerilog(new UartTest, Array("--target-dir", "gen"))
}

class UartTestSpec extends AnyFreeSpec with ChiselScalatestTester {
  val TEST_TIMES = 50;

  "UartTest" in {
    test(new UartTest) { c =>
      for(_ <- 0 until TEST_TIMES) {
        val data = scala.util.Random.nextInt(256)

        c.io.en.poke(true.B)
        c.io.dataIn.poke(data.U)
        c.clock.step(10)
        c.io.en.poke(false.B)
        c.clock.step(90)

        c.io.done.expect(true.B)
        c.io.dataOut.expect(data.U)
      }
    }
  }
}
