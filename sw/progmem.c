#include <stdint.h>
#include <math.h>

#include "reg.h"
#include "top_defines.h"

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

int main() {

    REG_WR(LED_DIR, 0xff);

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

    while(0){
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
