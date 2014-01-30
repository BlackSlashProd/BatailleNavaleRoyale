#include "board.h"

Board* board_build(int size){
	Board* b = malloc(sizeof(Board));
	b->size = size;
	int i,j;
	for(i=0;i<size;i++){
		for(j=0;j<size;j++){
			if(b->nb_case == 0)
				b->brd_board = malloc(sizeof(Case));
			else
				b->brd_board = realloc(b->brd_board, (b->nb_case+1)*sizeof(Case));
			b->brd_board[b->nb_case].x = i;
			b->brd_board[b->nb_case].y = j;
			b->nb_case++;
		}
	}
	return b;
}

void board_print(Board* b){
	int i;
	char c;
	for(i=0;i<b->nb_case;i++){
		if(i%b->size==0){
			putchar('\n');
			c = 64+b->size-(i/b->size);
			putchar(c);
			putchar('|');
		}
		putchar((char)(b->brd_board[i].val+48));
		putchar('|');
	}
}

int board_getIndice(int x, int y){
	return (((15-x)*16)+y);
}
