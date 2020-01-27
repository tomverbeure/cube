#include <stdint.h>
#include <math.h>

#include "reg.h"
#include "top_defines.h"
//#include "led_render.h"
#include "hub75_streamer.h"

#include "../movie/palette.h"
#include "../movie/ricks.h"

static inline uint32_t rdcycle(void) {
    uint32_t cycle;
    asm volatile ("rdcycle %0" : "=r"(cycle));
    return cycle;
}

static inline int nop(void) {
    asm volatile ("addi x0, x0, 0");
    return 0;
}

void wait(int cycles)
{
#if 1
    volatile int cnt = 0;

    for(int i=0;i<cycles/20;++i){
        ++cnt;
    }
#else
    int start;

    start = rdcycle();
    while ((rdcycle() - start) <= cycles);
#endif
}


uint16_t pacman_closed[11] = {
    0b00011111000,
    0b00111111100,
    0b01111111110,
    0b11111111111,
    0b11111111111,
    0b11111111111,
    0b11111111111,
    0b11111111111,
    0b01111111110,
    0b00111111100,
    0b00011111000,
};

uint16_t pacman_open[11] = {
    0b00011111000,
    0b00111111100,
    0b01111111110,
    0b00011111111,
    0b00000111111,
    0b00000001111,
    0b00000111111,
    0b00011111111,
    0b01111111110,
    0b00111111100,
    0b00011111000,
};

uint16_t pacman_test[11] = {
    0b10000000000,
    0b11000000000,
    0b10100000000,
    0b10010000000,
    0b10001000000,
    0b10000100000,
    0b10000010000,
    0b10000001000,
    0b10000000100,
    0b10000000010,
    0b10000000001,
};

uint32_t ghost_left_0[14] = {
    0b0000000000010101010000000000,
    0b0000000101010101010101000000,
    0b0000010101010101010101010000,
    0b0001010101010101010101010100,
    0b0001010111110101010111110100,
    0b0001011111111101011111111100,
    0b0101011111101001011111101001,
    0b0101011111101001011111101001,
    0b0101010111110101010111110101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010100010101010001010101,
    0b0001010000000101000000010100,
};

uint32_t ghost_left_1[14] = {
    0b0000000000010101010000000000,
    0b0000000101010101010101000000,
    0b0000010101010101010101010000,
    0b0001010101010101010101010100,
    0b0001010111110101010111110100,
    0b0001011111111101011111111100,
    0b0101011111101001011111101001,
    0b0101011111101001011111101001,
    0b0101010111110101010111110101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101000101010000010101000101,
    0b0100000001010000010100000001,
};


uint32_t ghost_up_0[14] = {
    0b0000000000010101010000000000,
    0b0000001010010101011010000000,
    0b0000111010110101111010110000,
    0b0001111111110101111111110100,
    0b0001111111110101111111110100,
    0b0001011111010101011111010100,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010100010101010001010101,
    0b0001010000000101000000010100,
};

uint32_t ghost_up_1[14] = {
    0b0000000000010101010000000000,
    0b0000001010010101011010000000,
    0b0000111010110101111010110000,
    0b0001111111110101111111110100,
    0b0001111111110101111111110100,
    0b0001011111010101011111010100,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101000101010000010101000101,
    0b0100000001010000010100000001,
};

uint32_t ghost_down_0[14] = {
    0b0000000000010101010000000000,
    0b0000000101010101010101000000,
    0b0000010101010101010101010000,
    0b0001010101010101010101010100,
    0b0001011111010101011111010100,
    0b0001111111110101111111110100,
    0b0101111111110101111111110101,
    0b0101111010110101111010110101,
    0b0101011010010101011010010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010100010101010001010101,
    0b0001010000000101000000010100,
};

