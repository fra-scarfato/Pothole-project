#include "server.h"

/*
FUNZIONE handleConnection:
- Gestisce la richiesta usando appropriate funzioni
*/
void *handleConnection(void *arg)
{
    //Dichiarazione variabili locali
    char buf[MAXBUFF];
    int check_value, flag, client_sd = *((int *)arg);
    struct timespec ttime;
    
    
    printf("[#] [%s] Thread %d handle client connection %d.\n", getLogTime(), (int)pthread_self(), client_sd);    
    
    //Thread aspetta per ricevere la richiesta del client
    ttime.tv_sec = 1;
    nanosleep(&ttime,NULL);

    //Lettura sulla socket e ricezione della richiesta da parte del client
    if(read(client_sd,buf,MAXBUFF) == -1)
        fprintf(stderr,"[***] [%s] Error! Can't read file. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);

    //Lettura della flag inviata dal client
    handleRequest(client_sd, &flag, buf);      //riga 24 "utils.c"
    
    
    switch (flag)
    {
        case 0:
            sendLimit(client_sd); // riga 55 "handling.c"
            break;

        case 1:
            receiveHole(client_sd, buf); // riga 75 "handling.c"
            break;

        case 2:
            sendHoles(client_sd, buf);
            break;

        case 3:
            registerUsername(client_sd, buf);
            break;  
    }
    if ((close(client_sd)) == -1)
        fprintf(stderr,"[***] [%s] Error! Can't close socket descriptor. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
    
    free(arg);
    pthread_exit(NULL);
    
}

/*
FUNZIONE sendLimit:
- Manda i valori soglia ai client
*/
void sendLimit(int client_sd)
{
    //Dichiarazioni variabili locali
    char buffer[10];
    int check_value;
    
    //Memorizzo il valore soglia nel buffer
    sprintf(buffer, "%f", LIMIT);
    
    //Manda il valore soglia al client
    if(write(client_sd, buffer, 4) == -1)
       fprintf(stderr,"[***] [%s] Error! Can't write on socket. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Sent limit value to client %d.\n", getLogTime(), client_sd);
}

/*
FUNZIONE receiveHole:
- Riceve la posizione della buca
- Riceve la variazione rispetto al valore soglia
*/
void receiveHole(int client_sd, char buf[])
{
    //Dichiarazione variabili locali
    clientData *hole = NULL;
    
    //Estrazione degli effettivi dati 
    extractHole(&hole, buf);       // riga 80 "utils.c"

    printf("[#] [%s] These are client %d coordinates: Latitude: %f; Longitude: %f. Variations in relation to limit value: %f.\n", getLogTime(), client_sd, hole->latitude, hole->longitude, hole->variation);
    
    //Impachettare un singolo elemento data contenente i dati estratti
    saveHole(hole);                // riga 119 "utils.c"
    
    free((void *)hole);
}

/*
FUNZIONE sendHoles:
- Riceve la posizione attuale del client e la estrae
- Recupera le buche nelle vicinanze
- Manda il risultato al client
*/
void sendHoles(int client_sd, char buf[])
{
    //Dichiarazione variabili locali
    clientData *position = NULL;
    clientData *holes = NULL;
    char json_string[50000] = "{\"potholes\":[";
    int check_value;

    //Estrazione della posizione attuale del client
    extractPosition(&position, buf);

    //Riprendo tutte le buche registrate dal database
    getNearbyHoles(&holes,position);

    //Creo stringa JSON da mandare al client
    createJSON(&holes, json_string);

    if(write(client_sd, json_string, strlen(json_string)) == -1)
        fprintf(stderr,"[***] [%s] Error! Can't write on socket. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Sent nearby holes to client %d.\n", getLogTime(), client_sd);
}

/*
FUNZIONE registerUsername:
- Riceve lo username e lo estrae
- Registra lo username nel database 
- Controlla i duplicati
*/
void registerUsername(int client_sd, char buf[])
{
    //Dichiarazione variabili globali
    int check;  //in cui controllo se lo username è stato salvato o no sul database
    int check_value;    //valore per errori del thread

    //Salvo nel valore check se ho un errore o l'operazione è andata a buon fine
    check = saveUser(buf);

    if(check == 0)
    {
        if(write(client_sd, "0", 2) == -1)
            fprintf(stderr,"[***] [%s] Error! Can't write on socket. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
    }
    else
    {
        if(write(client_sd, "-1", 2) == -1)
            fprintf(stderr,"[***] [%s] Error! Can't write on socket. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
    }
    printf("[#] [%s] Sent check value to client %d.\n", getLogTime(), client_sd);
}
