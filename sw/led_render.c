
#include <stddef.h>
#include <stdint.h>

#include "led_render.h"
#include "top_defines.h"
#include "reg.h"

/*
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
*/



