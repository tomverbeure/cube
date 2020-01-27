
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

typedef enum {
    SIDE_LEFT       = 0,
    SIDE_FRONT      = 1,
    SIDE_RIGHT      = 2,
    SIDE_BACK       = 3,
    SIDE_TOP        = 4,
    SIDE_BOTTOM     = 5
} e_side_nr;

typedef enum {
    ORIENT_X_Y          = 0,
    ORIENT_X_INV_Y      = 1,
    ORIENT_INV_X_Y      = 2,
    ORIENT_INV_X_INV_Y  = 3,

    ORIENT_Y_X          = 4,
    ORIENT_Y_INV_X      = 5,
    ORIENT_INV_Y_X      = 6,
    ORIENT_INV_Y_INV_X  = 7
} e_orient;


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

uint32_t hub75s_calc_phys_addr(int buffer, int log_addr)
{
    const uint32_t side_width  = HUB75S_SIDE_WIDTH;
    const uint32_t side_height = HUB75S_SIDE_HEIGHT;
    const uint32_t side_size   = HUB75S_SIDE_SIZE;
    const uint32_t strip_width = HUB75S_STRIP_WIDTH;
    const uint32_t strip_size  = HUB75S_STRIP_SIZE;
    const uint32_t ring_size   = HUB75S_RING_SIZE;

    uint32_t cur_ring_nr     = log_addr / ring_size;
    log_addr = log_addr % ring_size;

    uint32_t cur_strip_y_nr = log_addr  / strip_size;
    uint32_t cur_strip_x_nr = (log_addr / side_width) & 3;

    uint32_t y = (log_addr / strip_width) % side_height;
    uint32_t x = log_addr % side_width;

    const uint8_t side_nr_lut[3][3][4] = {
        // Ring 0: | L | F | R | Ba
        {
            { SIDE_TOP,     SIDE_TOP,      SIDE_TOP,      SIDE_TOP      },
            { SIDE_LEFT,    SIDE_FRONT,    SIDE_RIGHT,    SIDE_BACK     },
            { SIDE_BOTTOM,  SIDE_BOTTOM,   SIDE_BOTTOM,   SIDE_BOTTOM   }
        }
    };

    const uint8_t orient_class_lut[3][3][4] = {
        // Ring 0: | L | F | R | Ba| 
        {
            // 
            { ORIENT_INV_Y_X,   ORIENT_X_Y,    ORIENT_Y_INV_X,      ORIENT_INV_X_INV_Y     },
            { ORIENT_X_Y,       ORIENT_X_Y,    ORIENT_X_Y,          ORIENT_X_Y             },
            { ORIENT_Y_INV_X,   ORIENT_X_Y,    ORIENT_INV_Y_X,      ORIENT_INV_X_INV_Y     }
        }
    };

    uint32_t cur_side_nr      = side_nr_lut     [cur_ring_nr][cur_strip_y_nr][cur_strip_x_nr];
    uint32_t cur_orient_class = orient_class_lut[cur_ring_nr][cur_strip_y_nr][cur_strip_x_nr];

    uint32_t cur_x            = (cur_orient_class == ORIENT_X_Y     || cur_orient_class == ORIENT_X_INV_Y)     ? x    :
                                (cur_orient_class == ORIENT_INV_X_Y || cur_orient_class == ORIENT_INV_X_INV_Y) ? 31-x :
                                (cur_orient_class == ORIENT_Y_X     || cur_orient_class == ORIENT_Y_INV_X)     ? y    :
                                                                                                                 31-y ;

    uint32_t cur_y            = (cur_orient_class == ORIENT_X_Y     || cur_orient_class == ORIENT_INV_X_Y)     ? y    :
                                (cur_orient_class == ORIENT_X_INV_Y || cur_orient_class == ORIENT_INV_X_INV_Y) ? 31-y :
                                (cur_orient_class == ORIENT_Y_X     || cur_orient_class == ORIENT_INV_Y_X)     ? x    :
                                                                                                                 31-x ;

    uint32_t phys_addr = cur_side_nr * side_size + (cur_y * 32) + cur_x;
    phys_addr += buffer * 6 * side_size;

    return phys_addr;
}



