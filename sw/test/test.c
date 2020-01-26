
#include <stdint.h>
#include <stdio.h>

#include "hub75_streamer.h"

void main()
{
    int pos_x = 0;
    int pos_y = -5;

    uint32_t log_addr = (pos_y + HUB75S_SIDE_HEIGHT) * HUB75S_STRIP_WIDTH + pos_x;
    uint32_t phys_addr = hub75s_calc_phys_addr(0, log_addr);

    printf("log_addr: %d -> phy_addr: %d\n", log_addr, phys_addr);

}
