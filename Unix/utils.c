#include "server.h"

/*
FUNZIONE getLogTime:
- Restituisce l'ora e data corrente
*/
char* getLogTime()
{
    //Dichiarazione variabili locali
    char *buffer;
    time_t now;
    struct tm* tm_info;

    now = time(NULL);
    tm_info = localtime(&now);
    buffer = (char *) malloc(26);
    strftime(buffer, 26, "%d-%m-%Y %H:%M:%S", tm_info);

    return buffer;
}

/*
FUNZIONE handleSignal:
- Gestisce i signal che arrivano al server
- Dealloca memoria e chiude il server
*/
void handleSignal(int signum)
{
    
    int error_type;
    
    printf("\n[***] [%s] Closing application.\n", getLogTime());
    sleep(1);
    if ((error_type = pthread_attr_destroy(&attr)) != 0) 
    {
        fprintf(stderr, "[***] [%s] Error! Can't destroy thread attribute. For more information: %s\n", getLogTime(), strerror(error_type));
        exit(EXIT_FAILURE);
    }
	exit(EXIT_SUCCESS);
}

/*
FUNZIONE createDatabase:
- Gestisce la creazione del database se arriva signal SIGUSR1
*/
void createDatabase(int signum)
{
    //Dichiarazione variabili locali
    MYSQL *con;
    char query[MAXBUFF];
    int err = 0;
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] [%s] Error! Can't initialize database connection. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, NULL, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] [%s] Error! Can't connect to database. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] [%s] Connected to database.\n", getLogTime());

    //Preparazione della query ovvero creazione database
    sprintf(query, "CREATE DATABASE %s", DB_NAME);
    
    //Esecuzione della query sul database
    if (mysql_query(con, query))
    {
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)); 
        err = mysql_errno(con);
        if(err != 1007) //1007 = Database già esistente
        {
            mysql_close(con);
            exit(EXIT_FAILURE);
        }
    }
    
    if(err == 0)
        printf("[#] [%s] Database created correctly.\n", getLogTime());
        
    query[0] = '\0';
    //Preparazione query per usare database appena creato
    sprintf(query, "USE %s", DB_NAME);
    
    //Esecuzione query
    if(mysql_query(con, query))
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE); 

    query[0] = '\0';
    //Preparazione query per creazione tabella
    sprintf(query, "CREATE TABLE %s (latitude double, longitude double, variation float, CONSTRAINT pk PRIMARY KEY (latitude, longitude))", DB_TABLE);
    
    //Esecuzione query
    if(mysql_query(con, query))
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE); 
    
    if(err == 0)
        printf("[#] [%s] Table %s created correctly.\n", getLogTime(), DB_NAME);
    
    query[0] = '\0';
    //Preparazione query per creazione tabella
    sprintf(query, "CREATE TABLE user (username VARCHAR(32) PRIMARY KEY)");
    
    //Esecuzione query
    if(mysql_query(con, query))
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);
    
    if(err == 0)
        printf("[#] [%s] Table \"user\" created correctly.\n", getLogTime());
    mysql_close(con); 
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
        *flag = 0, printf("[#] [%s] Client %d request is type 0: client requires limits value.\n", getLogTime(), client_sd);
    
    //- 1 -> il client vuole comunicare la sua posizione e il valore del cambiamento rispetto ai valori soglia
    else if(buf[0] == '1')
    {
        printf("[#] [%s] Client %d request is type 1: client is sending his position beacause has exceeded the limits values.\n", getLogTime(), client_sd);
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
        printf("[#] [%s] Client %d request is type 2: client requires potholes in his zone.\n", getLogTime(), client_sd);

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
    else if(buf[0] == '3')
    {
        *flag = 3;
        printf("[#] [%s] Client %d request is type 3: client wants to access.\n", getLogTime(), client_sd);
        
        //Ciclo che elimina il primo elemento che indica il tipo di richiesta e salva nello stesso buffer lo username
        while(buf[old_index] != '#')
        {
            buf[new_index] = buf[old_index];
            new_index++;
            old_index++;
        }
        
        buf[new_index++] = '\0';
    }
    //Caso in cui il messaggio sia corrotto o non formattato correttamente
    else
        fprintf(stderr, "[***] [%s] Error! String is not formatted properly. Try again.\n", getLogTime());
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
    *hole = createNode(latitude, longitude, variation);
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
    int err;
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] [%s] Error! Can't initialize database connection. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, DB_NAME, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] [%s] Error! Can't connect to database. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] [%s] Connected to database.\n", getLogTime());

    //Preparazione della query
    sprintf(query, "INSERT INTO %s VALUES (%f,%f,%f);",DB_TABLE, hole->latitude, hole->longitude, hole->variation);
    
    //Esecuzione della query sul database
    if (mysql_query(con, query))
    {
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)); 
        
        err = mysql_errno(con);
        if(err != 1062) //1062 = Inserito un duplicato
        {
            mysql_close(con);
            exit(EXIT_FAILURE);
        }
    }

    if(err == 0)
        printf("[#] [%s] Data correctly saved.\n", getLogTime());

    mysql_close(con); 
}

