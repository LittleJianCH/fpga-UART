`timescale 10ns/10ns
`include "../../gen/Reverse.v"

module simulation;
  reg CP, RST, en;
  wire nRST;
  reg [7:0] data;

  wire tx, out, u1, u2;

  always #1 CP = !CP;

  UartTx utx(CP, nRST, en, data, tx, u1);

  Reverse rev(CP, RST, tx, out);

  assign nRST = !RST;

  initial begin
    $dumpfile("wave.vcd");
    $dumpvars(0, simulation);

    CP = 0;

    RST = 0; #5 RST = 1;

    data = 42; en = 1; #5 en = 0; #3000

    data = 23; en = 1; #5 en = 0; #3000

    data = 13; en = 1; #5 en = 0; #300000

    $finish;
  end
endmodule
