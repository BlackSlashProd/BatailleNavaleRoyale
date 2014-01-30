#ifndef TCHAT_H
#define TCHAT_H

#include <stdio.h>
#include <stdlib.h>

typedef struct Tchat Tchat;
typedef struct Message Message;

struct Message {
	char* msg;
};

struct Tchat {
	Message* msgs;
	int nb_msg;
};

void tchat_addmessage(Tchat* t, char* msg);
void tchat_print(Tchat* t);

#endif
