#include<stdio.h>
#include "SpiceUsr.h"

int main(){
	ConstSpiceChar *version;
	version = tkvrsn_c( "TOOLKIT" );
	printf(" Toolkit version %s\n", version);
	return(0);
}
