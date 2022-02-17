#include "server.h"

pthread_attr_t attr;
struct sockaddr_in server_addr;
struct sockaddr_in client_addr;

int main(int argc, char *argv[]) {
    
    //Dichiarazione variabili locali
    int server_sd, client_sd, check_value;
    socklen_t client_lngth = sizeof(client_addr);
    pthread_t tid;
    int* thread_sd;
    char client_ip[MAXBUFF];

    //Gestione dei segnali SIGINT e SIGTERM
    signal(SIGINT, handleSignal);
    signal(SIGTERM, handleSignal);
    
    //Configurazione dell'indirizzo del server e della sua porta
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    server_addr.sin_port = htons(MYPORT);        

    //Creazione socket
    if((server_sd = socket(AF_INET,SOCK_STREAM,0)) <= 0)
        perror("[***] Error! Can't create socket. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Socket created.\n", getLogTime());

    //Binding dell'indirizzo alla socket
    if (bind(server_sd, (struct sockaddr *)&server_addr, sizeof(server_addr)) == -1)
        perror("[***] Error! Can't bind. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Connected to the port.\n", getLogTime());

    //Server in ascolto
    if(listen(server_sd, 10) == -1)
        perror("[***] Error! Can't listen. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Listen to incoming connections.\n", getLogTime());
    
    //Inizializzazione attributo per i thread
    if((check_value = pthread_attr_init(&attr)) != 0)
        fprintf(stderr, "[***] [%s] Error! Can't initialize thread attribute. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);

    //Configurazione dell'attributo per i thread (DETACHED STATE)
    if ((check_value = pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED)) != 0) 
        fprintf(stderr,"[***] [%s] Error! Can't set detached state. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);
        
    while (1)
    {
        //Accettazioni dei client in ingresso e assegnazione del socket descriptor
        if((client_sd = accept(server_sd, (struct sockaddr *)&client_addr, &client_lngth)) == -1)
            perror("[***] Error! Can't accept connection. For more information"), exit(EXIT_FAILURE);
        
        inet_ntop(AF_INET, &client_addr.sin_addr, client_ip, INET_ADDRSTRLEN);  //Memorizzazione dell'indirizzo del client accettato in formato 0.0.0.0
        printf("[#] [%s] Accepted connection from the server to client %s with socket descriptor %d.\n", getLogTime(), client_ip, client_sd);
        
        //Inizializzazione del socket descriptor che verrà gestito dal thread
        thread_sd = (int *) malloc(sizeof(int));
        *thread_sd = client_sd;
        
        //Creazione dei thread per gestire ogni client connesso
        if((check_value = pthread_create(&tid, &attr, handleConnection, (void *)thread_sd)) != 0)
            fprintf(stderr,"[***] [%s] Error! Can't create thread. For more information: %s", getLogTime(), strerror(check_value)), exit(EXIT_FAILURE);   
    }

    //Deallocazione memoria
    free((void *)thread_sd);

    return 0;
}
