#ifndef HUB75_STREAMER_H
#define HUB75_STREAMER_H

void hub75s_config(void);
void hub75s_start(void);
int hub75s_get_scratch_buffer(void);
void hub75s_dim(unsigned char r_dim, unsigned char g_dim, unsigned char b_dim);

#endif
