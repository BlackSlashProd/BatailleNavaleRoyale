#ifndef SCREEN_H
#define SCREEN_H

#include <SDL/SDL.h>

typedef struct Game Game;

typedef struct Screen Screen;

struct Screen{
	SDL_Surface* ecran;
	SDL_Surface* board;
	SDL_Surface* tchat;
	SDL_Surface* command;
	SDL_Surface* info;
    int curpos;
    char chaine[100];
};

void screen_init(Screen* sc, Game* g);
int gereTouche(SDL_KeyboardEvent* keyevent, char* chaine, int pos);

#endif
