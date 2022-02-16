#include "server.h"

/*
FUNZIONE handleSignal:
- Gestisce i signal che arrivano al server
- Dealloca memoria e chiude il server
*/
void handleSignal(int signum)
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

/*
FUNZIONE handleRequest:
- Traduce la richiesta del client
- Riformatta la stringa in modo opportuno nel caso di invio di posizione e variazione rispetto al valore soglia
- Restituisce una flag per indicare al server come gestire la richiesta
*/
void handleRequest(int client_sd, int *flag, char buf[])
{
    //Dichiarazioni varaibili locali
    int old_index = 1, new_index = 0;
    
    /*
    Confronto il primo carattere della stringa e vedo di che tipo è la richiesta:
    
    - 0 -> il client vuole ricevere i valori soglia */
    if(buf[0] == '0')
        *flag = 0, printf("[#] Client %d request is type 0: client requires limits value.\n", client_sd);
    
    //- 1 -> il client vuole comunicare la sua posizione e il valore del cambiamento rispetto ai valori soglia
    else if(buf[0] == '1')
    {
        printf("[#] Client %d request is type 1: client is sending his position beacause has exceeded the limits values.\n", client_sd);
        *flag = 1;

        //Ciclo che elimina il primo elemento che indica il tipo di richiesta e salva nello stesso buffer posizione e variazione 
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
        *flag = 2;
        printf("[#] Client %d request is type 2: client requires potholes in his zone.\n", client_sd);

        //Ciclo che elimina il primo elemento che indica il tipo di richiesta e salva nello stesso buffer la posizione attuale del client
        while(buf[old_index] != '#')
        {
            buf[new_index] = buf[old_index];
            new_index++;
            old_index++;
        }
        
        buf[new_index++] = '#';
        buf[new_index] = '\0';
    }
    //Caso in cui il messaggio sia corrotto o non formattato correttamente
    else
        printf("[***] Error! String is not formatted properly\n"), exit(EXIT_FAILURE);
}

/*
FUNZIONE extractHole:
- Estrazione dati effettivi dalla stringa mandata su socket
- Impachettamento dati
*/
void extractHole(clientData **hole, char buf[])
{
    int buf_index = 0, i;
    char latitude[MAXBUFF], longitude[MAXBUFF], variation[MAXBUFF];

    /*
    Estrazione delle coordinate e della variazione dal messaggio del client
    Esempio: 35739.390*36483.474$3.45#
    - Latitudine: prima del '*'
    - Longitudine: dopo il '*' e prima di '$'
    - Variazione: dopo il '$' e prima di '#'
    */
    
    for(i = 0; buf[buf_index] != '*'; i++, buf_index++)
        latitude[i] = buf[buf_index];
        
    latitude[i] = '\0';
    buf_index++;  
        
    for (i = 0; buf[buf_index] != '$'; i++, buf_index++)
        longitude[i] = buf[buf_index];
    longitude[i] = '\0';
    buf_index++;
        
    for (i = 0; buf[buf_index] != '#'; i++, buf_index++)
        variation[i] = buf[buf_index];
    variation[i] = '\0';
    

    //Impachettamento dati in una struct
    *hole = createNode("fra", latitude, longitude, variation);
}

/*
FUNZIONE saveHole:
- Si connette al database MySQL locale del server
- Salva i dati ricevuti dal client nel database
N.B: Aggiungi -lmysqlclient quando compili
*/
void saveHole(clientData *hole)
{
    //Dichiarazione variabili locali
    MYSQL *con;
    char query[MAXBUFF];
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] Error! Can't initialize database connection. For more information: %s\n", mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, DB_NAME, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] Error! Can't connect to database. For more information: %s\n", mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] Connected to database.\n");

    //Preparazione della query
    sprintf(query, "INSERT INTO %s VALUES ('fra',%f,%f,%f);",DB_TABLE, hole->latitude, hole->longitude, hole->variation);
    
    //Esecuzione della query sul database
    if (mysql_query(con, query))
        fprintf(stderr, "[***] Error! Can't complete the query on the database. For more information: %s\n", mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);

    mysql_close(con); 
}

