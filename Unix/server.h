#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/stat.h> 
#include <fcntl.h> 
#include <sys/wait.h>
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <mysql/mysql.h>
#include <string.h>
#include <sys/un.h>
#include <time.h>
#include <arpa/inet.h>
#include <math.h>

#define MYPORT 10000
#define MAXBUFF 200
#define LIMIT 2.0
#define GEO_RADIUS 2.5

struct clientData
{
    char username[MAXBUFF];
    double longitude;
    double latitude;
    float variation;
    struct clientData *next;
};
typedef struct clientData clientData;

//FUNZIONI PRINCIPALI "handling.c"
void handleSignal(int);
void *handleConnection(void *arg);
void sendLimit(int);
void receiveHole(int, char []);
void sendHoles(int, char []);

//FUNZIONI AUSILIARIE GENERICHE "utils.c"
//-Funzione di gestione generale delle richieste
void handleRequest(int, int *, char []);    
//-Funzioni ausiliarie per ricezione buche
void extractHole(clientData **, char []);
void saveHole(clientData *);
//-Funzioni ausiliarie per mandare buche nelle vicinanze
void extractPosition(clientData **, char []);
void getNearbyHoles(clientData **, clientData *);
double calculateDistance(double, double, double, double); 
void createJSON(clientData **, char []);


//FUNZIONI AUSILIARIE PER STRUTTURA DATI "list.c"
clientData* createNode(char [], char [], char [], char []);
clientData* insert(clientData*, char [], char [], char [], char []);

//VARIABILI GLOBALI
extern pthread_attr_t attr;
extern struct sockaddr_in server_addr;
extern struct sockaddr_in client_addr;