uint32_t ghost_down_1[14] = {
    0b0000000000010101010000000000,
    0b0000000101010101010101000000,
    0b0000010101010101010101010000,
    0b0001010101010101010101010100,
    0b0001011111010101011111010100,
    0b0001111111110101111111110100,
    0b0101111111110101111111110101,
    0b0101111010110101111010110101,
    0b0101011010010101011010010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101000101010000010101000101,
    0b0100000001010000010100000001,
};



uint32_t ghost_scared_0[14] = {
    0b0000000000010101010000000000,
    0b0000000101010101010101000000,
    0b0000010101010101010101010000,
    0b0001010101010101010101010100,
    0b0001010101010101010101010100,
    0b0001010111110101111101010100,
    0b0101010111110101111101010101,
    0b0101010101010101010101010101,
    0b0101010101010101010101010101,
    0b0101111101011111010111110101,
    0b0111010111110101111101011101,
    0b0101010101010101010101010101,
    0b0101000101010000010101000101,
    0b0100000001010000010100000001,
};


uint32_t cherry[12] = {
    0b000000000000000000001010,
    0b000000000000000010101010,
    0b000000000000101000100000,
    0b000000000010000000100000,
    0b000101011000000010000000,
    0b010101100101001000000000,
    0b010101010100011001010000,
    0b011101010001011001010100,
    0b010111010001010101010100,
    0b000101010001110101010100,
    0b000000000001011101010100,
    0b000000000000010101010000,
};

uint16_t dot_small[2] = {
    0b11,
    0b11,
};

uint16_t dot_large[8] = {
    0b00111100,
    0b01111110,
    0b11111111,
    0b11111111,
    0b11111111,
    0b11111111,
    0b01111110,
    0b00111100,
};


uint32_t pac_color = 0x36fffe;

uint32_t ghost_pink_colors[4]    = { 0x000000, 0xcb98ff, 0xff0000, 0xffffff };
uint32_t ghost_cyan_colors[4]    = { 0x000000, 0xfffe2c, 0xff0000, 0xffffff };
uint32_t ghost_red_colors[4]     = { 0x000000, 0x0711ff, 0xff0000, 0xffffff };
uint32_t ghost_orange_colors[4]  = { 0x000000, 0x42cfff, 0xff0000, 0xffffff };
uint32_t ghost_scared_colors[4]  = { 0x000000, 0xfb1313, 0x000000, 0x42cfff };

uint32_t cherry_colors[4]        = { 0x000000, 0x0711ff, 0x5599de, 0xffffff };

uint32_t border_color = 0xfb0022;

uint32_t dot_color = 0x92b1f8;


#define WAIT_CYCLES 4000000

void led_mem_wr(int buffer_nr, int side, int x, int y, unsigned char r, unsigned char g, unsigned char b)
{
        MEM_WR(LED_MEM, buffer_nr * 6 * 32 * 32 + side * 32 * 32 + y*32 + x, r | (g<<8) | (b<<16));
}

void led_mem_fill(int buffer_nr, unsigned char r, unsigned char g, unsigned char b)
{
    for(int side = 0; side < 6; ++side){
	    for(int row=0;row<32;++row){
	        for(int col=0;col<32;++col){
	            led_mem_wr(buffer_nr, side, col, row, r, g, b);
	        }
	    }
    }
}

void led_mem_clear(int buffer_nr)
{
    led_mem_fill(buffer_nr, 0, 0, 0);
}

void led_mem_effect(int buffer_nr)
{
    unsigned char r,g,b;

    for(r=0;r<255;++r){
        for(g=0;g<255;++g){
            for(b=0;b<255;++b){
                led_mem_fill(buffer_nr, r,g,b);
            }
        }
    }
}

void led_mem_stripes(int buffer_nr)
{
    for(int side = 0; side < 6; ++side){
	    for(int row=0;row<32;++row){
	        for(int col=0;col<32;++col){
	            led_mem_wr(buffer_nr, side, col, row, 
	                            (col % 3) == 0 ? 255 : 0, 
	                            (col % 3) == 1 ? 255 : 0, 
	                            (col % 3) == 2 ? 255 : 0);
	        }
	    }
    }
}

