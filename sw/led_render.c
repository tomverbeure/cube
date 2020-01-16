
#include <stddef.h>
#include <stdint.h>

#include "led_render.h"
#include "top_defines.h"
#include "reg.h"

typedef struct {
    int topLeftCoordX;
    int topLeftCoordY;
    int topLeftCoordZ;

    int topLeftMemAddr;

    int xIncr;
    int yIncr;
    int zIncr;

} t_panel_info;

const int panel_rows    = 16;
const int panel_cols    = 32;

t_panel_info panels[] = {
    {-1, 1, 1,       0,       1,-1, 0 },
    {-1, 0, 1,       1,       1,-1, 0 },
    { 1, 1, 1,       2,       0,-1,-1 },
    { 1, 0, 1,       3,       0,-1,-1 },
    { 1, 1,-1,       4,      -1,-1, 0 },
    { 1, 0,-1,       5,      -1,-1, 0 },
    {-1, 1,-1,       6,       0,-1, 1 },
    {-1, 0,-1,       7,       0,-1, 1 },
    {-1, 1,-1,       8,       1, 0, 1 },
    {-1, 1, 0,       9,       1, 0, 1 },
    {-1,-1, 1,      10,       1, 0,-1 },
    {-1,-1, 0,      11,       1, 0,-1 },
    {-1,-1, 0,      -1,       1, 0,-1 },
};

void led_render_clear_leds(void)
{
    t_panel_info *pi = panels;
    volatile uint32_t *led_mem = (volatile uint32_t *)(0x80000000 | LED_MEM_ADDR);

    while(pi->topLeftMemAddr >= 0){
        volatile uint32_t *l = &led_mem[pi->topLeftMemAddr * panel_rows * panel_cols];
        for(int i=0;i<panel_rows * panel_cols; ++i){
            *l = 0;
            ++l;
        }

        ++pi;
    }
}



