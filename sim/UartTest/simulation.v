`timescale 10ns/10ns
`include "../../gen/UartTest.v"

module simulation;
  reg CP, RST;

  reg [7:0] data;
  wire [7:0] out;

  reg en;
  wire done;

  UartTest ut(CP, RST, en, data, out, done);

  always #1 CP = !CP;

  initial begin
    $dumpfile("wave.vcd");
    $dumpvars(0, simulation);

    CP = 0; RST = 1; #5 RST = 0;

    en = 1; data = 42; #5;

    en = 0; #900;

    en = 1; data = 23; #5;

    en = 0; #900;

    $finish;
  end
endmodule
