#ifndef REG_H
#define REG_H

#define REG_WR(reg_name, wr_data)                   (*((volatile uint32_t *)(0x80000000 | reg_name##_ADDR)) = (wr_data))
#define REG_RD(reg_name)                            (*((volatile uint32_t *)(0x80000000 | reg_name##_ADDR)))

#define MEM_WR(mem_name, wr_addr, wr_data)          (*( (volatile uint32_t *)(0x80000000 | mem_name##_ADDR) + (wr_addr)) = (wr_data))

#endif
