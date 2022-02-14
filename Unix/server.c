#include "server.h"

int main(int argc, char *argv[]) {
    
    //Dichiarazione variabili locali
    int server_sd, client_sd, check_value;
    socklen_t client_lngth = sizeof(client_addr);
    pthread_t tid;
    int* thread_sd;

    //Gestione dei segnali SIGINT e SIGTERM
    signal(SIGINT, fix_stop);
    signal(SIGTERM, fix_stop);
    
    //Configurazione dell'indirizzo del server e della sua porta
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    server_addr.sin_port = htons(MYPORT);        

    //Creazione socket
    if((server_sd = socket(AF_INET,SOCK_STREAM,0)) <= 0)
        perror("[***] Error! Can't create socket. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] Socket created.\n");

    //Binding dell'indirizzo alla socket
    if (bind(server_sd, (struct sockaddr *)&server_addr, sizeof(server_addr)) == -1)
        perror("[***] Error! Can't bind. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] Connected to the port.\n");

    //Server in ascolto
    if(listen(server_sd, 10) == -1)
        perror("[***] Error! Can't listen. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] Listen to incoming connections.\n");
    
    //Inizializzazione attributo per i thread
    if((check_value = pthread_attr_init(&attr)) != 0)
        fprintf(stderr, "[***] Error! Can't initialize thread attribute. For more information: %s",strerror(check_value)), exit(EXIT_FAILURE);

    //Configurazione dell'attributo per i thread (DETACHED STATE)
    if ((check_value = pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED)) != 0) 
        fprintf(stderr,"[***] Error! Can't set detached state. For more information: %s", strerror(check_value)), exit(EXIT_FAILURE);
        
    while (1)
    {
        //Accettazioni dei client in ingresso e assegnazione del socket descriptor
        if((client_sd = accept(server_sd, (struct sockaddr *)&client_addr, &client_lngth)) == -1)
            perror("[***] Error! Can't accept connection. For more information"), exit(EXIT_FAILURE);
        
        inet_ntop(AF_INET, &client_addr.sin_addr, client_ip, INET_ADDRSTRLEN);  //Memorizzazione dell'indirizzo del client accettato in formato 0.0.0.0
        printf("[#] Accepted connection from the server to client %s.\n", client_ip);
        
        //Inizializzazione del socket descriptor che verrà gestito dal thread
        thread_sd = (int *) malloc(sizeof(int));
        *thread_sd = client_sd;
        
        //Creazione dei thread per gestire ogni client connesso
        if((check_value = pthread_create(&tid, &attr, handle_connection, (void *)thread_sd)) != 0)
            fprintf(stderr,"[***] Error! Can't create thread. For more information: %s",strerror(check_value)), exit(EXIT_FAILURE);
        
    }

    //Deallocazione memoria
    free((void *)thread_sd);

    return 0;
}

/*
FUNZIONE handle_connection:
- Gestisce la richiesta usando appropriate funzioni
*/
void *handle_connection(void *arg)
{
    //Dichiarazione variabili locali
    char buf[MAXBUFF];
    int flag, client_sd = *((int *)arg);
    struct timespec ttime;
    
    
    printf("[#] Thread %d handle client connection %d with IP: %s\n", (int)pthread_self(), client_sd, client_ip);    
    
    //Thread aspetta per ricevere la richiesta del client
    ttime.tv_sec = 1;
    nanosleep(&ttime,NULL);

    //Lettura sulla socket e ricezione della richiesta da parte del client
    if(read(client_sd,buf,MAXBUFF) == -1)
        perror("[***] Error! Can't read file. For more information: "), exit(EXIT_FAILURE);

    //Lettura della flag inviata dal client
    handle_request(&flag, buf);
    
    
    switch (flag)
    {
        case 0:
            send_limits(client_sd);
            break;

        case 1:
            receive_values(client_sd, buf);
            break;

        case 2:
            print_time(client_sd);
            break;   
    }
    if ((close(client_sd)) == -1)
    {
        perror("Error! Can't close sd.");
        exit(EXIT_FAILURE);
    }
    free(arg);
    pthread_exit(NULL);
    
}

/*
FUNZIONE send_limits:
- Manda i valori soglia ai client
*/
void send_limits(int client_sd)
{
    //Dichiarazioni variabili locali
    char buffer[10];
    
    //Memorizzo il valore soglia nel buffer
    sprintf(buffer, "%f", LIMIT);
    
    //Manda il valore soglia al client
    if(write(client_sd, buffer, 4) == -1)
        perror("[***] Error! Can't write on socket. For more information"), exit(EXIT_FAILURE);
    
    printf("[#] Sent limit value to client %s.\n", client_ip);
}

