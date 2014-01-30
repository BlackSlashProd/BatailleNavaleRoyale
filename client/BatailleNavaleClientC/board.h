#ifndef BOARD_H
#define BOARD_H

#include <stdio.h>
#include <stdlib.h>

typedef struct Board Board;
typedef struct Case Case;

struct Case {
	int x;
	int y;
	int val;
};


struct Board {
	Case* brd_board;
	int size;
	int nb_case;
};

Board* board_build(int size);
void board_print(Board* b);
int board_getIndice(int x, int y);

#endif
