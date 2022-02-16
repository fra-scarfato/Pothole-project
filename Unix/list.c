#include "server.h"

clientData* createNode(char username[], char latitude[], char longitude[], char variation[])
{
    clientData* node = (clientData *)malloc(sizeof(clientData));
    
    if(node == NULL)
        printf("[***] Error! Can't allocate space (clientData **).\n"), exit(EXIT_FAILURE);

    strcpy(node->username, username);
    node->latitude = atof(latitude);
    node->longitude = atof(longitude);
    node->variation = (float)atof(variation);
    node->next = NULL;
    return node;
}

clientData* insert(clientData* l, char username[], char latitude[], char longitude[], char variation[])
{
    if(l == NULL)
        l = createNode(username, latitude, longitude, variation);
    else    
        l->next = insert(l->next, username, latitude, longitude, variation);

    return l;
}