/*
FUNZIONE receive_values:
- Riceve la posizione della buca
- Riceve la variazione rispetto al valore soglia
*/
void receive_values(int client_sd, char buf[])
{
    //Dichiarazione variabili locali
    int buf_index = 0;
    char longitude[MAXBUFF], latitude[MAXBUFF], variation[MAXBUFF];
    
    /*
    Estrazione delle coordinate e della variazione dal messaggio del client
    Esempio: 35739.390+36483.474-3.45#
    - Latitudine: prima del '+'
    - Longitudine: dopo il '+' e prima di '-'
    - Variazione: dopo il '-' e prima di '#'
    */
    while(buf[buf_index] != '#')
    {
        for(int i = 0; buf[buf_index] == '+'; i++, buf_index++)
            latitude[i] = buf[buf_index];  
        
        for (int i = 0; buf[buf_index] == '-'; i++, buf_index++)
            longitude[i] = buf[buf_index];

        for (int i = 0; buf[buf_index] == '#'; i++, buf_index++)
            variation[i] = buf[buf_index];
    }

    printf("[#] These are client %s coordinates: X - %s; Y - %s. Variations in relation to %s.\n", client_ip, longitude, latitude, variation);
}

/*
FUNZIONE handle_request:
- Traduce la richiesta del client
- Riformatta la stringa in modo opportuno nel caso di invio di posizione e variazione rispetto al valore soglia
- Restituisce una flag per indicare al server come gestire la richiesta
*/
void handle_request(int *flag,char buf[])
{
    //Dichiarazioni varaibili locali
    int old_index = 1, new_index = 0;
    
    /*
    Confronto il primo carattere della stringa e vedo di che tipo è la richiesta:
    
    - 0 -> il client vuole ricevere i valori soglia */
    if(buf[0] == '0')
        *flag = 0, printf("[#] Client %s request is type 0: client requires limits value.\n", client_ip);
    
    //- 1 -> il client vuole comunicare la sua posizione e il valore del cambiamento rispetto ai valori soglia
    else if(buf[0] == '1')
    {
        printf("[#] Client %s request is type 1: client is sending his position beacause has exceeded the limits values.\n", client_ip);
        *flag = 1;

        //Ciclo per prendere posizione, variazione e memorizzarlo in una stringa (POSSIBILE STRUCT)
        while(buf[old_index] != '#')
        {
            buf[new_index] = buf[old_index];
            new_index++;
            old_index++;
        }
        
        buf[new_index++] = '#';
        buf[new_index] = '\0';
    }
    //- 2 -> il client vuole ricevere gli eventi nella sua zona
    else if(buf[0] == '2')
    {
        //DA CAMBIARE POICHE' MANDA ANCHE POSIZIONE
        *flag = 2;
        printf("[#] Client %s request is type 2: client requires potholes in his zone.\n", client_ip);
    }
    //Caso in cui il messaggio sia corrotto o non formattato correttamente
    else
        printf("[***] Error! String is not formatted properly\n"), exit(EXIT_FAILURE);
}

void print_time(int client_sd)
{
    char buffer[265];
    time_t now;
    
    time(&now);
    ctime_r(&now, buffer);

    if(write(client_sd, buffer, 265) == -1)
    {
        perror("Error! Can't write on socket. For more information");
        exit(EXIT_FAILURE);
    }
    printf("Time sent succesfully.\n\n");

}

void read_msg(int client_sd)
{
    char buf[100];
    
    if(read(client_sd,buf,100) == -1)
    {
        perror("Error! Can't read file. For more information: ");
        exit(EXIT_FAILURE);
    }
    printf("My client message: %s\n\n",buf);
    
    printf("I'm writing answer . . .\n");
    strcpy(buf,"Thanks for your beautiful message\n");
    if(write(client_sd,buf,strlen(buf)) == -1) 
    {
        perror("Error! Can't write sd.");
        exit(EXIT_FAILURE);
    }
    printf("Answer sent succesfully.\n\n");

}

void fix_stop(int signum)
{
    
    int error_type;
    
    printf("\n[***] Closing application.\n");
    sleep(1);
    if ((error_type = pthread_attr_destroy(&attr)) != 0) 
    {
        printf("Error! Can't destroy thread attribute. For more information: %s\n", strerror(error_type));
        exit(EXIT_FAILURE);
    }
	exit(EXIT_SUCCESS);
}