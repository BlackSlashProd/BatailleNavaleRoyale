#include "command.h"
#include "game.h"

struct Command split_command(char* cmd){
	struct Command command;
	command.args = malloc(sizeof(char**));
	command.nb_args = 0;
	char** extract = split(cmd,"/",1);
	int i = 0, nb = 0;
	char** ptr = extract;
	while(*ptr){nb++;ptr++;}
	for(; i < nb; i++){
		if(strcmp(extract[i],"\n")==0)
			break;
		if(command.nb_args++){
			command.args[command.nb_args-2] = malloc(sizeof(char*));
			command.args[command.nb_args-2] = extract[i];
		}
		else
			command.name = extract[i];
	}
	return command;
}

int command_treatment(Game* g, Command* c){
	puts(c->name);
	int num_cmd = command_num(c->name);
	switch(num_cmd){
		case 0:		// WELCOME
			command_treat_welcome(g,c);
			break;
		case 1:		// ACCESSDENIED
			puts("\nConnection refusee. Fin du client !");
			return 2;
			break;
		case 2:		// PLAYERS
			puts("Liste des clients recue !");
			command_treat_players(g,c);
			break;
		case 3:		// SHIP
			puts("\nPlacement vaisseau de taille ");
			puts(c->args[0]);
			break;
		case 4:		// OK
			puts("\nEmplacement valide !");
			command_treat_ok(g);
			break;
		case 5:		// WRONG
			puts("\nEmplacement invalide !");
			break;
		case 6:		// ALLYOURBASE
			puts("\nLa partie peut commencer !");
			command_treat_allyourbase(g);
			break;
		case 7:		// YOURTURN
			puts("\nA toi de jouer...");
			command_treat_yourturn(g,c);
			break;
		case 8:		// MISS
			puts("Tir dans l'eau !");
			command_treat_tir(g,c,0);
			break;
		case 9:		// TOUCHE
			puts("Tir reussi !");
			command_treat_tir(g,c,1);
			break;
		case 10:		// OUTCH
			puts("Nous sommes touches");
			command_treat_oucth(g,c);
			break;
		case 11:		// DEATH
			command_treat_death(g,c);
			break;
		case 12:		// AWINNERIS
			command_treat_winner(g,c,1);
			break;
		case 13:		// HEYLISTEN
			puts(c->args[0]);
			command_treat_heylisten(g,c);
			break;
		case 14:		// ACTION (commande client)
			if(command_treat_action(g,c)!=0)
				return 1;
			break;
		case 15:		// DRAWGAME
			command_treat_winner(g,c,0);
			break;
		case 16:		// PLAYAGAIN (commande client)
			command_treat_endgame(g,1);
			break;
		case 17:		// BYE (commande client)
			command_treat_endgame(g,0);
			return 2;
			break;
		case 18: 		// SPECTATOR(commande client)
			game_init_spect(g);
			break;
		case 19:		// PLAYERSHIP
			command_treat_plship(g,c);
			break;
		case 20:		// PLAYERMOVE
			command_treat_plmove(g,c);
			break;
		case 21:		// PLAYEROUCH
			command_treat_plouch(g,c);
			break;
		case 22:		// GAMESLIST
			puts("Voici la liste des parties, veuillez preciser le numero de partie comme ceci : SPECTATOR/num/");
			int i=0;
			while(i<c->nb_args-2){
				puts("NumPartie=");
				puts(c->args[i]);
				puts(" NbJoueurs=");
				puts(c->args[i+1]);
				puts("\n");
				i+=2;
			}
			break;
		default:
			break;
	};
	return 0;
}

