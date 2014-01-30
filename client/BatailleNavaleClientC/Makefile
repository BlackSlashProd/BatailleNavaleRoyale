CC=gcc
CFLAGS=-W -Wall
LDFLAGS=-lpthread -lSDL
EXEC=client

all: $(EXEC)

client: board.o command.o game.o screen.o screen.o tchat.o utils.o main.o
	$(CC) -o $@ $^ $(LDFLAGS)

%.o: %.c
	$(CC) -o $@ -c $< $(CFLAGS)

clean:
	rm -rf *.o
	rm -rf $(EXEC)
