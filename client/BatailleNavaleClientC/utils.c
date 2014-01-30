#include "utils.h"

char** split(char* chaine,const char* delim,int vide){
	char** tab=NULL;
	char *ptr;
	int sizeStr;
	int sizeTab=0;
	char* largestring;
	int sizeDelim=strlen(delim);
	largestring = chaine;
	while( (ptr=strstr(largestring, delim))!=NULL ){
		sizeStr=ptr-largestring;
		if(vide==1 || sizeStr!=0){
			sizeTab++;
			tab= (char**) realloc(tab,sizeof(char*)*sizeTab);
			tab[sizeTab-1]=(char*) malloc( sizeof(char)*(sizeStr+1) );
			strncpy(tab[sizeTab-1],largestring,sizeStr);
			tab[sizeTab-1][sizeStr]='\0';
		}
		ptr=ptr+sizeDelim;
		largestring=ptr;
	}
	if(strlen(largestring)!=0){
		sizeStr=strlen(largestring);
		sizeTab++;
		tab= (char**) realloc(tab,sizeof(char*)*sizeTab);
		tab[sizeTab-1]=(char*) malloc( sizeof(char)*(sizeStr+1) );
		strncpy(tab[sizeTab-1],largestring,sizeStr);
		tab[sizeTab-1][sizeStr]='\0';
	}
	else if(vide==1){
		sizeTab++;
		tab= (char**) realloc(tab,sizeof(char*)*sizeTab);
		tab[sizeTab-1]=(char*) malloc( sizeof(char)*1 );
		tab[sizeTab-1][0]='\0';
	}
	sizeTab++;
	tab= (char**) realloc(tab,sizeof(char*)*sizeTab);
	tab[sizeTab-1]=NULL;
	return tab;
}

int getpos(char* chaine, const char car){
	unsigned int i = 0;
	while(i < strlen(chaine)){
		if(chaine[i]==car)
			return i;
		i++;
	}
	return -1;
}