int command_num(char* c){
	if(strcmp(c,"WELCOME")==0) return 0;
	if(strcmp(c,"ACCESSDENIED")==0) return 1;
	if(strcmp(c,"PLAYERS")==0) return 2;
	if(strcmp(c,"SHIP")==0) return 3;
	if(strcmp(c,"OK")==0) return 4;
	if(strcmp(c,"WRONG")==0) return 5;
	if(strcmp(c,"ALLYOURBASE")==0) return 6;
	if(strcmp(c,"YOURTURN")==0) return 7;
	if(strcmp(c,"MISS")==0) return 8;
	if(strcmp(c,"TOUCHE")==0) return 9;
	if(strcmp(c,"OUTCH")==0) return 10;
	if(strcmp(c,"DEATH")==0) return 11;
	if(strcmp(c,"AWINNERIS")==0) return 12;
	if(strcmp(c,"HEYLISTEN")==0) return 13;
	if(strcmp(c,"ACTION")==0) return 14;
	if(strcmp(c,"DRAWGAME")==0) return 15;
	if(strcmp(c,"PLAYAGAIN")==0) return 16;
	if(strcmp(c,"BYE")==0) return 17;
	if(strcmp(c,"SPECTATOR")==0) return 18;
	if(strcmp(c,"PLAYERSHIP")==0) return 19;
	if(strcmp(c,"PLAYERMOVE")==0) return 20;
	if(strcmp(c,"PLAYEROUCH")==0) return 21;
	if(strcmp(c,"GAMESLIST")==0) return 22;
	return-1;
}

void command_treat_welcome(Game* g, Command* c){
	Player* p = malloc(sizeof(Player));
	p->pl_name = c->args[0];
	g->play_me = p;
	g->isConnected = 1;
}

void command_treat_heylisten(Game* g, Command* c){
	tchat_addmessage(g->tchat, c->args[0]);
}

void command_treat_players(Game* g, Command* c){
	int i;
	for(i=0;i<c->nb_args-2;i++){
		if(strcmp(c->args[i],g->play_me->pl_name)!=0){
			g->play_others = realloc(g->play_others, (g->nb_players+1)*sizeof(Player*));
			Player* p = malloc(sizeof(Player));
			p->pl_name = malloc(sizeof(strlen(c->args[i])));
			strcpy(p->pl_name,c->args[i]);
			g->play_others[g->nb_players] = p;
			g->nb_players++;
		}
	}
}

void command_treat_allyourbase(Game* g){
	g->isStarted = 1;
}

void command_treat_ok(Game* g){
	int i=0;
	do{
		int x,y;
		sscanf(g->last_command->args[i],"%i",&y);
		x = (int)g->last_command->args[i+1][0]-65;
		Case* c = malloc(sizeof(Case));
		c->x = x;
		c->y = y;
		c->val = 0;
		g->sheeps = realloc(g->sheeps, g->nb_sheeps*sizeof(Case*)+1);
		g->sheeps[g->nb_sheeps++] = c;
		i+=2;
	}while(i<g->last_command->nb_args-2);
}

void command_treat_yourturn(Game* g, Command* c){
	int x,y,nb;
	sscanf(c->args[0],"%i",&y);
	x = (int)c->args[1][0]-65;
	sscanf(c->args[2],"%i",&nb);
	puts("\nNombre de coups possible :");
	puts(c->args[2]);
	g->drone->val = 1;
	g->drone->x = x;
	g->drone->y = y;
	g->nb_cps = nb;
}

int command_treat_action(Game* g, Command* c){
	int x_tmp,y_tmp, i, tir=0;
	x_tmp = g->drone->x;
	y_tmp = g->drone->y;
	for(i=0;i<c->nb_args-1;i++){
		if(i>g->nb_cps){
			puts("Trop de coups joues !");
			return -4;
		}
		if(strcmp(c->args[i],"L")==0)
			y_tmp--;
		else if(strcmp(c->args[i],"R")==0)
			y_tmp++;
		else if(strcmp(c->args[i],"U")==0)
			x_tmp++;
		else if(strcmp(c->args[i],"D")==0)
			x_tmp--;
		else if(strcmp(c->args[i],"E")==0){
			if(++tir != 1){
				puts("Un seul tir au plus par tour.");
				return -2;
			}
		}
		else if(strcmp(c->args[i],"B")==0){
			if(++g->bomb_use != 1){
				puts("Une seule bombe au plus par partie.");
				return -2;
			}
		}
		else {
			puts("Erreur : les commandes possibles sont U,D,L,R et E.");
			return -1;
		}
		if(x_tmp < 0 || x_tmp > 15 || y_tmp < 0 || y_tmp > 15){
			puts("Deplacements hors terrain impossibles.");
			return -3;
		}
	}
	puts("Actions valides.");
	return 0;
}

