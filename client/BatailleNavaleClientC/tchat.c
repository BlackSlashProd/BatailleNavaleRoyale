#include "tchat.h"

void tchat_addmessage(Tchat* t, char* msg){
	if(t->nb_msg != 0)
		t->msgs = realloc(t->msgs,(t->nb_msg+1)*sizeof(Message));
	else
		t->msgs = malloc(sizeof(Message));
	t->msgs[t->nb_msg].msg = msg;
	t->nb_msg++;
}

void tchat_print(Tchat* t){
	int i;
	for(i=0;i<t->nb_msg;i++){
		puts(t->msgs[i].msg);
	}
}
