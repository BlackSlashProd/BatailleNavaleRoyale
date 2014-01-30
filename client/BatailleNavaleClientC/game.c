#include "game.h"

void game_init(Game* game){
    Tchat* tchat = malloc(sizeof(Tchat));
    Command* command = malloc(sizeof(Command));
    Board* board = board_build(16);
    game->sheeps = malloc(sizeof(Case*));
    game->drone = malloc(sizeof(Case));
    game->drone->val = 0;
    game->game_board = board;
    game->tchat = tchat;
    game->last_command = command;
    game->player_game = 1;
    game->nb_sheeps = 0;
    game->bomb_use = 0;
}

void game_init_spect(Game* game){
    Tchat* tchat = malloc(sizeof(Tchat));
    tchat->nb_msg = 0;
    Board* board = board_build(16);
    game->game_board = board;
    game->tchat = tchat;
    game->player_game = 0;
}

void game_print(Game* g){
	if(g->isConnected != 0){
		puts("\n------------------------------\nPlayerName=");
		puts(g->play_me->pl_name);
		puts("\nLstPlayers=");
		int i;
		for(i=0;i<g->nb_players;i++){
			puts(g->play_others[i]->pl_name);
			if(g->play_others[i]->state == 0)
				puts("(Vivant)");
			else
				puts("(Mort)");
		}
		puts("\n------------------------------\nTchat :\n");
		tchat_print(g->tchat);
		puts("\n------------------------------\n");
	}
}
