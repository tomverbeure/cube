#include <stdint.h>
#include <math.h>

#include "reg.h"
#include "top_defines.h"

#include "../movie/palette.h"
#include "../movie/ricks.h"

#define NR_LEDS     384

const uint8_t gamma8[] = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,
    1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  2,  2,  2,
    2,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,  4,  5,  5,  5,
    5,  6,  6,  6,  6,  7,  7,  7,  7,  8,  8,  8,  9,  9,  9, 10,
    10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 15, 15, 16, 16,
    17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 24, 25,
    25, 26, 27, 27, 28, 29, 29, 30, 31, 32, 32, 33, 34, 35, 35, 36,
    37, 38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 50,
    51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68,
    69, 70, 72, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89,
    90, 92, 93, 95, 96, 98, 99,101,102,104,105,107,109,110,112,114,
    115,117,119,120,122,124,126,127,129,131,133,135,137,138,140,142,
    144,146,148,150,152,154,156,158,160,162,164,167,169,171,173,175,
    177,180,182,184,186,189,191,193,196,198,200,203,205,208,210,213,
    215,218,220,223,225,228,231,233,236,239,241,244,247,249,252,255 };


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

void led_mem_wr(int buffer_nr, int x, int y, unsigned char r, unsigned char g, unsigned char b)
{
        MEM_WR(LED_MEM, buffer_nr * 32 * 32 + y*32 + x, r | (g<<8) | (b<<16));
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
    for(int row=0;row<32;++row){
        for(int col=0;col<32;++col){
            led_mem_wr(buffer_nr, col, row, 
                            (col % 3) == 0 ? 255 : 0, 
                            (col % 3) == 1 ? 255 : 0, 
                            (col % 3) == 2 ? 255 : 0);
        }
    }
}

void led_mem_rows(int buffer_nr)
{
    for(int row=0;row<32;++row){
        for(int col=0;col<32;++col){
            if (col<10){
                led_mem_wr(buffer_nr, col, row, row*7, 0, 0);
            } 
            else if (col<20){
                led_mem_wr(buffer_nr, col, row, 0, row*7, 0);
            }
            else if (col<30){
                led_mem_wr(buffer_nr, col, row, 0, 0, row*7);
            }
            else{
                led_mem_wr(buffer_nr, col, row, 0, row*7, row*7);
            }
        }
    }
}

void led_mem_rick(int buffer_nr, int frame_nr)
{
    unsigned char *ptr = ricks_bin + frame_nr * 32 * 24;

    for(int row=0; row<32; ++row){
        for(int col=0;col<32;++col){
            if (row < 4 || row >= 28){
                led_mem_wr(buffer_nr, col, row, 0, 0, 0);
            }
            else{
                unsigned char val = *ptr;
                led_mem_wr(buffer_nr, col, row, 
                                gamma8[palette_bin[val * 3]],
                                gamma8[palette_bin[val * 3 + 1]],
                                gamma8[palette_bin[val * 3 + 2]]);
                ++ptr;
            }

        }
    }
}


/*
void matrix_fill()
{
    for(int i=0;i<NR_LEDS;++i){
        MEM_WR(LED_MEM, i, 0);
    }

    int cntr = 0;

    while(1){
        REG_WR(LED_STREAMER_CONFIG, 1);
        REG_WR(LED_STREAMER_CONFIG, 0);

#if 1
        for(int panel=0; panel<6; ++panel){
            for(int x=0; x<8; ++x){
                for(int y=0; y<8; ++y){
                    int led_nr = panel*64 + y*8 + x;

                    uint8_t r = (y<<3) << 1;
                    uint8_t g = ((x+(cntr>>4))<<4) & 0x7f;
                    uint8_t b = (panel & 3)<<3;

                    MEM_WR(LED_MEM, led_nr,   (gamma8[ g ] << 16)
                                            | (gamma8[ r ] <<  8)
                                            | (gamma8[ b ]      ) );
                }
            }
        }
#endif

#if 0
        for(int i=0;i<NR_LEDS;++i){
            MEM_WR(LED_MEM, i, ((i + (cntr>>4)) & 0x3f) | ((63-((i>>2) & 0x3f))<<8) );
        }
#endif

        while(REG_RD(LED_STREAMER_STATUS) == 1)
            ;

        cntr += 1;
    }
}
*/

void hub75_init()
{
    REG_WR_FIELD(HUB75_STREAMER_CONFIG, ENABLE, 1);
    REG_WR_FIELD(HUB75_STREAMER_CONFIG, START, 1);
    REG_WR_FIELD(HUB75_STREAMER_CONFIG, AUTO_RESTART, 1);
    REG_WR_FIELD(HUB75_STREAMER_CONFIG, BUFFER_NR, 0);
}

int hub75_get_scratch_buffer()
{
    int row;
    do{
        row = REG_RD_FIELD(HUB75_STREAMER_STATUS, CUR_ROW_NR);
    }
    while(row != 1);

    return !REG_RD_FIELD(HUB75_STREAMER_STATUS, CUR_BUFFER_NR);
}

int main() {

//    led_mem_fill(128, 64, 32);

    REG_WR(LED_DIR, 0xff);

    //matrix_fill();
    
//    while(1){
//        led_mem_effect();
//   }

//    led_mem_rows(0);
    led_mem_rick(0,0);
    hub75_init();

    int frame_cntr = 0;
    while(1){
        int scratch_buf = hub75_get_scratch_buffer();
        led_mem_rick(scratch_buf, frame_cntr / 16);
        REG_WR_FIELD(HUB75_STREAMER_CONFIG, BUFFER_NR, scratch_buf);
        frame_cntr = (frame_cntr + 1) % 256;
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
