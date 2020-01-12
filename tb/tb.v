
`timescale 1ns/1ns

module tb;

    reg clk;

    initial begin
        $dumpfile("waves.vcd");
        $dumpvars;

        clk = 0;

        repeat(80000)
            @(posedge clk);

        $finish;
    end

    always
        #40 clk = !clk;

    wire [3:0] leds;
    wire [2:0] hub75_row;

    CubeTop u_dut(
        .clk25(clk),
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

    localparam cols = 64;

    reg [23:0] led_values [0:cols*16-1];

    reg sclk_d;
    reg [2:0] row_d;

    reg [cols-1:0] r0_shift;
    reg [cols-1:0] g0_shift;
    reg [cols-1:0] b0_shift;

    reg [cols-1:0] r1_shift;
    reg [cols-1:0] g1_shift;
    reg [cols-1:0] b1_shift;

    reg [cols-1:0] r0_lat;
    reg [cols-1:0] g0_lat;
    reg [cols-1:0] b0_lat;

    reg [cols-1:0] r1_lat;
    reg [cols-1:0] g1_lat;
    reg [cols-1:0] b1_lat;

    reg [2:0] bit_nr;


    integer i;

    initial begin
        bit_nr  = 0;
    end

    always @(posedge clk) begin
        if (hub75_clk && !sclk_d) begin
            r0_shift <= { r0_shift[cols-2:0], hub75_r0 }; 
            g0_shift <= { g0_shift[cols-2:0], hub75_g0 }; 
            b0_shift <= { b0_shift[cols-2:0], hub75_b0 }; 

            r1_shift <= { r1_shift[cols-2:0], hub75_r1 }; 
            g1_shift <= { g1_shift[cols-2:0], hub75_g1 }; 
            b1_shift <= { b1_shift[cols-2:0], hub75_b1 }; 
        end

        if (hub75_row < row_d) begin
            bit_nr  <= 0;

            $writememh("ledvalues.hex", led_values, 0, cols*16-1);
        end

        sclk_d  <= hub75_clk;
        row_d   <= hub75_row;

    end

    always @(posedge hub75_lat) begin
        r0_lat  <= r0_shift;
        g0_lat  <= g0_shift;
        b0_lat  <= b0_shift;

        r1_lat  <= r1_shift;
        g1_lat  <= g1_shift;
        b1_lat  <= b1_shift;

        bit_nr  <= bit_nr + 1;
    end

    reg [15:0] led_addr;

    always @(negedge hub75_oe_) begin
        for(i = 0; i<cols; i = i+1) begin
            led_addr = hub75_row * cols;
            led_addr = led_addr + 1;

            led_values[led_addr][bit_nr     ] <= r0_lat[i];
            led_values[led_addr][bit_nr +  8] <= g0_lat[i];
            led_values[led_addr][bit_nr + 16] <= b0_lat[i];

            led_addr = led_addr + 8 * cols;
            led_values[led_addr][bit_nr     ] <= r0_lat[i];
            led_values[led_addr][bit_nr +  8] <= g0_lat[i];
            led_values[led_addr][bit_nr + 16] <= b0_lat[i];
        end
    end


endmodule

