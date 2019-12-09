
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


    wire led_r_, led_g_, led_b;

    LedMatrixTop u_dut(
        .OSC_CLK_IN(clk),
        .LED_R_(led_r_),
        .LED_G_(led_g_),
        .LED_B_(led_b_)
    );

endmodule
