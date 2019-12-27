
`timescale 1ns/1ns

module tb;

    reg clk;

    initial begin
        $dumpfile("waves.vcd");
        $dumpvars;

        clk = 0;

        repeat(40000)
            @(posedge clk);

        $finish;
    end

    always
        #40 clk = !clk;

    wire [3:0] leds;
    wire [2:0] hub75_row;

    CubeTop u_dut(
        .osc_clk(clk),
        .hub75_clk(hub75_clk),
        .hub75_lat(hub75_lat),
        .hub75_oe_(hub75_oe_),
        .hub75_row(hub75_row),
        .hub75_r0(hub75_r0),
        .hub75_g0(hub75_g0),
        .hub75_b0(hub75_b0),
        .hub75_r1(hub75_r1),
        .hub75_g1(hub75_g1),
        .hub75_b1(hub75_b1),
        .leds(leds)
    );

endmodule