void command_treat_tir(Game* g, Command* c, int touche) {
	int x,y;
	sscanf(c->args[0],"%i",&y);
	x = (int)c->args[1][0]-65;
	if(touche == 1){
		g->game_board->brd_board[(x*16)+y].val = 1;
	}
	else{
		g->game_board->brd_board[(x*16)+y].val = 2;
	}
}

void command_treat_oucth(Game* g, Command* c){
	int x,y,i;
	sscanf(c->args[0],"%i",&y);
	x = (int)c->args[1][0]-65;
	for(i=0;i<g->nb_sheeps;i++){
		if(g->sheeps[i]->x == y && g->sheeps[i]->y == x){
			g->sheeps[i]->val = -1;
			break;
		}
	}
}

void command_treat_death(Game* g, Command* c){
	if(strcmp(c->args[0],g->play_me->pl_name)==0){
		g->play_me->state = -1;
		int i;
		for(i=0;i<g->nb_sheeps;i++){
			g->sheeps[i]->val = -1;
		}
		puts("Vous etes mort, veuillez attendre la fin de partie !");
	}
	else {
		int i;
		for(i=0;i<g->nb_players;i++){
			if(strcmp(c->args[0],g->play_others[i]->pl_name)==0){
				g->play_others[i]->state = -1;
			}
		}
	}
}

void command_treat_winner(Game* g, Command* c, int awinner){
	if(awinner){
		if(strcmp(c->args[0],g->play_me->pl_name)==0){
			puts("Bravo, vous avez gagne !!!");
		}
		else {
			puts("Vous avez perdu matelot. Victoire de ");
			puts(c->args[0]);
		}
	}
	else
		puts("Aucun gagnant. Egalite parfaite !");
	g->isStarted = 2;
	puts("\nPour rejouer : PLAYAGAIN. Pour quitter : BYE.\n");
}

void command_treat_endgame(Game* g,int again){
	if(again!=0){
		g->bomb_use = 0;
		g->isStarted = 0;
		g->nb_cps = 0;
		g->drone->val = 0;
		int i;
		for(i=g->nb_sheeps-1;i>=0;i--){
			free(g->sheeps[i]);
		}
		free(g->sheeps);
		g->sheeps = malloc(sizeof(Case*));
		g->nb_sheeps = 0;
		for(i=g->nb_players-1;i>=0;i--){
			free(g->play_others[i]);
		}
		free(g->play_others);
		g->nb_players = 0;
		for(i=g->game_board->nb_case-1;i>=0;i--){
			g->game_board->brd_board[i].val = 0;
		}
	}
}
void command_treat_plship(Game* g,Command* c){
	int i=1,j;
	for(j=0;j<g->nb_players;j++){
		if(strcmp(g->play_others[j]->pl_name,c->args[0])==0)
			break;
	}
	j++;
	do{
		int x,y;
		sscanf(c->args[i],"%i",&y);
		x = (int)c->args[i+1][0]-65;
		g->game_board->brd_board[(x*16)+y].val = j;
		i+=2;
	}while(i<c->nb_args-2);
}

void command_treat_plmove(Game* g,Command* c){
	int x,y,j,i;
	sscanf(c->args[1],"%i",&y);
	x = (int)c->args[2][0]-65;
	for(j=0;j<g->nb_players;j++){
		if(strcmp(g->play_others[j]->pl_name,c->args[0])==0)
			break;
	}
	j++;
	for(i=0;i<g->nb_sheeps;i++){
		if(g->sheeps[i]->val == j){
			g->sheeps[i]->x = x;
			g->sheeps[i]->y = y;
			break;
		}
	}
	if(i == g->nb_sheeps){
		Case* cs = malloc(sizeof(Case));
		cs->x = x;
		cs->y = y;
		cs->val = j;
		g->sheeps = realloc(g->sheeps, g->nb_sheeps*sizeof(Case*)+1);
		g->sheeps[g->nb_sheeps] = cs;
		g->nb_sheeps++;
	}
}

void command_treat_plouch(Game* g,Command* c){
	puts("Joueur touche :");
	puts(c->args[0]);
	puts(c->args[1]);
	puts(c->args[2]);
	int x,y;
	sscanf(c->args[1],"%i",&y);
	x = (int)c->args[2][0]-65;
	g->game_board->brd_board[(x*16)+y].val = -1;
}
