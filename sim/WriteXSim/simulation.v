`timescale 10ns/10ns
`include "../../gen/WriteX.v"

module simulation;
  reg CP, RST, en;

  wire out;

  always #1 CP = !CP;

  WriteX wx(CP, RST, en, out);

  initial begin
    $dumpfile("wave.vcd");
    $dumpvars(0, simulation);

    CP = 0;
    
    #1000

    RST = 0; #5 RST = 1; #500

    en = 1; #5 en = 0; #5 en = 1; #10000

    en = 1; #5 en = 0; #500 en = 1; #10000

    #100000

    $finish;
  end
endmodule