/*
FUNZIONE extractHole:
- Estrazione dati effettivi dalla stringa mandata su socket
- Impachettamento dati
*/
void extractPosition(clientData **hole, char buf[])
{
    int buf_index = 0, i;
    char latitude[MAXBUFF], longitude[MAXBUFF];

    /*
    Estrazione delle coordinate della posizione attuale del client dal suo messaggio
    Esempio: 35739.390*36483.474#
    - Latitudine: prima del '*'
    - Longitudine: dopo il '*' e prima di '#'
    */
    
    for(i = 0; buf[buf_index] != '*'; i++, buf_index++)
        latitude[i] = buf[buf_index];
        
    latitude[i] = '\0';
    buf_index++;  
        
    for (i = 0; buf[buf_index] != '#'; i++, buf_index++)
        longitude[i] = buf[buf_index];
        
    longitude[i] = '\0';
    
    //Impachettamento dati in una struct
    *hole = createNode("fra", latitude, longitude, "0");
}

/*
FUNZIONE getNearbyHoles:
- Si connette al database MySQL locale del server
- Riprende tutte le buche registrate
- Filtra le buche nelle vicinanze
N.B: Aggiungi -lmysqlclient quando compili
*/
void getNearbyHoles(clientData **holes, clientData *position)
{
    //Dichiarazione variabili locali
    MYSQL *con;
    MYSQL_RES *result;
    MYSQL_ROW row;
    char username[MAXBUFF], longitude[MAXBUFF], latitude[MAXBUFF], variation[MAXBUFF], query[MAXBUFF], json_string[1000] = "{""potholes"":[";
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] Error! Can't initialize database connection. For more information: %s\n", mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, DB_NAME, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] Error! Can't connect to database. For more information: %s\n", mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] Connected to database.\n");

    setvbuf(stdout, NULL, _IONBF, 0);

    sprintf(query,"SELECT * FROM %s", DB_TABLE);
    //Preparazione della query
    if(mysql_query(con, query))
        fprintf(stderr, "[***] Error! Can't complete the query on the database. For more information: %s\n", mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);
    

    //Salvataggio del risultato della query
    if((result = mysql_store_result(con)) == NULL)
        fprintf(stderr, "[***] Error! Can't store the query result. For more information: %s\n", mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);
    
    printf("[#] Query result successfully stored.\n");

    //Impachettamento delle buche nella struct
    while ((row = mysql_fetch_row(result)))
    {
        double distance_client_hole = calculateDistance(position->latitude, position->longitude, atof(row[1]), atof(row[2]));
        
        if(distance_client_hole <= GEO_RADIUS)
            *holes = insert(*holes, row[0], row[1], row[2], row[3]);
    } 
}

/*
FUNZIONE calculate_Distance:
- Calcola la distanza tra la posizione attuale e la buca ripresa dal database (in km)
N.B: Aggiungi -lm quando compili
*/
double calculateDistance(double currPos_latitude, double currPos_longitude, double hole_latitude, double hole_longitude) 
{
    double dLatitude, dLongitude, v_a, v_c, distance;
    
    dLatitude = (hole_latitude - currPos_latitude);
    dLongitude = (hole_longitude - currPos_longitude);
    dLatitude /= 57.29577951;
    dLongitude /= 57.29577951; 
    
    v_a = sin(dLatitude/2) * sin(dLatitude/2) + cos(currPos_latitude) * cos(hole_latitude) * sin(dLongitude/2) * sin(dLongitude/2);
    v_c = 2 * atan2(sqrt(v_a),sqrt(1-v_a));
    
    distance = 6371 * v_c;
    
    return distance;
}

/*
FUNZIONE createJSON:
- Crea una stringa formattata in modo da creare un file JSON con tuttte le buche nelle vicinanze
*/
void createJSON(clientData **holes, char json_string[])
{
    char tmp[1000];
    int length;
    
    //Funzione ricorsiva 
    if(*holes != NULL)
    {
        //Salva una buca come elemento {"username":"??","lat":??,"lon":??,"variation":??}
        sprintf(tmp, "{\"username\":\"%s\",\"lat\":%f,\"lon\":%f,\"var\":%f}", (*holes)->username, (*holes)->latitude, (*holes)->longitude, (*holes)->variation);
        
        //Se il prossimo non è l'ultimo elemento della lista continua a concatenare elementi con la virgola
        if((*holes)->next != NULL)
            strcpy(tmp, strcat(tmp, ","));
        else
        {
            strcpy(tmp, strcat(tmp, "]}")); //Se prossimo elemento della lista è ultimo allora chiude la stringa JSON
            tmp[strlen(tmp)] = '\0';
        }
        strcpy(json_string, strcat(json_string, tmp));
        createJSON(&((*holes)->next), json_string);
    }    
}