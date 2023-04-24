`timescale 10ns/10ns
`include "../../gen/HelloWorld.v"

module simulation;
  reg CP, RST, en;

  wire Out;

  always #1 CP = !CP;

  HelloWorld hw(CP, RST, en, Out);

  initial begin
    $dumpfile("wave.vcd");
    $dumpvars(0, simulation);

    CP = 0; en = 0;

    RST = 1; #5 RST = 0; #500 RST = 1; #50

    en = 1; #5 en = 0; #5 en = 1; #100000

    en = 1; #5 en = 0; #500 en = 1; #100000

    $finish;
  end
endmodule