void led_mem_stripes_rick()
{

    for(int frame_nr = 0; frame_nr < 16; ++frame_nr){

        unsigned char *ptr = ricks_bin + frame_nr * 32 * 23;

        for(int row=0; row<23; ++row){
            for(int col=0;col<32;++col){
                if (col == frame_nr*2){
                    ptr[row * 32 + col] = 10;
                }
                else{
                    ptr[row * 32 + col] = 0;
                }
            }
        }
    }
}

void led_mem_rows(int buffer_nr)
{
    for(int side=0; side<6; side++){
	    for(int row=0;row<32;++row){
	        for(int col=0;col<32;++col){
	            if (col<10){
	                led_mem_wr(buffer_nr, side, col, row, row*7, 0, 0);
	            } 
	            else if (col<20){
	                led_mem_wr(buffer_nr, side, col, row, 0, row*7, 0);
	            }
	            else if (col<30){
	                led_mem_wr(buffer_nr, side, col, row, 0, 0, row*7);
	            }
	            else{
	                led_mem_wr(buffer_nr, side, col, row, 0, row*7, row*7);
	            }
	        }
	    }
    }
}

void led_mem_rick(int buffer_nr, int frame_nr)
{

    for(int side = 0; side < 6; ++side){

        int frame_nr_adj = (frame_nr + side) % 16;
        unsigned char *ptr = ricks_bin + frame_nr_adj * 32 * 23;

	    for(int row=0; row<32; ++row){
	        for(int col=0;col<32;++col){
	            if (row < 4 || row >= 27){
	                led_mem_wr(buffer_nr, side, col, row, side == 0 || side == 3 ? 32 : 0, side == 1 || side == 4 ? 32 : 0 , side == 2 || side == 5 ? 32 : 0);
	            }
	            else{
	                unsigned char val = *ptr;
	                led_mem_wr(buffer_nr, side, col, row, 
	                                palette_bin[val * 3],
	                                palette_bin[val * 3 + 1],
	                                palette_bin[val * 3 + 2]);
	                ++ptr;
	            }
	
	        }
	    }
    }
}

void render_bitmap_1bpp(uint16_t *bitmap, uint32_t color, int size_x, int size_y, int buffer_nr, e_hub75_ring ring, int pos_x, int pos_y)
{
    for(int y=0; y<size_y;++y){
        for(int x=0; x<size_x;++x){
                //uint32_t bit = (bitmap[y] >> (size_x-1-x)) & 1;
                uint32_t bit = (bitmap[y] >> (x)) & 1;
                if (!bit)
                    continue;

                uint32_t log_addr = ring * HUB75S_RING_SIZE 
                                    + ((pos_y+y) + HUB75S_SIDE_HEIGHT) * HUB75S_STRIP_WIDTH 
                                    + (pos_x+x);
                    
                uint32_t phys_addr = hub75s_calc_phys_addr(buffer_nr, log_addr);

                MEM_WR(LED_MEM, phys_addr, color);
        }
    }
}

void render_bitmap_2bpp(uint32_t *bitmap, uint32_t *colors, int size_x, int size_y, int buffer_nr, e_hub75_ring ring, int pos_x, int pos_y)
{
    for(int y=0; y<size_y;++y){
        for(int x=0; x<size_x;++x){
                //uint32_t bit = (bitmap[y] >> (size_x-1-x)) & 1;
                uint32_t bits = (bitmap[y] >> (2*size_x-2 - 2*x)) & 3;
                if (bits == 0)
                    continue;

                uint32_t log_addr = ring * HUB75S_RING_SIZE 
                                    + ((pos_y+y) + HUB75S_SIDE_HEIGHT) * HUB75S_STRIP_WIDTH 
                                    + (pos_x+x);
                    
                uint32_t phys_addr = hub75s_calc_phys_addr(buffer_nr, log_addr);

                uint32_t color = colors[bits];

                MEM_WR(LED_MEM, phys_addr, color);
        }
    }
}

