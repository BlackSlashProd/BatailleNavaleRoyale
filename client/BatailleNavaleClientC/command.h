#ifndef COMMAND_H_INCLUDED
#define COMMAND_H_INCLUDED

typedef struct Game Game;

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "utils.h"
#include "player.h"

typedef struct Command {
	char* name;
	char** args;
	int nb_args;
}Command;

struct Command split_command(char* cmd);
int command_treatment(Game* g, Command* c);
int command_num(char* c);
void command_treat_welcome(Game* g, Command* c);
void command_treat_heylisten(Game* g, Command* c);
void command_treat_players(Game* g, Command* c);
void command_treat_allyourbase(Game* g);
void command_treat_ok(Game* g);
void command_treat_yourturn(Game* g, Command* c);
int command_treat_action(Game* g, Command* c);
void command_treat_tir(Game* g, Command* c, int touche);
void command_treat_oucth(Game* g, Command* c);
void command_treat_death(Game* g, Command* c);
void command_treat_winner(Game* g, Command* c, int awinner);
void command_treat_endgame(Game* g,int again);
void command_treat_plship(Game* g,Command* c);
void command_treat_plmove(Game* g,Command* c);
void command_treat_plouch(Game* g,Command* c);

#endif
