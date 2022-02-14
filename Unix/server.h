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
#include <string.h>
#include <sys/un.h>
#include <time.h>
#include <arpa/inet.h>

#define MYPORT 10000
#define MAXBUFF 200
#define LIMIT 2.0

void print_time(int);
void read_msg(int);
void fix_stop(int);
void send_limits(int);
void *handle_connection(void *arg);
void handle_request(int *, char []);
void receive_values(int, char []);

pthread_attr_t attr;
struct sockaddr_in server_addr;
struct sockaddr_in client_addr;
char client_ip[MAXBUFF];