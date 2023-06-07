# fpga-UART

An UART lib written in Chisel3.

## Build

```shell
sbt run
```

Select the app you want to generate by type the number in front.

Program the verilog code in `gen/` to your fpga board. For example, if you're using Xilinx board, you need to copy the verilog file into Vivado to program your board.

## Test

```shell
sbt test
```

## Navigation

The most source code is in `src/main/scala/UART/`, in short, name it `src-code`.

The core implement code of UART is in `src-code/UartRx.scala` and `src-code/UartTx.scala`.

And there are some examples showing how to use this lib in `src-code/Example`. 