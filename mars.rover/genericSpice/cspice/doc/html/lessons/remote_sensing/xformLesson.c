/**********************************************************************************************
*	INCLUDES SECTIN
**********************************************************************************************/
#include <stdio.h>
#include "SpiceUsr.h"

#define META_KERNEL "xformMetaKernel.mkl"
#define STR_LEN 50
#define SEPARATOR "\n=======================================================================\n"

void printTransformationMatrix(SpiceDouble matrix[6][6]){
	for(int i = 0; i < 6; i++){
		for(int j =0; j < 6; j++){
			printf(" %d,%d :: %f |", i,j, matrix[i][j] );
		}
		printf("\n");
	}
}

int main(void){
	SpiceChar userUTCTime[STR_LEN] = "06/11/2004 19:32:00";
	SpiceDouble et,lightTime;
	SpiceDouble bfixst[6];
	SpiceDouble statePhoebe[6];
	SpiceDouble posnPhoebe[3];
	SpiceDouble stateTransformationMatrix[6][6];
	SpiceDouble xformHG_J2000[3][3];
	SpiceDouble posEarthCassini[3];
	SpiceDouble lightTimeCassiniEarth;
	SpiceDouble bsight[3];
	SpiceDouble angularSeparation;

	printf(SEPARATOR);
	printf("\n This task deals with transformations of spaceCraft orientation ");
	furnsh_c(META_KERNEL);
	printf("\n  User Entered UTC Time = %s", userUTCTime );
	str2et_c( userUTCTime, &et );
	printf("\n Computed ephemeris time is :: %lf \n", et );
	printf(SEPARATOR);

	//Find the state vector for Phoebe
	printf(SEPARATOR);
	spkezr_c( "PHOEBE", et, "J2000", "LT+S", "CASSINI", statePhoebe, &lightTime );
	printf("\n State Vector for Phoebe as seen from Cassini at above mentioned ephemerisTime\n ");
        printf(  "\n      X = %16.3f\n", statePhoebe[0]       );
        printf ( "      Y = %16.3f\n", statePhoebe[1]       );
        printf ( "      Z = %16.3f\n", statePhoebe[2]       );
        printf ( "     VX = %16.3f\n", statePhoebe[3]       );
        printf ( "     VY = %16.3f\n", statePhoebe[4]       );
        printf ( "     VZ = %16.3f\n", statePhoebe[5]       );

	printf("\nLight time between Phoebe and Cassini at %f :: is %f ", et, lightTime );
	printf(SEPARATOR);

	//Find the angular separation between apparent position of earth  as seen from CASSINI and
	//the nominal boresight of the CASSINI high gain antenna

	// antenna - cassini - earth  - use the position vector to compute the angular separation.
	// code goes here.

	//compute the state transformation matrix sxfrom
	sxform_c( "J2000", "IAU_PHOEBE", et - lightTime, stateTransformationMatrix );
	printf( SEPARATOR );
	printTransformationMatrix(stateTransformationMatrix);
	printf( SEPARATOR );

	//Rotate the state vector using the transformationMatrix
	// So you are converting a state vector from the J2000 frame to the IAU_PHOEBE frame
	mxvg_c( stateTransformationMatrix, statePhoebe, 6, 6, bfixst);

	/* Display the results. */
      	printf ( "   Apparent state of Phoebe as seen "
        	       "from CASSINI in the IAU_PHOEBE\n"    );
      	printf ( "      body-fixed frame (km, km/s):\n");
      	printf ( "      X = %19.6f\n", bfixst[0]       );
      	printf ( "      Y = %19.6f\n", bfixst[1]       );
      	printf ( "      Z = %19.6f\n", bfixst[2]       );
      	printf ( "     VX = %19.6f\n", bfixst[3]       );
      	printf ( "     VY = %19.6f\n", bfixst[4]       );
      	printf ( "     VZ = %19.6f\n", bfixst[5]       );

	/* Compute the position of earth wrt Cassini in J2000 frame  */
	spkpos_c( "EARTH", et, "J2000", "LT+S", "CASSINI",
		posEarthCassini, &lightTimeCassiniEarth);
	printf(SEPARATOR);
	printf("\n Position Vector for Earth as seen from Cassini at above mentioned ephemerisTime\n ");
        printf(  "\n      X = %16.3f\n", posEarthCassini[0]       );
        printf ( "      Y = %16.3f\n", posEarthCassini[1]       );
        printf ( "      Z = %16.3f\n", posEarthCassini[2]       );
        printf(SEPARATOR);

	/* From the Cassini frames kernel - we know that the HG Antenna is aligned
	with the z-axis of the spacecraft. */
	bsight[0] = 0.0;
	bsight[1] = 0.0;
	bsight[2] = 1.0;

	/* Compute the rotation matrix from Cassini HGA frame to J2000 frame */
	pxform_c( "CASSINI_HGA", "J2000", et, xformHG_J2000);

	/* Multiply the result to obtain the nominal antenna boresight in the J2000 frame */
	mxv_c( xformHG_J2000, bsight, bsight );

	/* Compute angular separation */
	convrt_c( vsep_c( bsight, posEarthCassini ), "RADIANS", "DEGREES", &angularSeparation );
	printf(" Angular separation between the apparent position of Earth and the\n CASSINI high gain antenna boresight (degrees) :\n" );
	printf(" %16.3f\n", angularSeparation);
	printf(SEPARATOR);
}

