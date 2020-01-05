
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

int main(int argc, char *argv[])
{
    FILE *input_file = fopen(argv[1], "rb");
    assert(input_file != NULL);

    fseek(input_file, 0, SEEK_END);
    long len = ftell(input_file);
    fseek(input_file, 0, SEEK_SET);

    printf("%s: %ld bytes\n", argv[1], len);

    assert(len);

    unsigned char *input_buf = malloc(len);
    unsigned char *output_buf = malloc(len/3);

    assert(input_buf);
    assert(output_buf);

    int result;
    result = fread(input_buf, 1, len, input_file);
    printf("result: %d\n", result);
    assert(result);

    for(int i=0;i<len/3;++i){
        unsigned char r = *input_buf++;
        unsigned char g = *input_buf++;
        unsigned char b = *input_buf++;

        output_buf[i] = (r>>6) | ((g>>6)<<2) | ((b>>6)<<4);
    }

    FILE *output_file = fopen(argv[2], "wb+");
    result = fwrite(output_buf, len/3, 1, output_file);
    assert(result);
    
    return(0);
}
