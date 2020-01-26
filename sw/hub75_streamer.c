
#include <stddef.h>
#include <stdint.h>

#include "top_defines.h"
#include "reg.h"
#include "hub75_streamer.h"

typedef struct {
    int topLeftCoordX;
    int topLeftCoordY;
    int topLeftCoordZ;

    int side;
    int sideTop;
    int sideRotation;

    int xIncr;
    int yIncr;
    int zIncr;

} t_panel_info;

const int panel_rows        = 16;
const int panel_cols        = 32;
int pixels_per_panel;

t_panel_info panels[] = {
    // L2 - Left
    { 1, 1,-1,       0, 1, 270,       -1,-1, 0 },
    { 1, 0,-1,       0, 1, 90,       -1,-1, 0 },

    // L1 - Back
    { 1, 1, 1,       3, 1, 180,         0,-1,-1 },
    { 1, 0, 1,       3, 1, 0,       0,-1,-1 },

    // L0 - Bottom
    {-1, 1, 1,       5, 1, 270,      1,-1, 0 },
    { 1, 0, 1,       5, 1, 90,       1,-1, 0 },

    // R2 - Top
    { 1, 1,-1,       4, 1, 270,       -1,-1, 0 },
    { 1, 0,-1,       4, 1, 90,       -1,-1, 0 },

    // R1 - Front
    { 1, 1, 1,       1, 1, 0,         0,-1,-1 },
    { 1, 0, 1,       1, 1, 180,       0,-1,-1 },

    // R0 - Right
    {-1, 1, 1,       2, 1, 270,      1,-1, 0 },
    { 1, 0, 1,       2, 1, 90,       1,-1, 0 }

};

void hub75s_config(void)
{
    pixels_per_panel  = panel_rows * panel_cols;

    for(int i=0; i<12;++i){
        t_panel_info *pi = &panels[i];

        int memAddrStartPh0     = pi->side * 2 * pixels_per_panel;
        int memAddrStartPh1     = pi->side * 2 * pixels_per_panel;
        int memAddrColMul       = 1;
        int memAddrRowMul       = 1;

        if (pi->sideRotation == 0){
            memAddrStartPh1     += panel_rows/2 * panel_cols;

            if (!pi->sideTop){
                memAddrStartPh0 += panel_rows * panel_cols;
                memAddrStartPh1 += panel_rows * panel_cols;
            }
            memAddrColMul       = 1;
            memAddrRowMul       = panel_cols;
        }
        else if (pi->sideRotation == 90) {
            memAddrStartPh0     += panel_cols -1;
            memAddrStartPh1     += panel_cols -1 - panel_rows/2;

            if (!pi->sideTop){
                memAddrStartPh0 -= panel_cols/2;
                memAddrStartPh1 -= panel_cols/2;
            }
            memAddrColMul       = panel_cols;
            memAddrRowMul       = -1;
        }
        else if (pi->sideRotation == 180){
            memAddrStartPh0     += panel_cols -1 + (panel_rows*2 -1) * panel_cols;
            memAddrStartPh1     += panel_cols -1 + (panel_rows*3/2 -1) * panel_cols;

            if (!pi->sideTop){
                memAddrStartPh0 -= panel_rows * panel_cols;
                memAddrStartPh1 -= panel_rows * panel_cols;
            }
            memAddrColMul       = -1;
            memAddrRowMul       = -panel_cols;
        }
        else{
            memAddrStartPh0     += (panel_rows*2 -1) * panel_cols;
            memAddrStartPh1     += (panel_rows*2 -1) * panel_cols + (panel_rows/2);

            if (!pi->sideTop){
                memAddrStartPh0 += panel_cols/2;
                memAddrStartPh1 += panel_cols/2;
            }
            memAddrColMul       = -panel_cols;
            memAddrRowMul       = 1;
        }

        HUB75S_PI_REG_WR(i, MEM_ADDR_START_PH0, memAddrStartPh0);
        HUB75S_PI_REG_WR(i, MEM_ADDR_START_PH1, memAddrStartPh1);
        HUB75S_PI_REG_WR(i, MEM_ADDR_COL_MUL, memAddrColMul);
        HUB75S_PI_REG_WR(i, MEM_ADDR_ROW_MUL, memAddrRowMul);

    }
}


void hub75s_start(void)
{
    REG_WR_FIELD(HUB75S_CONFIG, ENABLE, 1);
    REG_WR_FIELD(HUB75S_CONFIG, START, 1);
    REG_WR_FIELD(HUB75S_CONFIG, AUTO_RESTART, 1);
    REG_WR_FIELD(HUB75S_CONFIG, BUFFER_NR, 0);
}

void hub75s_dim(unsigned char r_dim, unsigned char g_dim, unsigned char b_dim)
{
    REG_WR(HUB75S_RGB_DIM, (b_dim << 16) | (g_dim << 8) | r_dim);
}

int hub75s_get_scratch_buffer(void)
{
    int row;
    do{
        row = REG_RD_FIELD(HUB75S_STATUS, CUR_ROW_NR);
    }
    while(row != 1);

    return !REG_RD_FIELD(HUB75S_STATUS, CUR_BUFFER_NR);
}

