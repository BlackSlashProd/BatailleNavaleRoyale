#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include <string.h>
#include <SDL/SDL.h>
#include <SDL/SDL_ttf.h>

#include "game.h"

typedef struct Params Params;

struct Params {
	Game* p_game;
	Screen* p_screen;
	int sock;
};

static pthread_mutex_t mutex_stock = PTHREAD_MUTEX_INITIALIZER;
pthread_t pth_recv, pth_display;

void* cl_display(void* args) {
	printf("Pret a afficher...\n");
	Params* p = (Params*)args;
	int sockfd = p->sock;
	screen_init(p->p_screen,p->p_game);
	int continuer = 1;
	p->p_screen->curpos = 0;
    SDL_EnableUNICODE(1);
    while (continuer)
    {
    	SDL_Event event;
    	SDL_PollEvent(&event);
        SDL_WaitEvent(&event);
        switch(event.type)
        {
            case SDL_QUIT:
                continuer = 0;
                break;
            case SDL_KEYDOWN:
            	 p->p_screen->curpos = gereTouche(&(event.key), p->p_screen->chaine, p->p_screen->curpos);
                 break;
        }
        if(p->p_screen->curpos==-1){
			struct Command c = split_command(p->p_screen->chaine);
			int num_cmd = command_num(c.name);
			pthread_mutex_lock(&mutex_stock);
			if(num_cmd==17 && command_treatment(p->p_game,&c)==2){
				continuer = 0;
				break;
			}
			if(num_cmd==16 || num_cmd==18 || num_cmd==22)
				command_treatment(p->p_game,&c);
			if(num_cmd != 14 || num_cmd==-1 || (num_cmd==14 && command_treatment(p->p_game,&c)==0)){
				p->p_game->last_command = &c;
				send(sockfd,p->p_screen->chaine,strlen(p->p_screen->chaine),0);
			}
			pthread_mutex_unlock(&mutex_stock);
        }
        if(p->p_screen->curpos==-1 || p->p_screen->curpos==-2){
			p->p_screen->curpos = 0;
			int j = 0;
			while(j < 100){
				p->p_screen->chaine[j++] = '\0';
			}
        }
        pthread_mutex_lock(&mutex_stock);
        screen_init(p->p_screen,p->p_game);
        pthread_mutex_unlock(&mutex_stock);
    }
    SDL_EnableUNICODE(0);
    pthread_cancel(pth_recv);
	free(p->p_screen);
	free(p->p_game);
	SDL_Quit();
	pthread_cancel(pth_display);
	exit(0);
	return NULL;
}

void* cl_recv(void* args){
	printf("Pret a recevoir...\n");
	char* ch = (char*)malloc(100);
	char* buff = (char*)malloc(1024);
	int i,r;
	Params* p = (Params*)args;
	int sockfd = p->sock;
	while(1){
		r = recv(sockfd,ch,100,0);
		if(r < 0){return NULL;}
		i = 0;
		while(i<r && ch[i] != '\0'){
			if(ch[i] != '\n'){
				sprintf(buff,"%s%c",buff,ch[i]);
			}
			else{
				struct Command c = split_command(buff);
				puts("\nReception : ");
				pthread_mutex_lock(&mutex_stock);
				command_treatment(p->p_game,&c);
				pthread_mutex_unlock(&mutex_stock);
				int j = 0;
				while(j < 1024){
					buff[j++] = '\0';
				}
				game_print(p->p_game);
			}
			i++;
		}
	}
    pthread_cancel(pth_display);
    pthread_cancel(pth_recv);
	return NULL;
}


int main(int argc, char *argv[])
{
	if(argc != 3) {
		fprintf(stderr, "Please use client like : client <srv_address> <srv_port>\n");
		return -1;
	}
    int sockfd, len, result, port;
	if (sscanf(argv[2],"%i",&port)!=1){
		perror("Port invalide.\n");
		return -1;
	}
    struct sockaddr_in address;
    sockfd = socket(PF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
        perror("Creation de socket impossible.\n") ;
        return -1 ;
    }
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = inet_addr(argv[1]);
    address.sin_port = htons(port);
    len = sizeof(address);
    result = connect(sockfd, (struct sockaddr *)&address, len);
    if(result == -1) {
        perror("Impossible de se connecter ");
        return -1;
    }
    Game* game = malloc(sizeof(Game));
    Params* params = malloc(sizeof(Params));
    params->p_screen = malloc(sizeof(Screen));
    params->sock = sockfd;
    game_init(game);
    params->p_game = game;
    if (SDL_Init(SDL_INIT_VIDEO) == -1) {
        fprintf(stderr, "Erreur d'initialisation de la SDL : %s\n", SDL_GetError());
        exit(EXIT_FAILURE);
    }
    params->p_screen->ecran = SDL_SetVideoMode(800, 600, 32, SDL_HWSURFACE);
	SDL_WM_SetCaption("Bataille Navale Royale", NULL);
    if(pthread_create(&pth_recv,NULL,cl_recv,params)){
    	printf("Erreur lors de la creation du thread de lecture.\n");
    	return -1;
    }
    if(pthread_create(&pth_display,NULL,cl_display,params)){
    	printf("Erreur lors de la creation du thread d'affichage.\n");
    	return -1;
    }
    if(pthread_join(pth_recv, NULL) && pthread_join(pth_display, NULL)){
    	printf("Joining error");
    	return -2;
    }
    free(params->p_game);
    free(params);
    close(sockfd);
    exit(0);
}
