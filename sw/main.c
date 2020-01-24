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



#define WAIT_CYCLES 4000000

void led_mem_fill(int buffer_nr, unsigned char r, unsigned char g, unsigned char b)
{
    for(int i=0;i<32*32;++i){
        MEM_WR(LED_MEM, buffer_nr * 32 * 32 +i, r | (g<<8) | (b<<16));
    }
}

void led_mem_wr(int buffer_nr, int side, int x, int y, unsigned char r, unsigned char g, unsigned char b)
{
        MEM_WR(LED_MEM, buffer_nr * 3 * 32 * 32 + side * 32 * 32 + y*32 + x, r | (g<<8) | (b<<16));
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
    unsigned char *ptr = ricks_bin + frame_nr * 32 * 23;

    for(int side = 0; side < 1; ++side){
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



void hub75_init()
{
    REG_WR_FIELD(HUB75S_CONFIG, ENABLE, 1);
    REG_WR_FIELD(HUB75S_CONFIG, START, 1);
    REG_WR_FIELD(HUB75S_CONFIG, AUTO_RESTART, 1);
    REG_WR_FIELD(HUB75S_CONFIG, BUFFER_NR, 0);
}

int hub75_get_scratch_buffer()
{
    int row;
    do{
        row = REG_RD_FIELD(HUB75S_STATUS, CUR_ROW_NR);
    }
    while(row != 1);

    return !REG_RD_FIELD(HUB75S_STATUS, CUR_BUFFER_NR);
}

int main() {

//    led_mem_fill(128, 64, 32);

    hub75_streamer_init();
//    led_mem_stripes_rick();

    REG_WR(LED_DIR, 0xff);

//    led_render_clear_leds();

//    while(1){
//        led_mem_effect();
//   }

//    led_mem_rows(0);
    led_mem_rick(0,0);
    hub75_init();

    uint32_t movie_frame = 0;
    //uint32_t scratch_buf = hub75_get_scratch_buffer();
    uint32_t scratch_buf = 1;

    while(1){
        led_mem_rick(scratch_buf, movie_frame);
        movie_frame = (movie_frame + 1) % 16;

        uint32_t prev_frame_cntr = REG_RD(HUB75S_FRAME_CNTR);
        while(REG_RD(HUB75S_FRAME_CNTR) < prev_frame_cntr + 14) ;

        REG_WR_FIELD(HUB75S_CONFIG, BUFFER_NR, scratch_buf);
        while(REG_RD_FIELD(HUB75S_STATUS, CUR_BUFFER_NR) != scratch_buf) 
            ;

        scratch_buf ^= 1;
    }

    while(1){
        REG_WR(LED_WRITE, 0x00);
        REG_WR(LED_MEM, 0x01);
        wait(WAIT_CYCLES);
        REG_WR(LED_MEM, 0x00);

        REG_WR(LED_WRITE, 0x02);
        wait(WAIT_CYCLES);
        REG_WR(LED_WRITE, 0x04);
        wait(WAIT_CYCLES);
    }

    while(1);
}
