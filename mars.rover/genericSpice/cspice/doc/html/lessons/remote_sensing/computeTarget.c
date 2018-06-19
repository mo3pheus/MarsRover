#include <stdio.h>
#include "SpiceUsr.h"

#define STRLEN 50
#define META_KER "computePhoebePosition.mkl"
#define SEPARATOR "\n======================================================================\n"

int main(void){
	/* Variable definitions */
	SpiceChar utcRequestTime[STRLEN] = "06/11/2004 19:32:00";
	SpiceDouble ltime; //light time
	SpiceDouble state[6];
	SpiceDouble pos[3];
	SpiceDouble dist;

	printf("\n This is the compute target position exercise. \n ");
	printf("\n You entered :: %s \n", utcRequestTime );

	furnsh_c(META_KER);

	//
	SpiceDouble ephemerisTime = 0.0;
	str2et_c( utcRequestTime, &ephemerisTime);
	printf("\n Ephemeris Time is :: %lf\n", ephemerisTime);	

	// make the spkezr call here	
	printf(SEPARATOR);
	spkezr_c( "PHOEBE", ephemerisTime, "J2000", "LT+S", "CASSINI", state, &ltime );
	printf(  "\n      X = %16.3f\n", state[0]       );
	printf ( "      Y = %16.3f\n", state[1]       );
        printf ( "      Z = %16.3f\n", state[2]       );
        printf ( "     VX = %16.3f\n", state[3]       );
        printf ( "     VY = %16.3f\n", state[4]       );
        printf ( "     VZ = %16.3f\n", state[5]       );
	printf( SEPARATOR );

	// compute the position spkpos
	spkpos_c( "EARTH", ephemerisTime, "J2000", "LT+S", "CASSINI", pos, &ltime);

	printf( SEPARATOR );
	printf( SEPARATOR );
 	 printf ( "\n\n   Apparent position of Earth as "
               "seen from CASSINI in the J2000\n"     );
         printf ( "      frame (km): \n"                 );
         printf ( "      X = %16.3f\n", pos[0]           );
         printf ( "      Y = %16.3f\n", pos[1]           );
         printf ( "      Z = %16.3f\n", pos[2]           );
	printf( SEPARATOR );

	printf(SEPARATOR);
	printf("\n One way light time between 	CASSINI and EARTH (seconds) : %16.3f\n", ltime);
	printf(SEPARATOR);

	//Compute the aparent position of the Sun as seen from Phoebe in the J2000 frame.
	spkpos_c( "SUN", ephemerisTime, "J2000", "LT+S", "PHOEBE", pos, &ltime );
	printf ( "\n\n   Apparent position of Sun as "
               "seen from PHOEBE in the J2000\n"     );
         printf ( "      frame (km): \n"                 );
         printf ( "      X = %16.3f\n", pos[0]           );
         printf ( "      Y = %16.3f\n", pos[1]           );
         printf ( "      Z = %16.3f\n", pos[2]           );
        printf( SEPARATOR );

	printf( SEPARATOR );
	printf("\n Distance between the body centers in km " );
	dist = vnorm_c( pos );
        //	convrt_c( dist, "KM", "AU", &dist );
	printf(" %16.3f \n", dist );
	convrt_c( dist, "KM", "AU", &dist );
	printf("\n Distance between the body centers in AU :: %16.3f \n ", dist);
	printf(SEPARATOR);
}	