/*
FUNZIONE extractPosition:
- Estrazione dati effettivi dalla stringa mandata su socket
- Impachettamento dati
*/
void extractPosition(clientData **position, char buf[])
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
    *position = createNode(latitude, longitude, "0");
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
    char longitude[MAXBUFF], latitude[MAXBUFF], variation[MAXBUFF], query[MAXBUFF];
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] [%s] Error! Can't initialize database connection. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, DB_NAME, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] [%s] Error! Can't connect to database. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] [%s] Connected to database.\n", getLogTime());

    setvbuf(stdout, NULL, _IONBF, 0);

    sprintf(query,"SELECT * FROM %s", DB_TABLE);
    //Preparazione della query
    if(mysql_query(con, query))
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);
    

    //Salvataggio del risultato della query
    if((result = mysql_store_result(con)) == NULL)
        fprintf(stderr, "[***] [%s] Error! Can't store the query result. For more information: %s\n", getLogTime(), mysql_error(con)), mysql_close(con), exit(EXIT_FAILURE);
    
    printf("[#] [%s] Query result successfully stored.\n", getLogTime());

    //Impachettamento delle buche nella struct
    while ((row = mysql_fetch_row(result)))
    {
        double distance_client_hole = calculateDistance(position->latitude, position->longitude, atof(row[0]), atof(row[1]));
        
        if(distance_client_hole <= RADIUS)
            *holes = insert(*holes, row[0], row[1], row[2]);
    } 
}

/*
FUNZIONE calculate_Distance (formula di Haversine):
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
        //Salva una buca come elemento {"lat":??,"lon":??,"variation":??}
        sprintf(tmp, "{\"lat\":%f,\"lon\":%f,\"var\":%f}", (*holes)->latitude, (*holes)->longitude, (*holes)->variation);
        
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

/*
FUNZIONE saveUser
- Salva lo username nel database
- Se duplicato manda valore -1
- Se salvataggio avviene correttamente manda valore 0
*/
int saveUser(char username[])
{
    //Dichiarazione variabili locali
    MYSQL *con;
    char query[MAXBUFF];
    int err = 0;
    
    //Inizializzazione connessione al database
    if((con = mysql_init(NULL)) == NULL)
        fprintf(stderr, "[***] [%s] Error! Can't initialize database connection. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);

    //Connessione al database
    if(mysql_real_connect(con, DB_HOST, DB_USER, DB_PWD, DB_NAME, 0, NULL, 0) == NULL)
        mysql_close(con), fprintf(stderr, "[***] [%s] Error! Can't connect to database. For more information: %s\n", getLogTime(), mysql_error(con)), exit(EXIT_FAILURE);
    printf("[#] [%s] Connected to database.\n", getLogTime());

    //Preparazione della query
    sprintf(query, "INSERT INTO user (username) VALUES ('%s')", username);
    
    //Esecuzione della query sul database
    if (mysql_query(con, query))
    {
        fprintf(stderr, "[***] [%s] Error! Can't complete the query on the database. For more information: %s\n", getLogTime(), mysql_error(con)); 
        
        err = mysql_errno(con);
        if(err != 1062) //1062 = Inserito un duplicato
        {
            mysql_close(con);
            exit(EXIT_FAILURE);
        }
        else if(err == 1062)
            err = -1;
    }

    if(err == 0)
        printf("[#] [%s] Username %s correctly saved.\n", getLogTime(), username);

    mysql_close(con); 
    
    return err; 
}