int play_rick()
{
    uint32_t movie_frame = 0;
    uint32_t scratch_buf = 1;

    int pos_x = 0;

    while(1){
        led_mem_clear(scratch_buf);
        led_mem_rick(scratch_buf, movie_frame);
        movie_frame = (movie_frame + 1) % 16;

        pos_x = (pos_x + 1) % (4 * HUB75S_SIDE_WIDTH);

        uint32_t prev_frame_cntr = REG_RD(HUB75S_FRAME_CNTR);
        while(REG_RD(HUB75S_FRAME_CNTR) < prev_frame_cntr + 14) ;

        REG_WR_FIELD(HUB75S_CONFIG, BUFFER_NR, scratch_buf);
        while(REG_RD_FIELD(HUB75S_STATUS, CUR_BUFFER_NR) != scratch_buf) 
            ;

        scratch_buf ^= 1;
    }

}

int play_pacman()
{
    uint32_t scratch_buf = 1;

    int pos_x = 0;
    int pos_y = 10;

    while(1){
        led_mem_clear(scratch_buf);

        uint32_t *current_ghost = ghost_left_0;
        uint16_t *current_pac   = pacman_open;
        
        if (REG_RD(HUB75S_FRAME_CNTR) % 20 > 10){
            current_ghost = ghost_left_1;
            current_pac   = pacman_closed;
        }

        render_bitmap_1bpp(current_pac, pac_color, 11, 11, scratch_buf, RING_LFRBa, (pos_x) % (4*HUB75S_SIDE_WIDTH), pos_y);

        int chase_dist = 20;
        int ghost_delta = 12;

        render_bitmap_2bpp(current_ghost,   ghost_pink_colors,    14, 14, scratch_buf, RING_LFRBa, (pos_x - chase_dist - 0 * ghost_delta) % (4*HUB75S_SIDE_WIDTH), pos_y-2);
        render_bitmap_2bpp(current_ghost,   ghost_red_colors,     14, 14, scratch_buf, RING_LFRBa, (pos_x - chase_dist - 1 * ghost_delta) % (4*HUB75S_SIDE_WIDTH), pos_y-2);
        render_bitmap_2bpp(current_ghost,   ghost_orange_colors,  14, 14, scratch_buf, RING_LFRBa, (pos_x - chase_dist - 2 * ghost_delta) % (4*HUB75S_SIDE_WIDTH), pos_y-2);
        render_bitmap_2bpp(current_ghost,   ghost_cyan_colors,    14, 14, scratch_buf, RING_LFRBa, (pos_x - chase_dist - 3 * ghost_delta) % (4*HUB75S_SIDE_WIDTH), pos_y-2);

        render_bitmap_2bpp(ghost_scared_0, ghost_scared_colors,  14, 14, scratch_buf, RING_LFRBa, (pos_x + 30) % (4*HUB75S_SIDE_WIDTH), pos_y-2);

        render_bitmap_2bpp(cherry, cherry_colors, 12, 12, scratch_buf, RING_LFRBa, 10 + HUB75S_SIDE_WIDTH, 10-HUB75S_SIDE_WIDTH);

        pos_x = (pos_x + 1) % (4 * HUB75S_SIDE_WIDTH);

        uint32_t prev_frame_cntr = REG_RD(HUB75S_FRAME_CNTR);
        while(REG_RD(HUB75S_FRAME_CNTR) < prev_frame_cntr + 6) ;

        REG_WR_FIELD(HUB75S_CONFIG, BUFFER_NR, scratch_buf);
        while(REG_RD_FIELD(HUB75S_STATUS, CUR_BUFFER_NR) != scratch_buf) 
            ;

        scratch_buf ^= 1;
    }
}


int main() {

    hub75s_config();
    hub75s_start();

    REG_WR(LED_DIR, 0xff);

    hub75s_dim(0x40, 0x40, 0x40);

    //play_rick();
    play_pacman();

}
