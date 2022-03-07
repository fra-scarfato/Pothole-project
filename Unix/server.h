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

#define MYPORT 80
#define MAXBUFF 200
#define LIMIT 10.0
#define RADIUS 30
#define DB_USER "test"
#define DB_PWD "lso1234"
#define DB_NAME "pothole"
#define DB_HOST "localhost"
#define DB_TABLE "Hole"

struct clientData
{
    double longitude;
    double latitude;
    float variation;
    struct clientData *next;
};
typedef struct clientData clientData;

//FUNZIONI PRINCIPALI "handling.c"
void handleSignal(int);
void createDatabase(int);
void *handleConnection(void *arg);
void sendLimit(int);
void receiveHole(int, char []);
void sendHoles(int, char []);
void registerUsername(int, char []);

//FUNZIONI AUSILIARIE GENERICHE "utils.c"
char* getLogTime();
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
//- Funzione ausiliaria per salvare username nel database
int saveUser(char []);


//FUNZIONI AUSILIARIE PER STRUTTURA DATI "list.c"
clientData* createNode(char [], char [], char []);
clientData* insert(clientData*, char [], char [], char []);

//VARIABILI GLOBALI
extern pthread_attr_t attr;
extern struct sockaddr_in server_addr;
extern struct sockaddr_in client_addr;
