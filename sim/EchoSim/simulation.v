`timescale 10ns/10ns
`include "../../gen/Echo.v"

module simulation;
  reg CP, RST;
  
  reg rx;

  reg [7:0] data;

  wire tx, led;

  Echo echo(CP, RST, rx, tx, led);

  integer i;

  always #1 CP = !CP;

  initial begin
    $dumpfile("wave.vcd");
    $dumpvars(0, simulation);

    CP = 0; RST = 1; #5 RST = 0;

    rx = 1; #400

    data = 123;

    rx = 0; #208 // start
    
    for (i = 0; i < 8; i = i + 1) begin
      rx = data[i];
      #208;
    end

    rx = 1; #20000

    $finish;
  end
endmodule
