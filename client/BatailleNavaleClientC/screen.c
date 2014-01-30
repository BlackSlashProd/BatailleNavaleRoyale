#include "screen.h"
#include "game.h"

void screen_init(Screen* sc, Game* g){
	SDL_FillRect(sc->ecran, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 0));
	SDL_Rect position;
	// Board
	sc->board = SDL_CreateRGBSurface(SDL_HWSURFACE, 425, 425, 32, 0, 0, 0, 0);
	position.x = 0;
	position.y = 0;
	SDL_FillRect(sc->board, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 0));
	SDL_BlitSurface(sc->board, NULL, sc->ecran, &position);
	int i;
	SDL_Surface* case_board;
	// Info
	sc->info = SDL_CreateRGBSurface(SDL_HWSURFACE, 375, 125, 32, 0, 0, 0, 0);
	position.x = 425;
	position.y = 300;
	SDL_FillRect(sc->info, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 0));
	SDL_BlitSurface(sc->info, NULL, sc->ecran, &position);
	// Command
	sc->command = SDL_CreateRGBSurface(SDL_HWSURFACE, 800, 175, 32, 0, 0, 0, 0);
	position.x = 0;
	position.y = 425;
	SDL_FillRect(sc->command, NULL, SDL_MapRGB(sc->ecran->format, 0, 255, 0));
	SDL_BlitSurface(sc->command, NULL, sc->ecran, &position);
	// Tchat
	sc->tchat = SDL_CreateRGBSurface(SDL_HWSURFACE, 375, 300, 32, 0, 0, 0, 0);
	position.x = 425;
	position.y = 0;
	SDL_FillRect(sc->tchat, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 255));
	SDL_BlitSurface(sc->tchat, NULL, sc->ecran, &position);
	if(g->player_game==1){
		for(i=0;i<g->game_board->nb_case;i++){
			if(i%16==0) {
				case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
				position.x = 2;
				position.y = 425-(25*((i/16)+1))+2;
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 255));
				SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
			}
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
			position.x = 25+(g->game_board->brd_board[i].y*25)+2;
			position.y = 425-((g->game_board->brd_board[i].x+1)*25)+2;
			if(g->game_board->brd_board[i].val == 1){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 255, 0));
			} else if(g->game_board->brd_board[i].val == 2){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 0));
			}
			else
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 255, 255));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
		for(i=0;i<g->game_board->size;i++){
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
			position.x = 25+25*(i%16)+2;
			position.y = 2;
			SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 255));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
		for(i=0;i<g->nb_sheeps;i++){
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 17, 17, 32, 0, 0, 0, 0);
			position.x = 25+(g->sheeps[i]->y*25)+4;
			position.y = 425-((g->sheeps[i]->x+1)*25)+4;
			if(g->sheeps[i]->val == 0){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 30, 30, 30));
			}
			else
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 100, 100));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
		if(g->drone->val != 0){
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 11, 11, 32, 0, 0, 0, 0);
			position.x = 25+(g->drone->y*25)+7;
			position.y = 425-((g->drone->x+1)*25)+7;
			SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 255));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
	}
	else {
		for(i=0;i<g->game_board->nb_case;i++){
			if(i%16==0) {
				case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
				position.x = 2;
				position.y = 425-(25*((i/16)+1))+2;
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 255));
				SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
			}
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
			position.x = 25+(g->game_board->brd_board[i].y*25)+2;
			position.y = 425-((g->game_board->brd_board[i].x+1)*25)+2;
			if(g->game_board->brd_board[i].val == 1){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 255, 0));
			} else if(g->game_board->brd_board[i].val == 2){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 255));
			} else if(g->game_board->brd_board[i].val == 3){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 255, 255));
			} else if(g->game_board->brd_board[i].val == 4){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 255, 0));
			} else if(g->game_board->brd_board[i].val == -1){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 0));
			}
			else
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 255, 255));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
		for(i=0;i<g->game_board->size;i++){
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 21, 21, 32, 0, 0, 0, 0);
			position.x = 25+25*(i%16)+2;
			position.y = 2;
			SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 255, 0, 255));
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
		for(i=0;i<g->nb_sheeps;i++){
			case_board = SDL_CreateRGBSurface(SDL_HWSURFACE, 11, 11, 32, 0, 0, 0, 0);
			position.x = 25+(g->sheeps[i]->y*25)+7;
			position.y = 425-((g->sheeps[i]->x+1)*25)+7;
			if(g->sheeps[i]->val == 1){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 150, 0));
			} else if(g->sheeps[i]->val == 2){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 0, 150));
			} else if(g->sheeps[i]->val == 3){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 0, 150, 150));
			} else if(g->sheeps[i]->val == 4){
				SDL_FillRect(case_board, NULL, SDL_MapRGB(sc->ecran->format, 150, 150, 0));
			}
			SDL_BlitSurface(case_board, NULL, sc->ecran, &position);
		}
	}
	SDL_Flip(sc->ecran);
}

int gereTouche(SDL_KeyboardEvent* keyevent, char* chaine, int pos)
{
     switch(keyevent->keysym.sym)
     {
          case SDLK_RETURN:
        	  	  if(pos > 0 && chaine[pos-1] == '/'){
        	  		  puts(chaine);
        	  		  chaine[pos] = '\n';
        	  		  return -1;
        	  	  }
        	  	  else {
        	  		  puts("Erreur : les commandes doivent se finir par /.");
        	  		  return -2;
        	  	  }
          case SDLK_BACKSPACE:
                  if (pos > 0) {
                      chaine[--pos] = 0;
                  }
                  return pos;
          case SDLK_TAB:
                  {
                     int posmax;
                     for (posmax=pos+4; pos < posmax; pos++) {
                          chaine[pos] = ' ';
                     }
                     chaine[pos] = 0;
                     return pos;
                  }
          default:
              break;
     }
     if (keyevent->keysym.unicode != 0 && pos<99)
     {
         chaine[pos] = keyevent->keysym.unicode;
         pos++;
     }
     return pos;
}
