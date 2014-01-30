#ifndef GAME_H_INCLUDED
#define GAME_H_INCLUDED

#include <stdio.h>
#include <stdlib.h>
#include "board.h"
#include "tchat.h"
#include "command.h"
#include "screen.h"

typedef struct Game Game;
typedef struct Player** Players;
typedef struct Case** Cases;

struct Game {
	Command* last_command;
	Player* play_me;
	Players play_others;
	Board* game_board;
	Tchat* tchat;
	Cases sheeps;
	Case* drone;
	int isConnected;
	int isStarted;
	int nb_players;
	int nb_sheeps;
	int nb_cps;
	int player_game;
	int bomb_use;
};

void game_init(Game* g);
void game_init_spect(Game* game);
void game_print(Game* g);

#endif
